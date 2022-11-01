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

data class OneElementSublistExpression(val index: Expression, val expression: Expression) : Expression() {

  override fun evaluate(editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    val expressionValue = expression.evaluate(editor, context, fimContext)
    if (expressionValue is FimDictionary) {
      return expressionValue.dictionary[FimString(index.evaluate(editor, context, fimContext).asString())]
        ?: throw ExException(
          "E716: Key not present in Dictionary: \"${
          index.evaluate(editor, context, fimContext).asString()
          }\""
        )
    } else {
      val indexValue = Integer.parseInt(index.evaluate(editor, context, fimContext).asString())
      if (expressionValue is FimList && (indexValue >= expressionValue.values.size || indexValue < 0)) {
        throw ExException("E684: list index out of range: $indexValue")
      }
      if (indexValue < 0) {
        return FimString("")
      }
      return SublistExpression(SimpleExpression(indexValue), SimpleExpression(indexValue), SimpleExpression(expressionValue)).evaluate(editor, context, fimContext)
    }
  }
}
