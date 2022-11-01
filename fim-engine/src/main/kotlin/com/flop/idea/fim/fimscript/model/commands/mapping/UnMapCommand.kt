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

package com.flop.idea.fim.fimscript.model.commands.mapping

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.commands.Command

data class UnMapCommand(val ranges: Ranges, val argument: String, val cmd: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_REQUIRED, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    return if (executeCommand()) ExecutionResult.Success else ExecutionResult.Error
  }

  private fun executeCommand(): Boolean {
    val commandInfo = COMMAND_INFOS.find { cmd.startsWith(it.prefix) } ?: return false

    if (argument.isEmpty()) return false

    val parsedKeys = injector.parser.parseKeys(argument.trimStart())

    injector.keyGroup.removeKeyMapping(commandInfo.mappingModes, parsedKeys)

    return true
  }

  companion object {
    private val COMMAND_INFOS = arrayOf(
      CommandInfo("unm", "ap", MappingMode.NVO, false),
      CommandInfo("nun", "map", MappingMode.N, false),
      CommandInfo("vu", "nmap", MappingMode.V, false),
      CommandInfo("xu", "nmap", MappingMode.X, false),
      CommandInfo("sunm", "ap", MappingMode.S, false),
      CommandInfo("ou", "nmap", MappingMode.O, false),
      CommandInfo("iu", "nmap", MappingMode.I, false),
      CommandInfo("cu", "nmap", MappingMode.C, false)
    )
  }
}
