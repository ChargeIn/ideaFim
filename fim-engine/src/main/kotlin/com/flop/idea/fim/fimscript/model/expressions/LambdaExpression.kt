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
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.Executable
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.commands.LetCommand
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.expressions.operators.AssignmentOperator
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag
import com.flop.idea.fim.fimscript.model.statements.ReturnStatement

data class LambdaExpression(val args: List<String>, val expr: Expression) : Expression() {

  override fun evaluate(editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimFuncref {
    val function = FunctionDeclaration(null, getFunctionName(), args, listOf(), buildBody(), false, setOf(FunctionFlag.CLOSURE), true)
    function.fimContext = fimContext
    return FimFuncref(DefinedFunctionHandler(function), FimList(mutableListOf()), null, FimFuncref.Type.LAMBDA)
  }

  private fun getFunctionName(): String {
    return "<lambda>" + FimFuncref.lambdaCounter++
  }

  private fun buildBody(): List<Executable> {
    val body = mutableListOf<Executable>()
    for (argument in args) {
      body.add(
        LetCommand(
          Ranges(), Variable(Scope.LOCAL_VARIABLE, argument), AssignmentOperator.ASSIGNMENT,
          Variable(
            Scope.FUNCTION_VARIABLE, argument
          ),
          true
        )
      )
    }
    body.add(ReturnStatement(expr))
    return body
  }
}
