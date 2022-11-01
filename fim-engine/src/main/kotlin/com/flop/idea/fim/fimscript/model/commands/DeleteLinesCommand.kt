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
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * see "h :delete"
 */
data class DeleteLinesCommand(val ranges: Ranges, var argument: String) : Command.ForEachCaret(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.WRITABLE)

  override fun processCommand(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    operatorArguments: OperatorArguments
  ): ExecutionResult {
    val argument = this.argument
    val register = if (argument.isNotEmpty() && !argument[0].isDigit()) {
      this.argument = argument.substring(1)
      argument[0]
    } else {
      injector.registerGroup.defaultRegister
    }

    if (!injector.registerGroup.selectRegister(register)) return ExecutionResult.Error

    val textRange = getTextRange(editor, caret, true)
    return if (injector.changeGroup
      .deleteRange(editor, caret, textRange, SelectionType.LINE_WISE, false, operatorArguments)
    ) ExecutionResult.Success
    else ExecutionResult.Error
  }
}
