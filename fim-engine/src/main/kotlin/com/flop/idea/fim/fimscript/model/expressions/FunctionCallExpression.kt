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

package com.flop.idea.fim.fimscript.model.expressions

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag

data class FunctionCallExpression(val scope: Scope?, val functionName: CurlyBracesName, val arguments: MutableList<Expression>) :
  Expression() {
  constructor(scope: Scope?, functionName: String, arguments: MutableList<Expression>) :
    this(scope, CurlyBracesName(listOf(SimpleExpression(functionName))), arguments)

  override fun evaluate(editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    injector.statisticsService.setIfFunctionCallUsed(true)
    val handler = injector.functionService.getFunctionHandlerOrNull(scope, functionName.evaluate(editor, context, fimContext).value, fimContext)
    if (handler != null) {
      if (handler is DefinedFunctionHandler && handler.function.flags.contains(FunctionFlag.DICT)) {
        throw ExException(
          "E725: Calling dict function without Dictionary: " +
            (scope?.toString() ?: "") + functionName.evaluate(editor, context, fimContext)
        )
      }
      return handler.executeFunction(this.arguments, editor, context, fimContext)
    }

    val funcref = injector.variableService.getNullableVariableValue(Variable(scope, functionName), editor, context, fimContext)
    if (funcref is FimFuncref) {
      val name = (if (scope != null) scope.c + ":" else "") + functionName
      return funcref.execute(name, arguments, editor, context, fimContext)
    }
    throw ExException("E117: Unknown function: ${if (scope != null) scope.c + ":" else ""}${functionName.evaluate(editor, context, fimContext)}")
  }
}
