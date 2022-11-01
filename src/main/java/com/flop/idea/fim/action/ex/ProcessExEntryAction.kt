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
package com.flop.idea.fim.action.ex

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.action.ComplicatedKeysAction
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.FimActionHandler
import java.util.*
import javax.swing.KeyStroke

/**
 * Called by KeyHandler to process the contents of the ex entry panel
 *
 * The mapping for this action means that the ex command is executed as a write action
 */
class ProcessExEntryAction : FimActionHandler.SingleExecution(), ComplicatedKeysAction {
  override val keyStrokesSet: Set<List<KeyStroke>> =
    parseKeysSet("<CR>", "<C-M>", 0x0a.toChar().toString(), 0x0d.toChar().toString())

  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override val flags: EnumSet<CommandFlags> = EnumSet.of(CommandFlags.FLAG_COMPLETE_EX)

  override fun execute(editor: FimEditor, context: ExecutionContext, cmd: Command, operatorArguments: OperatorArguments): Boolean {
    return com.flop.idea.fim.FimPlugin.getProcess().processExEntry(editor, context)
  }
}
