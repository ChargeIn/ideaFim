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
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.LineRange
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.inBlockSubMode
import com.flop.idea.fim.fimscript.model.ExecutionResult
import java.util.*

/**
 * @author Alex Selesse
 * see "h :sort"
 */
data class SortCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.WRITABLE)

  @Throws(ExException::class)
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val arg = argument
    val nonEmptyArg = arg.trim().isNotEmpty()

    val reverse = nonEmptyArg && "!" in arg
    val ignoreCase = nonEmptyArg && "i" in arg
    val number = nonEmptyArg && "n" in arg

    val lineComparator = LineComparator(ignoreCase, number, reverse)
    if (editor.inBlockSubMode) {
      val primaryCaret = editor.primaryCaret()
      val range = getSortLineRange(editor, primaryCaret)
      val worked = injector.changeGroup.sortRange(editor, range, lineComparator)
      primaryCaret.moveToInlayAwareOffset(
        injector.motion.moveCaretToLineStartSkipLeading(editor, range.startLine)
      )
      return if (worked) ExecutionResult.Success else ExecutionResult.Error
    }

    var worked = true
    for (caret in editor.nativeCarets()) {
      val range = getSortLineRange(editor, caret)
      if (!injector.changeGroup.sortRange(editor, range, lineComparator)) {
        worked = false
      }
      caret.moveToInlayAwareOffset(injector.motion.moveCaretToLineStartSkipLeading(editor, range.startLine))
    }

    return if (worked) ExecutionResult.Success else ExecutionResult.Error
  }

  private fun getSortLineRange(editor: FimEditor, caret: FimCaret): LineRange {
    val range = getLineRange(editor, caret)

    // Something like "30,20sort" gets converted to "20,30sort"
    val normalizedRange = if (range.endLine < range.startLine) LineRange(range.endLine, range.startLine) else range

    // If we don't have a range, we either have "sort", a selection, or a block
    if (normalizedRange.endLine - normalizedRange.startLine == 0) {
      // If we have a selection.
      val selectionModel = editor.getSelectionModel()
      return if (selectionModel.hasSelection()) {
        val start = selectionModel.selectionStart
        val end = selectionModel.selectionEnd

        val startLine = editor.offsetToLogicalPosition(start).line
        val endLine = editor.offsetToLogicalPosition(end).line

        LineRange(startLine, endLine)
      } else {
        LineRange(0, editor.lineCount() - 1)
      } // If we have a generic selection, i.e. "sort" entire document
    }

    return normalizedRange
  }

  private class LineComparator(
    private val myIgnoreCase: Boolean,
    private val myNumber: Boolean,
    private val myReverse: Boolean,
  ) : Comparator<String> {

    override fun compare(o1: String, o2: String): Int {
      var o1ToCompare = o1
      var o2ToCompare = o2
      if (myReverse) {
        val tmp = o2ToCompare
        o2ToCompare = o1ToCompare
        o1ToCompare = tmp
      }
      if (myIgnoreCase) {
        o1ToCompare = o1ToCompare.uppercase(Locale.getDefault())
        o2ToCompare = o2ToCompare.uppercase(Locale.getDefault())
      }
      return if (myNumber) {
        // About natural sort order - http://www.codinghorror.com/blog/2007/12/sorting-for-humans-natural-sort-order.html
        val n1 = injector.searchGroup.findDecimalNumber(o1ToCompare)
        val n2 = injector.searchGroup.findDecimalNumber(o2ToCompare)
        if (n1 == null) {
          if (n2 == null) 0 else -1
        } else {
          if (n2 == null) 1 else n1.compareTo(n2)
        }
      } else o1ToCompare.compareTo(o2ToCompare)
    }
  }
}
