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

package com.flop.idea.fim.action.change.change

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.DuplicableOperatorAction
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.endOffsetInclusive

class FilterMotionAction : FimActionHandler.SingleExecution(), DuplicableOperatorAction {

  override val type: Command.Type = Command.Type.CHANGE

  override val argumentType: Argument.Type = Argument.Type.MOTION

  override val duplicateWith: Char = '!'

  override fun execute(editor: FimEditor, context: ExecutionContext, cmd: Command, operatorArguments: OperatorArguments): Boolean {
    val argument = cmd.argument ?: return false
    val range = injector.motion.getMotionRange(editor, editor.primaryCaret(), context, argument, operatorArguments)
      ?: return false

    val current = editor.currentCaret().getLogicalPosition()
    val start = editor.offsetToLogicalPosition(range.startOffset)
    val end = editor.offsetToLogicalPosition(range.endOffsetInclusive)
    if (current.line != start.line) {
      injector.motion.moveCaret(editor, editor.primaryCaret(), range.startOffset)
    }

    val count = if (start.line < end.line) end.line - start.line + 1 else 1

    injector.processGroup.startFilterCommand(editor, context, Argument.EMPTY_COMMAND.copy(rawCount = count))

    return true
  }
}
