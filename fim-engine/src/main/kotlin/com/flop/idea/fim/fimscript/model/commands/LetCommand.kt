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
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.register.RegisterConstants
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.Script
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimBlob
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.EnvVariableExpression
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.OneElementSublistExpression
import com.flop.idea.fim.fimscript.model.expressions.OptionExpression
import com.flop.idea.fim.fimscript.model.expressions.Register
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.SublistExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.expressions.operators.AssignmentOperator
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag

/**
 * see "h :let"
 */
data class LetCommand(
  val ranges: Ranges,
  val variable: Expression,
  val operator: AssignmentOperator,
  val expression: Expression,
  val isSyntaxSupported: Boolean,
) : Command.SingleExecution(ranges) {

  companion object {
    private val logger = fimLogger<LetCommand>()
  }
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  @Throws(ExException::class)
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    if (!isSyntaxSupported) return ExecutionResult.Error
    when (variable) {
      is Variable -> {
        if ((variable.scope == Scope.SCRIPT_VARIABLE && fimContext.getFirstParentContext() !is Script) ||
          (!isInsideFunction(fimContext) && (variable.scope == Scope.FUNCTION_VARIABLE || variable.scope == Scope.LOCAL_VARIABLE))
        ) {
          throw ExException("E461: Illegal variable name: ${variable.toString(editor, context, fimContext)}")
        }

        if (isReadOnlyVariable(variable, editor, context)) {
          throw ExException("E46: Cannot change read-only variable \"${variable.toString(editor, context, fimContext)}\"")
        }

        val leftValue = injector.variableService.getNullableVariableValue(variable, editor, context, fimContext)
        if (leftValue?.isLocked == true && (leftValue.lockOwner as? Variable)?.name == variable.name) {
          throw ExException("E741: Value is locked: ${variable.toString(editor, context, fimContext)}")
        }
        val rightValue = expression.evaluate(editor, context, fimContext)
        injector.variableService.storeVariable(variable, operator.getNewValue(leftValue, rightValue), editor, context, this)
      }

      is OneElementSublistExpression -> {
        when (val containerValue = variable.expression.evaluate(editor, context, fimContext)) {
          is FimDictionary -> {
            val dictKey = FimString(variable.index.evaluate(editor, context, this).asString())
            if (operator != AssignmentOperator.ASSIGNMENT && !containerValue.dictionary.containsKey(dictKey)) {
              throw ExException("E716: Key not present in Dictionary: $dictKey")
            }
            val expressionValue = expression.evaluate(editor, context, this)
            var valueToStore = if (dictKey in containerValue.dictionary) {
              if (containerValue.dictionary[dictKey]!!.isLocked) {
                // todo better exception message
                throw ExException("E741: Value is locked: ${variable.originalString}")
              }
              operator.getNewValue(containerValue.dictionary[dictKey]!!, expressionValue)
            } else {
              if (containerValue.isLocked) {
                // todo better exception message
                throw ExException("E741: Value is locked: ${variable.originalString}")
              }
              expressionValue
            }
            if (valueToStore is FimFuncref && !valueToStore.isSelfFixed &&
              valueToStore.handler is DefinedFunctionHandler &&
              (valueToStore.handler as DefinedFunctionHandler).function.flags.contains(FunctionFlag.DICT)
            ) {
              valueToStore = valueToStore.copy()
              valueToStore.dictionary = containerValue
            }
            containerValue.dictionary[dictKey] = valueToStore
          }
          is FimList -> {
            // we use Integer.parseInt(........asString()) because in case if index's type is Float, List, Dictionary etc
            // fim throws the same error as the asString() method
            val index = Integer.parseInt(variable.index.evaluate(editor, context, this).asString())
            if (index > containerValue.values.size - 1) {
              throw ExException("E684: list index out of range: $index")
            }
            if (containerValue.values[index].isLocked) {
              throw ExException("E741: Value is locked: ${variable.originalString}")
            }
            containerValue.values[index] = operator.getNewValue(containerValue.values[index], expression.evaluate(editor, context, fimContext))
          }
          is FimBlob -> TODO()
          else -> throw ExException("E689: Can only index a List, Dictionary or Blob")
        }
      }

      is SublistExpression -> {
        if (variable.expression is Variable) {
          val variableValue = injector.variableService.getNonNullVariableValue(variable.expression, editor, context, this)
          if (variableValue is FimList) {
            // we use Integer.parseInt(........asString()) because in case if index's type is Float, List, Dictionary etc
            // fim throws the same error as the asString() method
            val from = Integer.parseInt(variable.from?.evaluate(editor, context, this)?.toString() ?: "0")
            val to = Integer.parseInt(
              variable.to?.evaluate(editor, context, this)?.toString()
                ?: (variableValue.values.size - 1).toString()
            )

            val expressionValue = expression.evaluate(editor, context, this)
            if (expressionValue !is FimList && expressionValue !is FimBlob) {
              throw ExException("E709: [:] requires a List or Blob value")
            } else if (expressionValue is FimList) {
              if (expressionValue.values.size < to - from + 1) {
                throw ExException("E711: List value does not have enough items")
              } else if (variable.to != null && expressionValue.values.size > to - from + 1) {
                throw ExException("E710: List value has more items than targets")
              }
              val newListSize = expressionValue.values.size - (to - from + 1) + variableValue.values.size
              var i = from
              if (newListSize > variableValue.values.size) {
                while (i < variableValue.values.size) {
                  variableValue.values[i] = expressionValue.values[i - from]
                  i += 1
                }
                while (i < newListSize) {
                  variableValue.values.add(expressionValue.values[i - from])
                  i += 1
                }
              } else {
                while (i <= to) {
                  variableValue.values[i] = expressionValue.values[i - from]
                  i += 1
                }
              }
            } else if (expressionValue is FimBlob) {
              TODO()
            }
          } else {
            throw ExException("wrong variable type")
          }
        }
      }

      is OptionExpression -> {
        val optionValue = variable.evaluate(editor, context, fimContext)
        if (operator == AssignmentOperator.ASSIGNMENT || operator == AssignmentOperator.CONCATENATION ||
          operator == AssignmentOperator.ADDITION || operator == AssignmentOperator.SUBTRACTION
        ) {
          val newValue = operator.getNewValue(optionValue, expression.evaluate(editor, context, this))
          when (variable.scope) {
            Scope.GLOBAL_VARIABLE -> injector.optionService.setOptionValue(OptionScope.GLOBAL, variable.optionName, newValue, variable.originalString)
            Scope.LOCAL_VARIABLE -> injector.optionService.setOptionValue(OptionScope.LOCAL(editor), variable.optionName, newValue, variable.originalString)
            else -> throw ExException("Invalid option scope")
          }
        } else {
          TODO()
        }
      }

      is EnvVariableExpression -> TODO()

      is Register -> {
        if (RegisterConstants.WRITABLE_REGISTERS.contains(variable.char)) {
          val result = injector.registerGroup.storeText(variable.char, expression.evaluate(editor, context, fimContext).asString())
          if (!result) {
            logger.error(
              """
              Error during `let ${variable.originalString} ${operator.value} ${expression.originalString}` command execution.
              Could not set register value
              """.trimIndent()
            )
          }
        } else if (RegisterConstants.VALID_REGISTERS.contains(variable.char)) {
          throw ExException("E354: Invalid register name: '${variable.char}'")
        } else {
          throw ExException("E18: Unexpected characters in :let")
        }
      }

      else -> throw ExException("E121: Undefined variable")
    }
    return ExecutionResult.Success
  }

  private fun isInsideFunction(fimLContext: FimLContext): Boolean {
    var isInsideFunction = false
    var node = fimLContext
    while (!node.isFirstParentContext()) {
      if (node is FunctionDeclaration) {
        isInsideFunction = true
      }
      node = node.getPreviousParentContext()
    }
    return isInsideFunction
  }

  private fun isReadOnlyVariable(variable: Variable, editor: FimEditor, context: ExecutionContext): Boolean {
    if (variable.scope == Scope.FUNCTION_VARIABLE) return true
    if (variable.scope == null && variable.name.evaluate(editor, context, fimContext).value == "self" && isInsideDictionaryFunction()) return true
    return false
  }

  private fun isInsideDictionaryFunction(): Boolean {
    var node: FimLContext = this
    while (!node.isFirstParentContext()) {
      if (node is FunctionDeclaration && node.flags.contains(FunctionFlag.DICT)) {
        return true
      }
      node = node.getPreviousParentContext()
    }
    return false
  }
}
