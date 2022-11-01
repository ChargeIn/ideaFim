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

import com.flop.idea.fim.action.motion.updown.MotionDownLess1FirstNonSpaceAction
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.CommandFlags.FLAG_MULTIKEY_UNDO
import com.flop.idea.fim.command.CommandFlags.FLAG_NO_REPEAT_INSERT
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.ChangeEditorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

class ChangeLineAction : ChangeEditorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.CHANGE

  override val flags: EnumSet<CommandFlags> = enumSetOf(FLAG_NO_REPEAT_INSERT, FLAG_MULTIKEY_UNDO)

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    // `S` command is a synonym of `cc`
    val motion = MotionDownLess1FirstNonSpaceAction()
    val command = Command(1, motion, motion.type, motion.flags)
    return injector.changeGroup.changeMotion(
      editor,
      caret,
      context,
      Argument(command),
      operatorArguments
    )
  }
}
