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

package com.flop.idea.fim.newapi

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.IdeLookup
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLookupManager

@Service
class IjFimLookupManager : FimLookupManager {
  override fun getActiveLookup(editor: FimEditor): IjLookup? {
    return LookupManager.getActiveLookup(editor.ij)?.let { IjLookup(it) }
  }
}

class IjLookup(val lookup: Lookup) : IdeLookup {
  override fun down(caret: FimCaret, context: ExecutionContext) {
    EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN)
      .execute(caret.editor.ij, caret.ij, context.ij)
  }

  override fun up(caret: FimCaret, context: ExecutionContext) {
    EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_MOVE_CARET_UP)
      .execute(caret.editor.ij, caret.ij, context.ij)
  }
}
