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
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.put.PutData
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * see "h :put"
 */
data class PutLinesCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    if (editor.isOneLineMode()) return ExecutionResult.Error

    val registerGroup = injector.registerGroup
    val arg = argument
    if (arg.isNotEmpty()) {
      if (!registerGroup.selectRegister(arg[0]))
        return ExecutionResult.Error
    } else {
      registerGroup.selectRegister(registerGroup.defaultRegister)
    }

    val line = if (ranges.size() == 0) -1 else getLine(editor)
    val textData = registerGroup.lastRegister?.let {
      PutData.TextData(
        it.text ?: injector.parser.toKeyNotation(it.keys),
        SelectionType.LINE_WISE,
        it.transferableData
      )
    }
    val putData = PutData(
      textData,
      null,
      1,
      insertTextBeforeCaret = false,
      rawIndent = false,
      caretAfterInsertedText = false,
      putToLine = line
    )
    return if (injector.put.putText(editor, context, putData, operatorArguments = operatorArguments)) ExecutionResult.Success else ExecutionResult.Error
  }
}
