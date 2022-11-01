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
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimBlob
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.datatypes.asFimInt
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler

object EmptyFunctionHandler : FunctionHandler() {

  override val name = "empty"
  override val minimumNumberOfArguments: Int = 1
  override val maximumNumberOfArguments: Int = 1

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: FimEditor,
    context: ExecutionContext,
    fimContext: FimLContext,
  ): FimDataType {
    val argument = argumentValues[0].evaluate(editor, context, fimContext)
    // TODO: 03.08.2021
    // - |v:false|, |v:none| and |v:null| are empty, |v:true| is not.
    val isEmpty = when (argument) {
      is FimList -> argument.values.isEmpty()
      is FimDictionary -> argument.dictionary.isEmpty()
      is FimString -> argument.value.isEmpty()
      is FimInt -> argument.value == 0
      is FimFloat -> argument.value == 0.0
      is FimFuncref -> false
      is FimBlob -> TODO("Not yet implemented")
      else -> error("Unexpected")
    }
    return isEmpty.asFimInt()
  }
}
