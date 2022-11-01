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
package com.flop.idea.fim.action.macro

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.register.RegisterConstants.LAST_COMMAND_REGISTER

class PlaybackRegisterAction : FimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override val argumentType: Argument.Type = Argument.Type.CHARACTER

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val argument = cmd.argument ?: return false
    val reg = argument.character
    val application = injector.application
    val res = arrayOf(false)
    when {
      reg == LAST_COMMAND_REGISTER || (reg == '@' && injector.macro.lastRegister == LAST_COMMAND_REGISTER) -> { // No write action
        try {
          var i = 0
          while (i < cmd.count) {
            res[0] = injector.fimscriptExecutor.executeLastCommand(editor, context)
            if (!res[0]) {
              break
            }
            i += 1
          }
          if (reg != '@') { // @ is not a register itself, it just tells fim to use the last register
            injector.macro.lastRegister = reg
          }
        } catch (e: ExException) {
          res[0] = false
        }
      }

      reg == '@' -> {
        application.runWriteAction {
          res[0] = injector.macro.playbackLastRegister(editor, context, cmd.count)
        }
      }

      else -> {
        application.runWriteAction {
          res[0] = injector.macro.playbackRegister(editor, context, reg, cmd.count)
        }
      }
    }
    return res[0]
  }
}
