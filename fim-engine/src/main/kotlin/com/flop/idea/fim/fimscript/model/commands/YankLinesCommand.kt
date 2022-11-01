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
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * see "h :yank"
 */
data class YankLinesCommand(val ranges: Ranges, var argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  @Throws(ExException::class)
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val argument = this.argument
    val registerGroup = injector.registerGroup
    val register = if (argument.isNotEmpty() && !argument[0].isDigit()) {
      this.argument = argument.substring(1)
      argument[0]
    } else {
      registerGroup.defaultRegister
    }

    if (!registerGroup.selectRegister(register)) return ExecutionResult.Error

    val starts = ArrayList<Int>(editor.nativeCarets().size)
    val ends = ArrayList<Int>(editor.nativeCarets().size)
    for (caret in editor.nativeCarets()) {
      val range = getTextRange(editor, caret, true)
      starts.add(range.startOffset)
      ends.add(range.endOffset)
    }

    return if (injector.yank.yankRange(
        editor,
        TextRange(starts.toIntArray(), ends.toIntArray()),
        SelectionType.LINE_WISE, false
      )
    ) ExecutionResult.Success else ExecutionResult.Error
  }
}
