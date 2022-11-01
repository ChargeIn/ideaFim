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

package com.flop.idea.fim.fimscript.model.statements

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.Executable
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.OneElementSublistExpression
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler

data class AnonymousFunctionDeclaration(
  val sublist: OneElementSublistExpression,
  val args: List<String>,
  val defaultArgs: List<Pair<String, Expression>>,
  val body: List<Executable>,
  val replaceExisting: Boolean,
  val flags: Set<FunctionFlag>,
  val hasOptionalArguments: Boolean,
) : Executable {

  override lateinit var fimContext: FimLContext

  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    val container = sublist.expression.evaluate(editor, context, fimContext)
    if (container !is FimDictionary) {
      throw ExException("E1203: Dot can only be used on a dictionary")
    }
    val index = ((sublist.index as SimpleExpression).data as FimString)
    if (container.dictionary.containsKey(index)) {
      if (container.dictionary[index] is FimFuncref && !replaceExisting) {
        throw ExException("E717: Dictionary entry already exists")
      } else if (container.dictionary[index] !is FimFuncref) {
        throw ExException("E718: Funcref required")
      }
    }
    val declaration = FunctionDeclaration(null, FimFuncref.anonymousCounter++.toString(), args, defaultArgs, body, replaceExisting, flags + FunctionFlag.DICT, hasOptionalArguments)
    declaration.fimContext = this.fimContext
    container.dictionary[index] = FimFuncref(DefinedFunctionHandler(declaration), FimList(mutableListOf()), container, FimFuncref.Type.FUNCREF)
    container.dictionary[index]
    return ExecutionResult.Success
  }
}
