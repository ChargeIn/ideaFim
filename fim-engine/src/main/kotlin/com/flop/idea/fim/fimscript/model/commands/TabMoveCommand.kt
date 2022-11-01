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
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import java.lang.NumberFormatException

/*
 * see "h :tabmove"
 */
data class TabMoveCommand(val ranges: Ranges, var argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    if (ranges.size() != 0) {
      throw ExException("Range form of tabmove command is not supported. Please use the argument form")
    }

    val tabService = injector.tabService
    val tabCount = tabService.getTabCount(context)
    val currentIndex = tabService.getCurrentTabIndex(context)
    val index: Int

    try {
      argument = argument.trim()
      if (argument == "+" || argument == "-") {
        argument += "1"
      }
      index = if (argument.startsWith("+")) {
        val number = Integer.parseInt(argument.substring(1))
        if (number == 0) {
          throw ExException("E474: Invalid argument")
        }
        currentIndex + number
      } else if (argument.startsWith("-")) {
        val number = Integer.parseInt(argument.substring(1))
        if (number == 0) {
          throw ExException("E474: Invalid argument")
        }
        currentIndex - number
      } else if (argument == "$" || argument.isBlank()) {
        tabCount - 1
      } else {
        var number = Integer.parseInt(argument)

        // it's strange, but it is the way Fim works
        if (number > currentIndex) number -= 1
        number
      }
    } catch (e: NumberFormatException) {
      throw ExException("E474: Invalid argument")
    }

    if (index < 0 || index >= tabCount) {
      throw ExException("E474: Invalid argument")
    }
    tabService.moveCurrentTabToIndex(index, context)
    return ExecutionResult.Success
  }
}
