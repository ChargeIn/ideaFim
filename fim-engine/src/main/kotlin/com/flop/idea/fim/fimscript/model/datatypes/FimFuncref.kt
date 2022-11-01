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

package com.flop.idea.fim.fimscript.model.datatypes

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag

data class FimFuncref(
  val handler: FunctionHandler,
  val arguments: FimList,
  var dictionary: FimDictionary?,
  val type: Type,
) : FimDataType() {

  var isSelfFixed = false

  companion object {
    var lambdaCounter = 1
    var anonymousCounter = 1
  }

  override fun asDouble(): Double {
    throw ExException("E703: using Funcref as a Number")
  }

  override fun asString(): String {
    throw ExException("E729: using Funcref as a String")
  }

  override fun toString(): String {
    return if (arguments.values.isEmpty() && dictionary == null) {
      when (type) {
        Type.LAMBDA -> "function('${handler.name}')"
        Type.FUNCREF -> "function('${handler.name}')"
        Type.FUNCTION -> handler.name
      }
    } else {
      val result = StringBuffer("function('${handler.name}'")
      if (arguments.values.isNotEmpty()) {
        result.append(", ").append(arguments.toString())
      }
      result.append(")")
      return result.toString()
    }
  }

  override fun toFimNumber(): FimInt {
    throw ExException("E703: using Funcref as a Number")
  }

  fun execute(name: String, args: List<Expression>, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    if (handler is DefinedFunctionHandler && handler.function.flags.contains(FunctionFlag.DICT)) {
      if (dictionary == null) {
        throw ExException("E725: Calling dict function without Dictionary: $name")
      } else {
        injector.variableService.storeVariable(
          Variable(Scope.LOCAL_VARIABLE, "self"),
          dictionary!!,
          editor,
          context,
          handler.function
        )
      }
    }

    val allArguments = listOf(this.arguments.values.map { SimpleExpression(it) }, args).flatten()
    if (handler is DefinedFunctionHandler && handler.function.isDeleted) {
      throw ExException("E933: Function was deleted: ${handler.name}")
    }
    val handler = when (type) {
      Type.LAMBDA, Type.FUNCREF -> this.handler
      Type.FUNCTION -> {
        injector.functionService.getFunctionHandlerOrNull(handler.scope, handler.name, fimContext)
          ?: throw ExException("E117: Unknown function: ${handler.name}")
      }
    }
    return handler.executeFunction(allArguments, editor, context, fimContext)
  }

  override fun deepCopy(level: Int): FimFuncref {
    return copy()
  }

  override fun lockVar(depth: Int) {
    this.isLocked = true
  }

  override fun unlockVar(depth: Int) {
    this.isLocked = false
  }

  enum class Type {
    LAMBDA,
    FUNCREF,
    FUNCTION,
  }
}
