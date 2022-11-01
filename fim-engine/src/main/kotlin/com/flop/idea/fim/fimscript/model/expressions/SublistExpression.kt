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
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString

data class SublistExpression(val from: Expression?, val to: Expression?, val expression: Expression) : Expression() {

  override fun evaluate(editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    val expressionValue = expression.evaluate(editor, context, fimContext)
    val arraySize = when (expressionValue) {
      is FimDictionary -> throw ExException("E719: Cannot slice a Dictionary")
      is FimList -> expressionValue.values.size
      else -> expressionValue.asString().length
    }
    var fromInt = Integer.parseInt(from?.evaluate(editor, context, fimContext)?.asString() ?: "0")
    if (fromInt < 0) {
      fromInt += arraySize
    }
    var toInt = Integer.parseInt(to?.evaluate(editor, context, fimContext)?.asString() ?: (arraySize - 1).toString())
    if (toInt < 0) {
      toInt += arraySize
    }
    return if (expressionValue is FimList) {
      if (fromInt > arraySize) {
        FimList(mutableListOf())
      } else if (fromInt == toInt) {
        expressionValue.values[fromInt]
      } else if (fromInt <= toInt) {
        FimList(expressionValue.values.subList(fromInt, toInt + 1))
      } else {
        FimList(mutableListOf())
      }
    } else {
      if (fromInt > arraySize) {
        FimString("")
      } else if (fromInt <= toInt) {
        if (toInt > expressionValue.asString().length - 1) {
          FimString(expressionValue.asString().substring(fromInt))
        } else {
          FimString(expressionValue.asString().substring(fromInt, toInt + 1))
        }
      } else {
        FimString("")
      }
    }
  }
}
