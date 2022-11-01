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
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.EngineStringHelper
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * see "h :marks"
 */
data class MarksCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {

    // Yeah, lower case. Fim uses lower case here, but Title Case in :registers. Go figure.
    val res = injector.markGroup.getMarks(editor)
      .filter { argument.isEmpty() || argument.contains(it.key) }
      .joinToString("\n", prefix = "mark line  col file/text\n") { mark ->

        // Lines are 1 based, columns zero based. See :help :marks
        val line = (mark.logicalLine + 1).toString().padStart(5)
        val column = mark.col.toString().padStart(3)
        val vf = editor.getVirtualFile()
        val text = if (vf != null && vf.path == mark.filename) {
          val lineText = editor.getLineText(mark.logicalLine).trim().take(200)
          EngineStringHelper.toPrintableCharacters(injector.parser.stringToKeys(lineText)).take(200)
        } else {
          mark.filename
        }

        " ${mark.key}  $line  $column $text"
      }

    injector.exOutputPanel.getPanel(editor).output(res)

    return ExecutionResult.Success
  }
}
