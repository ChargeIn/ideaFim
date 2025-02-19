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
package com.flop.idea.fim.ex.ranges

import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.common.TextRange
import org.jetbrains.annotations.NonNls
import kotlin.math.min

/**
 * Handles the set of range values entered as part of an Ex command.
 */
class Ranges {
  /** Adds a range to the list */
  fun addRange(range: Array<Range>) {
    ranges.addAll(range)
  }

  /** Gets the number of ranges in the list */
  fun size(): Int = ranges.size

  /**
   * Sets the default line to be used by this range if no range was actually given by the user. -1 is used to
   * mean the current line.
   *
   * @param line The line or -1 for current line
   */
  fun setDefaultLine(line: Int) {
    defaultLine = line
  }

  /**
   * Gets the line of the last range specified in the range list
   *
   * @param editor  The editor to get the line for
   * @return The line number represented by the range
   */
  fun getLine(editor: FimEditor): Int {
    processRange(editor)
    return endLine
  }

  fun getLine(editor: FimEditor, caret: FimCaret): Int {
    processRange(editor, caret)
    return endLine
  }

  fun getFirstLine(editor: FimEditor, caret: FimCaret): Int {
    processRange(editor, caret)
    return startLine
  }

  /**
   * Gets the count for an Ex command. This is either an explicit count enter at the end of the command or the
   * end of the specified range.
   *
   * @param editor  The editor to get the count for
   * @param count   The count given at the end of the command or -1 if no such count (use end line)
   * @return count if count != -1, else return end line of range
   */
  fun getCount(editor: FimEditor, count: Int): Int = if (count == -1) getLine(editor) else count

  fun getCount(editor: FimEditor, caret: FimCaret, count: Int): Int {
    return if (count == -1) getLine(editor, caret) else count
  }

  /**
   * Gets the line range represented by this range. If a count is given, the range is the range end line through
   * count-1 lines. If no count is given (-1), the range is the range given by the user.
   *
   * @param editor  The editor to get the range for
   * @param count   The count given at the end of the command or -1 if no such count
   * @return The line range
   */
  fun getLineRange(editor: FimEditor, count: Int): LineRange {
    processRange(editor)
    val end: Int
    val start: Int
    if (count == -1) {
      end = endLine
      start = startLine
    } else {
      start = endLine
      end = start + count - 1
    }
    return LineRange(start, end)
  }

  fun getLineRange(editor: FimEditor, caret: FimCaret, count: Int): LineRange {
    processRange(editor, caret)
    return if (count == -1) LineRange(startLine, endLine) else LineRange(endLine, endLine + count - 1)
  }

  /**
   * Gets the text range represented by this range. If a count is given, the range is the range end line through
   * count-1 lines. If no count is given (-1), the range is the range given by the user. The text range is based
   * on the line range but this is character based from the start of the first line to the end of the last line.
   *
   * @param editor  The editor to get the range for
   * @param count   The count given at the end of the command or -1 if no such count
   * @return The text range
   */
  fun getTextRange(editor: FimEditor, count: Int): TextRange {
    val lr = getLineRange(editor, count)
    val start = injector.engineEditorHelper.getLineStartOffset(editor, lr.startLine)
    val end = injector.engineEditorHelper.getLineEndOffset(editor, lr.endLine, true) + 1
    return TextRange(start, min(end, editor.fileSize().toInt()))
  }

  fun getTextRange(editor: FimEditor, caret: FimCaret, count: Int): TextRange {
    val lineRange = getLineRange(editor, caret, count)
    val start = injector.engineEditorHelper.getLineStartOffset(editor, lineRange.startLine)
    val end = injector.engineEditorHelper.getLineEndOffset(editor, lineRange.endLine, true) + 1
    return TextRange(start, min(end, editor.fileSize().toInt()))
  }

  /**
   * Processes the list of ranges and calculates the start and end lines of the range
   *
   * @param editor  The editor to get the lines for
   */
  private fun processRange(editor: FimEditor) {
    // Already done
    if (done) return
    // Start with the range being the current line
    startLine = if (defaultLine == -1) editor.currentCaret().getLogicalPosition().line else defaultLine
    endLine = startLine
    var lastZero = false
    // Now process each range, moving the cursor if appropriate
    for (range in ranges) {
      startLine = endLine
      endLine = range.getLine(editor, lastZero)
      if (range.isMove) {
        injector.motion.moveCaret(
          editor, editor.primaryCaret(),
          injector.motion.moveCaretToLineWithSameColumn(editor, endLine, editor.primaryCaret())
        )
      }
      // Did that last range represent the start of the file?
      lastZero = endLine < 0
      count++
    }
    // If only one range given, make the start and end the same
    if (count == 1) {
      startLine = endLine
    }
    done = true
  }

  private fun processRange(editor: FimEditor, caret: FimCaret) {
    startLine = if (defaultLine == -1) caret.getLogicalPosition().line else defaultLine
    endLine = startLine
    var lastZero = false
    for (range in ranges) {
      startLine = endLine
      endLine = range.getLine(editor, caret, lastZero)
      if (range.isMove) injector.motion.moveCaret(
        editor,
        caret,
        injector.motion.moveCaretToLineWithSameColumn(editor, endLine, editor.primaryCaret())
      )
      lastZero = endLine < 0
      ++count
    }
    if (count == 1) startLine = endLine
    count = 0
  }

  @NonNls
  override fun toString(): String = "Ranges[ranges=$ranges]"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Ranges) return false

    if (startLine != other.startLine) return false
    if (endLine != other.endLine) return false
    if (count != other.count) return false
    if (defaultLine != other.defaultLine) return false
    if (done != other.done) return false
    if (ranges != other.ranges) return false

    return true
  }

  override fun hashCode(): Int {
    var result = startLine
    result = 31 * result + endLine
    result = 31 * result + count
    result = 31 * result + defaultLine
    result = 31 * result + done.hashCode()
    result = 31 * result + ranges.hashCode()
    return result
  }

  private var startLine = 0
  private var endLine = 0
  private var count = 0
  private var defaultLine = -1
  private var done = false
  var ranges: MutableList<Range> = mutableListOf()
}
