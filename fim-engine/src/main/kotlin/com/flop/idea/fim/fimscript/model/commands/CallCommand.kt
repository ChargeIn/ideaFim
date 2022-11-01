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

package com.flop.idea.fim.fimscript.model.commands

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.FuncrefCallExpression
import com.flop.idea.fim.fimscript.model.expressions.FunctionCallExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag

/**
 * see "h :call"
 */
class CallCommand(val ranges: Ranges, val functionCall: Expression) : Command.SingleExecution(ranges) {

  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.SELF_SYNCHRONIZED)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    if (functionCall is FunctionCallExpression) {
      val function = injector.functionService.getFunctionHandlerOrNull(
        functionCall.scope,
        functionCall.functionName.evaluate(editor, context, fimContext).value,
        fimContext
      )
      if (function != null) {
        if (function is DefinedFunctionHandler && function.function.flags.contains(FunctionFlag.DICT)) {
          throw ExException(
            "E725: Calling dict function without Dictionary: " +
              (functionCall.scope?.toString() ?: "") + functionCall.functionName.evaluate(editor, context, fimContext)
          )
        }
        function.ranges = ranges
        function.executeFunction(functionCall.arguments, editor, context, this)
        return ExecutionResult.Success
      }

      val name = (functionCall.scope?.toString() ?: "") + functionCall.functionName.evaluate(editor, context, fimContext)
      val funcref = injector.variableService.getNullableVariableValue(Variable(functionCall.scope, functionCall.functionName), editor, context, fimContext)
      if (funcref is FimFuncref) {
        funcref.handler.ranges = ranges
        funcref.execute(name, functionCall.arguments, editor, context, fimContext)
        return ExecutionResult.Success
      }

      throw ExException("E117: Unknown function: $name")
    } else if (functionCall is FuncrefCallExpression) {
      functionCall.evaluateWithRange(ranges, editor, context, fimContext)
      return ExecutionResult.Success
    } else {
      // todo add more exceptions
      throw ExException("E129: Function name required")
    }
  }
}
