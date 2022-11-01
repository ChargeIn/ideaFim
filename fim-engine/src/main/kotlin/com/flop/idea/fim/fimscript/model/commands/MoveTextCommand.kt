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
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.InvalidRangeException
import com.flop.idea.fim.ex.ranges.LineRange
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.Msg
import com.flop.idea.fim.put.PutData
import com.flop.idea.fim.fimscript.model.ExecutionResult
import kotlin.math.min

/**
 * see "h :move"
 */
data class MoveTextCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_REQUIRED, Access.WRITABLE)

  @Throws(ExException::class)
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val carets = editor.sortedCarets()
    val caretCount = editor.nativeCarets().size

    val texts = ArrayList<String>(caretCount)
    val ranges = ArrayList<TextRange>(caretCount)
    var line = editor.fileSize().toInt()
    val goToLineCommand = injector.fimscriptParser.parseCommand(argument) ?: throw ExException("E16: Invalid range")

    var lastRange: TextRange? = null
    for (caret in carets) {
      val range = getTextRange(editor, caret, false)
      val lineRange = getLineRange(editor, caret)

      line = min(line, normalizeLine(editor, caret, goToLineCommand, lineRange))
      texts.add(injector.engineEditorHelper.getText(editor, range))

      if (lastRange == null || lastRange.startOffset != range.startOffset && lastRange.endOffset != range.endOffset) {
        ranges.add(range)
        lastRange = range
      }
    }

    ranges.forEach { editor.deleteString(it) }

    for (i in 0 until caretCount) {
      val caret = carets[i]
      val text = texts[i]

      val textData = PutData.TextData(text, SelectionType.LINE_WISE, emptyList())
      val putData = PutData(
        textData,
        null,
        1,
        insertTextBeforeCaret = false,
        rawIndent = true,
        caretAfterInsertedText = false,
        putToLine = line
      )
      injector.put.putTextForCaret(editor, caret, context, putData)
    }

    return ExecutionResult.Success
  }

  @Throws
  private fun normalizeLine(
    editor: FimEditor,
    caret: FimCaret,
    command: Command,
    lineRange: LineRange,
  ): Int {
    var line = command.commandRanges.getFirstLine(editor, caret)
    val adj = lineRange.endLine - lineRange.startLine + 1
    if (line >= lineRange.endLine)
      line -= adj
    else if (line >= lineRange.startLine) throw InvalidRangeException(injector.messages.message(Msg.e_backrange))

    return line
  }
}
