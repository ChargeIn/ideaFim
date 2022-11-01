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

package com.flop.idea.fim.action.motion.leftright

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimMotionGroupBase
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.handler.ShiftedSpecialKeyHandler
import com.flop.idea.fim.helper.inInsertMode
import com.flop.idea.fim.helper.inSelectMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString

class MotionShiftEndAction : ShiftedSpecialKeyHandler() {

  override val type: Command.Type = Command.Type.OTHER_READONLY

  override fun motion(editor: FimEditor, context: ExecutionContext, cmd: Command, caret: FimCaret) {
    var allow = false
    if (editor.inInsertMode) {
      allow = true
    } else if (editor.inVisualMode || editor.inSelectMode) {
      val opt = (
        injector.optionService.getOptionValue(
          OptionScope.LOCAL(editor),
          OptionConstants.selectionName
        ) as FimString
        ).value
      if (opt != "old") {
        allow = true
      }
    }

    val newOffset = injector.motion.moveCaretToLineEndOffset(editor, caret, cmd.count - 1, allow)
    caret.fimLastColumn = FimMotionGroupBase.LAST_COLUMN
    caret.moveToOffset(newOffset)
    caret.fimLastColumn = FimMotionGroupBase.LAST_COLUMN
  }
}
