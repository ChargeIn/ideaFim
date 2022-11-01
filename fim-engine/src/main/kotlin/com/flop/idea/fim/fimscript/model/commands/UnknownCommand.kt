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
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.GoalCommand
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.InvalidCommandException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.Msg
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.commands.UnknownCommand.Constants.MAX_RECURSION

/**
 * any command with no parser rule. we assume that it is an alias
 */
data class UnknownCommand(val ranges: Ranges, val name: String, val argument: String) :
  Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.SELF_SYNCHRONIZED)

  private object Constants {
    const val MAX_RECURSION = 100
  }

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    return processPossiblyAliasCommand("$name $argument", editor, context, MAX_RECURSION)
  }

  private fun processPossiblyAliasCommand(name: String, editor: FimEditor, context: ExecutionContext, aliasCountdown: Int): ExecutionResult {
    if (injector.commandGroup.isAlias(name)) {
      if (aliasCountdown > 0) {
        val commandAlias = injector.commandGroup.getAliasCommand(name, 1)
        when (commandAlias) {
          is GoalCommand.Ex -> {
            if (commandAlias.command.isEmpty()) {
              val message = injector.messages.message(Msg.NOT_EX_CMD, name)
              throw InvalidCommandException(message, null)
            }
            val parsedCommand = injector.fimscriptParser.parseCommand(commandAlias.command) ?: throw ExException("E492: Not an editor command: ${commandAlias.command}")
            return if (parsedCommand is UnknownCommand) {
              processPossiblyAliasCommand(commandAlias.command, editor, context, aliasCountdown - 1)
            } else {
              parsedCommand.fimContext = this.fimContext
              parsedCommand.execute(editor, context)
              ExecutionResult.Success
            }
          }
          is GoalCommand.Call -> {
            commandAlias.handler.execute(name, ranges, editor, context)
            return ExecutionResult.Success
          }
        }
      } else {
        injector.messages.showStatusBarMessage(injector.messages.message("recursion.detected.maximum.alias.depth.reached"))
        injector.messages.indicateError()
        return ExecutionResult.Error
      }
    } else {
      throw ExException("E492: Not an editor command: $name")
    }
  }
}
