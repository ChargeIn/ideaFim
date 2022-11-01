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

package com.flop.idea.fim.fimscript.model.commands

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import java.lang.Integer.min

/**
 * see "h :[range]"
 */
data class GoToLineCommand(val ranges: Ranges) :
  Command.ForEachCaret(ranges) {

  override val argFlags = flags(RangeFlag.RANGE_REQUIRED, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    operatorArguments: OperatorArguments,
  ): ExecutionResult {
    val line = min(this.getLine(editor, caret), editor.lineCount() - 1)

    if (line >= 0) {
      val offset = injector.motion.moveCaretToLineWithStartOfLineOption(editor, line, caret)
      injector.motion.moveCaret(editor, caret, offset)
      return ExecutionResult.Success
    }

    injector.motion.moveCaret(editor, caret, 0)
    return ExecutionResult.Error
  }
}
