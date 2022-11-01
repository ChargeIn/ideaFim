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
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler
import com.flop.idea.fim.handler.toMotion
import com.flop.idea.fim.helper.enumSetOf
import com.flop.idea.fim.helper.inInsertMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import java.util.*

class MotionGotoLineLastEndAction : MotionActionHandler.ForEachCaret() {
  override val motionType: MotionType = MotionType.LINE_WISE

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_SAVE_JUMP)

  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    var allow = false
    if (editor.inInsertMode) {
      allow = true
    } else if (editor.inVisualMode) {
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

    return moveCaretGotoLineLastEnd(editor, operatorArguments.count0, operatorArguments.count1 - 1, allow).toMotion()
  }
}

class MotionGotoLineLastEndInsertAction : MotionActionHandler.ForEachCaret() {
  override val motionType: MotionType = MotionType.EXCLUSIVE

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_CLEAR_STROKES)

  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    var allow = false
    if (editor.inInsertMode) {
      allow = true
    } else if (editor.inVisualMode) {
      val opt = (
        injector.optionService
          .getOptionValue(OptionScope.LOCAL(editor), OptionConstants.selectionName) as FimString
        ).value
      if (opt != "old") {
        allow = true
      }
    }

    return moveCaretGotoLineLastEnd(editor, operatorArguments.count0, operatorArguments.count1 - 1, allow).toMotion()
  }
}

private fun moveCaretGotoLineLastEnd(
  editor: FimEditor,
  rawCount: Int,
  line: Int,
  pastEnd: Boolean,
): Int {
  return injector.motion.moveCaretToLineEnd(
    editor,
    if (rawCount == 0) injector.engineEditorHelper.normalizeLine(editor, editor.lineCount() - 1) else line,
    pastEnd
  )
}
