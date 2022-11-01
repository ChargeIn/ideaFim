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

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExOutputModel
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.fimscript.model.ExecutionResult
import java.util.*

/**
 * @author smartbomb
 */
data class ActionListCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val lineSeparator = "\n"
    val searchPattern = argument.trim().lowercase(Locale.getDefault()).split("*")
    val actionManager = ActionManager.getInstance()

    val actions = actionManager.getActionIdList("")
      .sortedWith(String.CASE_INSENSITIVE_ORDER)
      .map { actionName ->
        val shortcuts = actionManager.getAction(actionName).shortcutSet.shortcuts.joinToString(" ") {
          if (it is KeyboardShortcut) injector.parser.toKeyNotation(it.firstKeyStroke) else it.toString()
        }
        if (shortcuts.isBlank()) actionName else "${actionName.padEnd(50)} $shortcuts"
      }
      .filter { line -> searchPattern.all { it in line.lowercase(Locale.getDefault()) } }
      .joinToString(lineSeparator)

    ExOutputModel.getInstance(editor.ij).output(MessageHelper.message("ex.show.all.actions.0.1", lineSeparator, actions))
    return ExecutionResult.Success
  }
}
