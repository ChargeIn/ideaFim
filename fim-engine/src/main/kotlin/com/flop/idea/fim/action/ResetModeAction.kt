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
package com.flop.idea.fim.action

import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.mode

class ResetModeAction : FimActionHandler.ConditionalMulticaret() {
  private lateinit var modeBeforeReset: FimStateMachine.Mode
  override val type: Command.Type = Command.Type.OTHER_WRITABLE
  override fun runAsMulticaret(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    modeBeforeReset = editor.mode
    KeyHandler.getInstance().fullReset(editor)
    return true
  }

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (modeBeforeReset == FimStateMachine.Mode.INSERT) {
      val position = injector.motion.getOffsetOfHorizontalMotion(editor, caret, -1, false)
      caret.moveToOffset(position)
    }
    return true
  }

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    error("This method should not be used")
  }
}
