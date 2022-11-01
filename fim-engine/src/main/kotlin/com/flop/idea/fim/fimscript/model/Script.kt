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

package com.flop.idea.fim.fimscript.model

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration

data class Script(val units: List<Executable> = ArrayList()) : Executable {
  override lateinit var fimContext: FimLContext

  /**
   * we store the "s:" scope variables and functions here
   * see ":h scope"
   */
  val scriptVariables: MutableMap<String, FimDataType> = mutableMapOf()
  val scriptFunctions: MutableMap<String, FunctionDeclaration> = mutableMapOf()

  override fun getPreviousParentContext(): FimLContext {
    throw RuntimeException("Script has no parent context")
  }

  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    var latestResult: ExecutionResult = ExecutionResult.Success
    for (unit in units) {
      unit.fimContext = this
      if (latestResult is ExecutionResult.Success) {
        latestResult = unit.execute(editor, context)
      } else {
        break
      }
    }
    return latestResult
  }
}
