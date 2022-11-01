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
package com.flop.idea.fim.action.change

import com.intellij.openapi.command.CommandProcessor
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.ij

class RepeatChangeAction : FimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_WRITABLE

  override fun execute(editor: FimEditor, context: ExecutionContext, cmd: Command, operatorArguments: OperatorArguments): Boolean {
    val state = editor.fimStateMachine
    val lastCommand = FimRepeater.lastChangeCommand

    if (lastCommand == null && Extension.lastExtensionHandler == null) return false

    // Save state
    val save = state.executingCommand
    val lastFTCmd = injector.motion.lastFTCmd
    val lastFTChar = injector.motion.lastFTChar
    val reg = injector.registerGroup.currentRegister
    val lastHandler = Extension.lastExtensionHandler
    val repeatHandler = FimRepeater.repeatHandler

    state.isDotRepeatInProgress = true

    // A fancy 'redo-register' feature
    // VIM-2643, :h redo-register
    if (FimRepeater.lastChangeRegister in '1'..'8') {
      FimRepeater.lastChangeRegister = FimRepeater.lastChangeRegister.inc()
    }

    injector.registerGroup.selectRegister(FimRepeater.lastChangeRegister)

    try {
      if (repeatHandler && lastHandler != null) {
        val processor = CommandProcessor.getInstance()
        processor.executeCommand(
          editor.ij.project,
          { lastHandler.execute(editor, context) },
          "Fim " + lastHandler.javaClass.simpleName,
          null
        )
      } else if (!repeatHandler && lastCommand != null) {
        if (cmd.rawCount > 0) {
          lastCommand.count = cmd.count
          val arg = lastCommand.argument
          if (arg != null) {
            val mot = arg.motion
            mot.count = 0
          }
        }
        state.setExecutingCommand(lastCommand)

        val arguments = operatorArguments.copy(count0 = lastCommand.rawCount)
        injector.actionExecutor.executeFimAction(editor, lastCommand.action, context, arguments)

        FimRepeater.saveLastChange(lastCommand)
      }
    } catch (ignored: Exception) {
    }

    state.isDotRepeatInProgress = false

    // Restore state
    if (save != null) state.setExecutingCommand(save)
    com.flop.idea.fim.FimPlugin.getMotion().setLastFTCmd(lastFTCmd, lastFTChar)
    if (lastHandler != null) Extension.lastExtensionHandler = lastHandler
    FimRepeater.repeatHandler = repeatHandler
    Extension.reset()
    com.flop.idea.fim.FimPlugin.getRegister().selectRegister(reg)
    return true
  }
}
