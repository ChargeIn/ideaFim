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
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.Scope

abstract class FunctionHandler {

  abstract val name: String
  open val scope: Scope? = null
  abstract val minimumNumberOfArguments: Int?
  abstract val maximumNumberOfArguments: Int?
  var ranges: Ranges? = null

  protected abstract fun doFunction(argumentValues: List<Expression>, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType

  fun executeFunction(arguments: List<Expression>, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    checkFunctionCall(arguments)
    val result = doFunction(arguments, editor, context, fimContext)
    ranges = null
    return result
  }

  private fun checkFunctionCall(arguments: List<Expression>) {
    if (minimumNumberOfArguments != null && arguments.size < minimumNumberOfArguments!!) {
      throw ExException("E119: Not enough arguments for function: $name")
    }
    if (maximumNumberOfArguments != null && arguments.size > maximumNumberOfArguments!!) {
      throw ExException("E118: Too many arguments for function: $name")
    }
  }
}
