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

package com.flop.idea.fim.fimscript.model.functions.handlers

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler

class JoinFunctionHandler : FunctionHandler() {
  override val name: String = "join"
  override val minimumNumberOfArguments: Int = 1
  override val maximumNumberOfArguments: Int = 2

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: FimEditor,
    context: ExecutionContext,
    fimContext: FimLContext,
  ): FimDataType {
    val firstArgument = argumentValues[0].evaluate(editor, context, fimContext)
    if (firstArgument !is FimList) {
      throw ExException("E714: List required")
    }
    val secondArgument = argumentValues.getOrNull(1)?.evaluate(editor, context, fimContext) ?: FimString(" ")
    return FimString(firstArgument.values.joinToString(secondArgument.asString()) { it.toString() })
  }
}
