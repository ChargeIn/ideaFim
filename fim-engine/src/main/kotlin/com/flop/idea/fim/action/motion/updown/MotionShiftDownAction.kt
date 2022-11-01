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

package com.flop.idea.fim.action.motion.updown

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.handler.ShiftedArrowKeyHandler

/**
 * @author Alex Plate
 */

class MotionShiftDownAction : ShiftedArrowKeyHandler(false) {

  override val type: Command.Type = Command.Type.OTHER_READONLY

  override fun motionWithKeyModel(editor: FimEditor, caret: FimCaret, context: ExecutionContext, cmd: Command) {
    val vertical = injector.motion.getVerticalMotionOffset(editor, caret, cmd.count)
    val col = injector.engineEditorHelper.prepareLastColumn(caret)
    caret.moveToOffset(vertical)

    injector.engineEditorHelper.updateLastColumn(caret, col)
  }

  override fun motionWithoutKeyModel(editor: FimEditor, context: ExecutionContext, cmd: Command) {
    injector.motion.scrollFullPage(editor, editor.primaryCaret(), cmd.count)
  }
}
