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
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.Direction
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.Motion.AbsoluteOffset
import com.flop.idea.fim.handler.MotionActionHandler

class MotionBigWordEndLeftAction : WordEndAction(Direction.BACKWARDS, true)
class MotionBigWordEndRightAction : WordEndAction(Direction.FORWARDS, true)
class MotionWordEndLeftAction : WordEndAction(Direction.BACKWARDS, false)
class MotionWordEndRightAction : WordEndAction(Direction.FORWARDS, false)

sealed class WordEndAction(val direction: Direction, val bigWord: Boolean) : MotionActionHandler.ForEachCaret() {
  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    return moveCaretToNextWordEnd(editor, caret, direction.toInt() * operatorArguments.count1, bigWord)
  }

  override val motionType: MotionType = MotionType.INCLUSIVE
}

fun moveCaretToNextWordEnd(editor: FimEditor, caret: FimCaret, count: Int, bigWord: Boolean): Motion {
  if (caret.offset.point == 0 && count < 0 || caret.offset.point >= editor.fileSize() - 1 && count > 0) {
    return Motion.Error
  }

  // If we are doing this move as part of a change command (e.q. cw), we need to count the current end of
  // word if the cursor happens to be on the end of a word already. If this is a normal move, we don't count
  // the current word.
  val pos = injector.searchHelper.findNextWordEnd(editor, caret, count, bigWord)
  return if (pos == -1) {
    if (count < 0) {
      AbsoluteOffset(injector.motion.moveCaretToLineStart(editor, 0))
    } else {
      AbsoluteOffset(injector.motion.moveCaretToLineEnd(editor, editor.lineCount() - 1, false))
    }
  } else {
    AbsoluteOffset(pos)
  }
}
