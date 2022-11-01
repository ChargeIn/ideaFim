/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.flop.idea.fim.fimscript.model.functions

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.FinishException
import com.flop.idea.fim.ex.ranges.LineNumberRange
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag

data class DefinedFunctionHandler(val function: FunctionDeclaration) : FunctionHandler() {

  private val logger = fimLogger<DefinedFunctionHandler>()
  override val name = function.name
  override val scope = function.scope
  override val minimumNumberOfArguments = function.args.size
  override val maximumNumberOfArguments get() = if (function.hasOptionalArguments) null else function.args.size + function.defaultArgs.size

  override fun doFunction(argumentValues: List<Expression>, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    var returnValue: FimDataType? = null
    val exceptionsCaught = mutableListOf<ExException>()
    val isRangeGiven = (ranges?.size() ?: 0) > 0

    if (!isRangeGiven) {
      val currentLine = editor.currentCaret().getLogicalPosition().line
      ranges = Ranges()
      ranges!!.addRange(
        arrayOf(
          LineNumberRange(currentLine, 0, false),
          LineNumberRange(currentLine, 0, false)
        )
      )
    }
    initializeFunctionVariables(argumentValues, editor, context, fimContext)

    if (function.flags.contains(FunctionFlag.RANGE)) {
      val line = (injector.variableService.getNonNullVariableValue(Variable(Scope.FUNCTION_VARIABLE, "firstline"), editor, context, function) as FimInt).value
      returnValue = executeBodyForLine(line, isRangeGiven, exceptionsCaught, editor, context)
    } else {
      val firstLine = (injector.variableService.getNonNullVariableValue(Variable(Scope.FUNCTION_VARIABLE, "firstline"), editor, context, function) as FimInt).value
      val lastLine = (injector.variableService.getNonNullVariableValue(Variable(Scope.FUNCTION_VARIABLE, "lastline"), editor, context, function) as FimInt).value
      for (line in firstLine..lastLine) {
        returnValue = executeBodyForLine(line, isRangeGiven, exceptionsCaught, editor, context)
      }
    }

    if (exceptionsCaught.isNotEmpty()) {
      injector.messages.indicateError()
      injector.messages.showStatusBarMessage(exceptionsCaught.last().message)
    }
    return returnValue ?: FimInt(0)
  }

  private fun executeBodyForLine(line: Int, isRangeGiven: Boolean, exceptionsCaught: MutableList<ExException>, editor: FimEditor, context: ExecutionContext): FimDataType? {
    var returnValue: FimDataType? = null
    if (isRangeGiven) {
      editor.currentCaret().moveToLogicalPosition(FimLogicalPosition(line - 1, 0))
    }
    var result: ExecutionResult = ExecutionResult.Success
    if (function.flags.contains(FunctionFlag.ABORT)) {
      for (statement in function.body) {
        statement.fimContext = function
        if (result is ExecutionResult.Success) {
          result = statement.execute(editor, context)
        }
      }
      // todo in release 1.9. we should return value AND throw exception
      when (result) {
        is ExecutionResult.Break -> exceptionsCaught.add(ExException("E587: :break without :while or :for: break"))
        is ExecutionResult.Continue -> exceptionsCaught.add(ExException("E586: :continue without :while or :for: continue"))
        is ExecutionResult.Error -> exceptionsCaught.add(ExException("unknown error occurred")) // todo
        is ExecutionResult.Return -> returnValue = result.value
        is ExecutionResult.Success -> {}
      }
    } else {
      // todo in release 1.9. in this case multiple exceptions can be thrown at once but we don't support it
      for (statement in function.body) {
        statement.fimContext = function
        try {
          result = statement.execute(editor, context)
          when (result) {
            is ExecutionResult.Break -> exceptionsCaught.add(ExException("E587: :break without :while or :for: break"))
            is ExecutionResult.Continue -> exceptionsCaught.add(ExException("E586: :continue without :while or :for: continue"))
            is ExecutionResult.Error -> exceptionsCaught.add(ExException("unknown error occurred")) // todo
            is ExecutionResult.Return -> {
              returnValue = result.value
              break
            }
            is ExecutionResult.Success -> {}
          }
        } catch (e: ExException) {
          if (e is FinishException) {
            // todo in 1.9: also throw all caught exceptions
            throw FinishException()
          }
          exceptionsCaught.add(e)
          logger.warn("Caught exception during execution of function with [abort] flag. Exception: ${e.message}")
        }
      }
    }
    return returnValue
  }

  private fun initializeFunctionVariables(argumentValues: List<Expression>, editor: FimEditor, context: ExecutionContext, functionCallContext: FimLContext) {
    // non-optional function arguments
    for ((index, name) in function.args.withIndex()) {
      injector.variableService.storeVariable(
        Variable(Scope.FUNCTION_VARIABLE, name),
        argumentValues[index].evaluate(editor, context, functionCallContext),
        editor,
        context,
        function
      )
    }
    // optional function arguments with default values
    for (index in 0 until function.defaultArgs.size) {
      val expressionToStore = if (index + function.args.size < argumentValues.size) argumentValues[index + function.args.size] else function.defaultArgs[index].second
      injector.variableService.storeVariable(
        Variable(Scope.FUNCTION_VARIABLE, function.defaultArgs[index].first),
        expressionToStore.evaluate(editor, context, functionCallContext),
        editor,
        context,
        function
      )
    }
    // all the other optional arguments passed to function are stored in a:000 variable
    if (function.hasOptionalArguments) {
      val remainingArgs = if (function.args.size + function.defaultArgs.size < argumentValues.size) {
        FimList(
          argumentValues.subList(function.args.size + function.defaultArgs.size, argumentValues.size)
            .map { it.evaluate(editor, context, functionCallContext) }.toMutableList()
        )
      } else {
        FimList(mutableListOf())
      }
      injector.variableService.storeVariable(
        Variable(Scope.FUNCTION_VARIABLE, "000"),
        remainingArgs,
        editor,
        context,
        function
      )
    }
    injector.variableService.storeVariable(
      Variable(Scope.FUNCTION_VARIABLE, "firstline"),
      FimInt(ranges!!.getFirstLine(editor, editor.currentCaret()) + 1), editor, context, function
    )
    injector.variableService.storeVariable(
      Variable(Scope.FUNCTION_VARIABLE, "lastline"),
      FimInt(ranges!!.getLine(editor, editor.currentCaret()) + 1), editor, context, function
    )
  }
}
