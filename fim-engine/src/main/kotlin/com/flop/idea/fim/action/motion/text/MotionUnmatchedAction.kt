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
package com.flop.idea.fim.action.motion.text

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler
import com.flop.idea.fim.handler.toMotionOrError
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

sealed class MotionUnmatchedAction(private val motionChar: Char) : MotionActionHandler.ForEachCaret() {
  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_SAVE_JUMP)

  override val motionType: MotionType = MotionType.EXCLUSIVE

  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    return moveCaretToUnmatchedBlock(editor, caret, operatorArguments.count1, motionChar)
      .toMotionOrError()
  }
}

class MotionUnmatchedBraceCloseAction : MotionUnmatchedAction('}')
class MotionUnmatchedBraceOpenAction : MotionUnmatchedAction('{')
class MotionUnmatchedParenCloseAction : MotionUnmatchedAction(')')
class MotionUnmatchedParenOpenAction : MotionUnmatchedAction('(')

private fun moveCaretToUnmatchedBlock(editor: FimEditor, caret: FimCaret, count: Int, type: Char): Int {
  return if (editor.currentCaret().offset.point == 0 && count < 0 || editor.currentCaret().offset.point >= editor.fileSize() - 1 && count > 0) {
    -1
  } else {
    var res = injector.searchHelper.findUnmatchedBlock(editor, caret, type, count)
    if (res != -1) {
      res = injector.engineEditorHelper.normalizeOffset(editor, res, false)
    }
    res
  }
}
