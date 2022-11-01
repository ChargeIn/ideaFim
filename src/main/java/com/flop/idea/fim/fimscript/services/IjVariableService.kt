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

package com.flop.idea.fim.fimscript.services

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.ex.fimscript.FimScriptGlobalEnvironment
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimBlob
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.Variable

class IjVariableService : FimVariableServiceBase() {
  override fun storeVariable(variable: Variable, value: FimDataType, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext) {
    super.storeVariable(variable, value, editor, context, fimContext)

    val scope = variable.scope ?: getDefaultVariableScope(fimContext)
    if (scope == Scope.GLOBAL_VARIABLE) {
      val scopeForGlobalEnvironment = variable.scope?.toString() ?: ""
      com.flop.idea.fim.ex.fimscript.FimScriptGlobalEnvironment.getInstance()
        .variables[scopeForGlobalEnvironment + variable.name.evaluate(editor, context, fimContext)] = value.simplify()
    }
  }

  private fun FimDataType.simplify(): Any {
    return when (this) {
      is FimString -> this.value
      is FimInt -> this.value
      is FimFloat -> this.value
      is FimList -> this.values
      is FimDictionary -> this.dictionary
      is FimBlob -> "blob"
      is FimFuncref -> "funcref"
      else -> error("Unexpected")
    }
  }
}
