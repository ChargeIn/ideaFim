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

package com.flop.idea.fim.group.visual

import com.intellij.find.FindManager
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimVisualMotionGroupBase
import com.flop.idea.fim.command.CommandState
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.command.engine
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim

/**
 * @author Alex Plate
 */
class VisualMotionGroup : FimVisualMotionGroupBase() {
  override fun autodetectVisualSubmode(editor: FimEditor): FimStateMachine.SubMode {
    // IJ specific. See https://youtrack.jetbrains.com/issue/VIM-1924.
    val project = editor.ij.project
    if (project != null && FindManager.getInstance(project).selectNextOccurrenceWasPerformed()) {
      return FimStateMachine.SubMode.VISUAL_CHARACTER
    }

    return super.autodetectVisualSubmode(editor)
  }

  /**
   * COMPATIBILITY-LAYER: Added a method
   * Please see: https://jb.gg/zo8n0r
   */
  fun enterVisualMode(editor: Editor, subMode: CommandState.SubMode? = null): Boolean {
    return this.enterVisualMode(editor.fim, subMode?.engine)
  }
}
