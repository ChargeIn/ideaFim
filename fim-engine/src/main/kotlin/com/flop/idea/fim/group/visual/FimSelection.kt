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

package com.flop.idea.fim.group.visual

import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.SelectionType.BLOCK_WISE
import com.flop.idea.fim.command.SelectionType.CHARACTER_WISE
import com.flop.idea.fim.command.SelectionType.LINE_WISE
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.Pointer
import com.flop.idea.fim.common.TextRange
import org.jetbrains.annotations.NonNls
import kotlin.math.max
import kotlin.math.min

/**
 * @author Alex Plate
 *
 * Interface for storing selection range.
 *
 * Type of selection is stored in [type]
 * [fimStart] and [fimEnd] - selection offsets in fim model. There values will be stored in '< and '> marks.
 *   Actually [fimStart] - initial caret position when visual mode entered and [fimEnd] - current caret position.
 *
 * This selection has direction. That means that by moving in left-up direction (e.g. `vbbbb`)
 *   [fimStart] will be greater then [fimEnd].
 *
 * All starts are included and ends are excluded
 */
sealed class FimSelection {
  abstract val type: SelectionType
  abstract val fimStart: Int
  abstract val fimEnd: Int
  protected abstract val editor: FimEditor

  abstract fun toFimTextRange(skipNewLineForLineMode: Boolean = false): TextRange

  abstract fun getNativeStartAndEnd(): Pair<Int, Int>

  companion object {
    fun create(fimStart: Int, fimEnd: Int, type: SelectionType, editor: FimEditor) = when (type) {
      CHARACTER_WISE -> {
        val nativeSelection = charToNativeSelection(editor, fimStart, fimEnd, FimStateMachine.Mode.VISUAL)
        FimCharacterSelection(fimStart, fimEnd, nativeSelection.first, nativeSelection.second, editor)
      }
      LINE_WISE -> {
        val nativeSelection = lineToNativeSelection(editor, fimStart, fimEnd)
        FimLineSelection(fimStart, fimEnd, nativeSelection.first, nativeSelection.second, editor)
      }
      BLOCK_WISE -> FimBlockSelection(fimStart, fimEnd, editor, false)
    }
  }

  @NonNls
  override fun toString(): String {
    val startLogPosition = editor.offsetToLogicalPosition(fimStart)
    val endLogPosition = editor.offsetToLogicalPosition(fimEnd)
    return "Selection [$type]: fim start[offset: $fimStart : col ${startLogPosition.column} line ${startLogPosition.line}]" +
      " fim end[offset: $fimEnd : col ${endLogPosition.column} line ${endLogPosition.line}]"
  }
}

/**
 * Interface for storing simple selection range.
 *   Simple means that this selection can be represented only by start and end values.
 *   There selections in fim are character- and linewise selections.
 *
 *  [nativeStart] and [nativeEnd] are the offsets of native selection
 *
 * [fimStart] and [fimEnd] - selection offsets in fim model. There values will be stored in '< and '> marks.
 *   There values can differ from [nativeStart] and [nativeEnd] in case of linewise selection because [fimStart] - initial caret
 *   position when visual mode entered and [fimEnd] - current caret position.
 *
 * This selection has direction. That means that by moving in left-up direction (e.g. `vbbbb`)
 *   [nativeStart] will be greater than [nativeEnd].
 * If you need normalized [nativeStart] and [nativeEnd] (start always less than end) you
 *   can use [normNativeStart] and [normNativeEnd]
 *
 * All starts are included and ends are excluded
 */
sealed class FimSimpleSelection : FimSelection() {
  abstract val nativeStart: Int
  abstract val nativeEnd: Int
  abstract val normNativeStart: Int
  abstract val normNativeEnd: Int

  override fun getNativeStartAndEnd() = normNativeStart to normNativeEnd

  companion object {
    /**
     * Create character- and linewise selection if native selection is already known. Doesn't work for block selection
     */
    fun createWithNative(
      fimStart: Int,
      fimEnd: Int,
      nativeStart: Int,
      nativeEnd: Int,
      type: SelectionType,
      editor: FimEditor,
    ) = when (type) {
      CHARACTER_WISE -> FimCharacterSelection(fimStart, fimEnd, nativeStart, nativeEnd, editor)
      LINE_WISE -> FimLineSelection(fimStart, fimEnd, nativeStart, nativeEnd, editor)
      BLOCK_WISE -> error("This method works only for line and character selection")
    }
  }
}

class FimCharacterSelection(
  override val fimStart: Int,
  override val fimEnd: Int,
  override val nativeStart: Int,
  override val nativeEnd: Int,
  override val editor: FimEditor,
) : FimSimpleSelection() {
  override val normNativeStart = min(nativeStart, nativeEnd)
  override val normNativeEnd = max(nativeStart, nativeEnd)
  override val type: SelectionType = CHARACTER_WISE

  override fun toFimTextRange(skipNewLineForLineMode: Boolean) = TextRange(normNativeStart, normNativeEnd)
}

class FimLineSelection(
  override val fimStart: Int,
  override val fimEnd: Int,
  override val nativeStart: Int,
  override val nativeEnd: Int,
  override val editor: FimEditor,
) : FimSimpleSelection() {
  override val normNativeStart = min(nativeStart, nativeEnd)
  override val normNativeEnd = max(nativeStart, nativeEnd)
  override val type = LINE_WISE

  override fun toFimTextRange(skipNewLineForLineMode: Boolean) =
    if (skipNewLineForLineMode && editor.fileSize() >= normNativeEnd && normNativeEnd > 0 && editor.charAt(Pointer(normNativeEnd - 1)) == '\n') {
      TextRange(normNativeStart, (normNativeEnd - 1).coerceAtLeast(0))
    } else {
      TextRange(normNativeStart, normNativeEnd)
    }
}

class FimBlockSelection(
  override val fimStart: Int,
  override val fimEnd: Int,
  override val editor: FimEditor,
  private val toLineEnd: Boolean,
) : FimSelection() {
  override fun getNativeStartAndEnd() = blockToNativeSelection(editor, fimStart, fimEnd, FimStateMachine.Mode.VISUAL).let {
    editor.logicalPositionToOffset(it.first) to editor.logicalPositionToOffset(it.second)
  }

  override val type = BLOCK_WISE

  override fun toFimTextRange(skipNewLineForLineMode: Boolean): TextRange {
    val starts = mutableListOf<Int>()
    val ends = mutableListOf<Int>()
    forEachLine { start, end ->
      starts += start
      ends += end
    }
    return TextRange(starts.toIntArray(), ends.toIntArray()).also { it.normalize(editor.fileSize().toInt()) }
  }

  private fun forEachLine(action: (start: Int, end: Int) -> Unit) {
    val (logicalStart, logicalEnd) = blockToNativeSelection(editor, fimStart, fimEnd, FimStateMachine.Mode.VISUAL)
    val lineRange =
      if (logicalStart.line > logicalEnd.line) logicalEnd.line..logicalStart.line else logicalStart.line..logicalEnd.line
    lineRange.map { line ->
      val start = editor.logicalPositionToOffset(FimLogicalPosition(line, logicalStart.column))
      val end = if (toLineEnd) {
        editor.getLineEndOffset(line, true)
      } else {
        editor.logicalPositionToOffset(FimLogicalPosition(line, logicalEnd.column))
      }
      action(start, end)
    }
  }
}
