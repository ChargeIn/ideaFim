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

package com.flop.idea.fim.api

import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.EditorLine
import com.flop.idea.fim.common.LiveRange
import com.flop.idea.fim.common.Offset
import com.flop.idea.fim.common.OperatedRange
import com.flop.idea.fim.common.Pointer
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.common.FimRange
import com.flop.idea.fim.common.offset
import com.flop.idea.fim.common.pointer
import java.util.*

/**
 * Every line in [FimEditor] ends with a new line TODO <- this is probably not true already
 *
 * # New line and line count
 *
 * In fim **every** line always ends with a new line character. If you'll open a file that doesn't end with
 *   a new line and save it without a modification, the new line character will be automatically added.
 * This is not how the most editors work. The file and the line may end without a new character symbol.
 * This affects a [lineCount] function. A three-lines file in fim contains four lines in other editors.
 * Also this complicated deletion logic.
 *
 * At the moment, we consider a what-user-sees approach. A file with two lines where every line ends with
 *   a new line character, is considered as a three-lines file because it's represented in the editor like this.
 *
 * TODO: We should understand what logic changes if we use a two or three lines editors.
 *
 * ---
 * # Offset and range deletion
 *
 * [Offset] is a position between characters.
 * [delete] method, that works on offset-offset, deltes the character *between* two offsets. That means that:
 * - It's possible and simply applicable when the start offset is larger than end offset
 * - Inclusive/Exclusive words are not applicable to such operation. So we don't define the operation
 *      like "... with end offset exclusive". However, technically, the "right" or the "larger" offset is exclusive.
 *
 * ## Other decisions review:
 * - End offset exclusive:
 *   This is a classic approach, that is used in most APIs, however, it's inconvenient for the case where the
 *     start offset is larger than end offset (direction switching) because for such offset switching it turns that
 *     start offset is exclusive and end offset is inclusive.
 * - End offset inclusive:
 *   This approach is convenient for direction switching, however, makes the situation that empty range cannot be
 *     specified. E.g. range 1:1 would delete the character under `1`, however it looks like that nothing should be
 *     deleted.
 *  Also, during the development it turned out that using such appriach causes a lot of `+1` and `-1` operations that
 *     seem to be redundant.
 *
 * ---
 * # Offset and pointer
 *
 * It seems line it would be helpful to split "string offset" into [Pointer] and [Offset] where [Pointer] referrers
 *   to a concrete existing character and [Offset] referrers to an in-between position.
 * Apart from semantic improvement (methods like `insertAt(Offset)`, `deleteAt(Pointer)` seem to be more obvious),
 *   looks like it may fix some concrete issues where we pass one type as a parameter, which is a different type.
 *   For example, let's delete a first character on the line. For the classic code it would look like
 *   ```
 *   fun lineStart(line: Int): Int
 *   fun deleteAt(offset: Int)
 *
 *   val lineStart = data.lineStart(x)
 *   data.deleteAt(lineStart)
 *   ```
 *   This code compiles and looks fine. However, with [Offset] and [Pointer] approach it would fail
 *   ```
 *   fun lineStart(line: Int): Offset // <- return the removed position
 *   fun deleteAt(offset: Pointer)
 *
 *   val lineStart: Offset = data.lineStart(x)
 *   data.deleteAt(lineStart) // ERROR: incorrect argument type
 *   ```
 *   So, we have to convert the [Offset] to [Pointer] somehow and during the conversion we may observe the problem
 *   that the line may contain no characters at all (at the file end). So the code is either semantically incorrect,
 *   or we should convert an [Offset] to [Pointer] keeping the fact that [Offset] is not exactly the [Pointer].
 *   ```
 *   ...
 *   fun Offset.toPointer(forData: String): Pointer? {
 *     return if (this < forData.length) Pointer(this) else null
 *   }
 *
 *   val lineFirstCharacter: Pointer = data.lineStart(x).toPointer(data)
 *   if (lineFirstCharacter != null) {
 *     data.deleteAt(lineFirstCharacter)
 *   }
 *   ```
 *
 * ---
 * # Lines
 *
 * We use two types of line reference: Offset and pointer. Offset referrers to a between-lines position.
 *   Pointer referrers to the concrete line.
 *
 * TODO We should check if we can keep the same FimEditor instance between edits.
 *   For example, can we store local options right in the editor implementation?
 */
interface FimEditor {

  val lfMakesNewLine: Boolean
  var fimChangeActionSwitchMode: FimStateMachine.Mode?
  var fimKeepingVisualOperatorAction: Boolean

  fun deleteDryRun(range: FimRange): OperatedRange?
  fun fileSize(): Long
  fun text(): CharSequence

  /**
   * Fim has always at least one line. When we need to understand that there are no lines, it has a flag "ML_EMPTY"
   *   which indicated that the buffer is empty. However, the line count is still 1.
   *
   * The variable for line count is named `ml_line_count` in `memline` structure. There is a single spot where
   *   `0` is assigned to this variable (at the end of `buf_freeall` function), however I'm not sure that this affects
   *   the opened buffer.
   * Another thing that I don't understand is that I don't see where this variable is updated. There is a small chance
   *   that this variable doesn't present the line count, so I may be wrong and line count can return zero.
   * I've explored this question by looking at the implementation of ctrl-g command in normal mode.
   */
  fun lineCount(): Int
  fun nativeLineCount(): Int

  fun getLineRange(line: EditorLine.Pointer): Pair<Offset, Offset>
  fun charAt(offset: Pointer): Char
  fun carets(): List<FimCaret>
  fun sortedCarets(): List<FimCaret> = carets().sortedByOffset()
  fun nativeCarets(): List<FimCaret>
  fun sortedNativeCarets(): List<FimCaret> = nativeCarets().sortedByOffset()

  private fun List<FimCaret>.sortedByOffset(): List<FimCaret> {
    return this.sortedWith(compareBy { it.offset.point }).reversed()
  }
  /**
   * This method should perform caret merging after the operations. This is similar to IJ runForEachCaret
   * TODO review
   */

  fun forEachCaret(action: (FimCaret) -> Unit)
  fun forEachCaret(action: (FimCaret) -> Unit, reverse: Boolean = false)
  fun forEachNativeCaret(action: (FimCaret) -> Unit)
  fun forEachNativeCaret(action: (FimCaret) -> Unit, reverse: Boolean = false)

  // --------------------------------------------------------------------

  /**
   * Do we really need this?
   * TODO
   */
  fun primaryCaret(): FimCaret
  fun currentCaret(): FimCaret

  fun charsSequence(): CharSequence

  fun isWritable(): Boolean
  fun isDocumentWritable(): Boolean
  fun isOneLineMode(): Boolean

  /**
   * Function for refactoring, get rid of it
   */
  fun search(
    pair: Pair<Offset, Offset>,
    editor: FimEditor,
    shiftType: LineDeleteShift,
  ): Pair<Pair<Offset, Offset>, LineDeleteShift>?

  fun updateCaretsVisualAttributes()
  fun updateCaretsVisualPosition()

  fun lineEndForOffset(offset: Int): Int
  fun lineStartForOffset(offset: Int): Int

  fun offsetToLogicalPosition(offset: Int): FimLogicalPosition
  fun logicalPositionToOffset(position: FimLogicalPosition): Int

  fun offsetToVisualPosition(offset: Int): FimVisualPosition
  fun visualPositionToOffset(position: FimVisualPosition): Offset

  fun getVirtualFile(): VirtualFile?
  fun deleteString(range: TextRange)
  fun getText(range: TextRange): String

  fun getLineText(line: Int): String
  fun lineLength(line: Int): Int

  fun getSelectionModel(): FimSelectionModel

  fun removeCaret(caret: FimCaret)
  fun removeSecondaryCarets()
  fun fimSetSystemBlockSelectionSilently(start: FimLogicalPosition, end: FimLogicalPosition)

  fun getLineStartOffset(line: Int): Int
  fun getLineEndOffset(line: Int, allowEnd: Boolean): Int
  fun getLineEndOffset(line: Int): Int
  fun getLineEndForOffset(offset: Int): Int

  fun addCaretListener(listener: FimCaretListener)
  fun removeCaretListener(listener: FimCaretListener)

  fun isDisposed(): Boolean

  fun removeSelection()

  fun getPath(): String?
  fun extractProtocol(): String?

  fun exitInsertMode(context: ExecutionContext, operatorArguments: OperatorArguments)
  fun exitSelectModeNative(adjustCaret: Boolean)
  fun exitVisualModeNative()

  var fimLastSelectionType: SelectionType?

  fun isTemplateActive(): Boolean

  fun startGuardedBlockChecking()
  fun stopGuardedBlockChecking()

  fun hasUnsavedChanges(): Boolean

  fun createLiveMarker(start: Offset, end: Offset): LiveRange
  var insertMode: Boolean

  val document: FimDocument
}

interface MutableFimEditor : FimEditor {
  /**
   * Returns actually deleted range and the according text, if any.
   *
   * TODO: How to make a clear code difference between [delete] and [deleteDryRun]. How to make sure that [deleteDryRun]
   *   will be called before [delete]? Should we call [deleteDryRun] before [delete]?
   */
  fun delete(range: FimRange)
  fun addLine(atPosition: EditorLine.Offset): EditorLine.Pointer?
  fun insertText(atPosition: Offset, text: CharSequence)
}

abstract class LinearEditor : FimEditor {
  abstract fun getLine(offset: Offset): EditorLine.Pointer
  abstract fun getText(left: Offset, right: Offset): CharSequence
}

abstract class MutableLinearEditor : MutableFimEditor, LinearEditor() {
  abstract fun deleteRange(leftOffset: Offset, rightOffset: Offset)

  override fun delete(range: FimRange) {
    when (range) {
      is FimRange.Block -> TODO()
      is FimRange.Character.Multiple -> TODO()
      is FimRange.Character.Range -> {
        deleteRange(range.offsetAbove(), range.offsetBelow())
      }
      is FimRange.Line.Multiple -> TODO()
      is FimRange.Line.Range -> {
        val startOffset = getLineRange(range.lineAbove()).first
        val endOffset = getLineRange(range.lineBelow()).second
        deleteRange(startOffset, endOffset)
      }
      is FimRange.Line.Offsets -> {
        var startOffset = getLineRange(getLine(range.offsetAbove())).first
        var endOffset = getLineRange(getLine(range.offsetBelow())).second
        var shiftType = LineDeleteShift.NO_NL
        if (endOffset.point < fileSize() && charAt(endOffset.point.pointer) == '\n') {
          endOffset = (endOffset.point + 1).offset
          shiftType = LineDeleteShift.NL_ON_END
        } else if (startOffset.point > 0 && lfMakesNewLine) {
          startOffset = (startOffset.point - 1).offset
          shiftType = LineDeleteShift.NL_ON_START
        }
        val (newStart, newEnd) = search((startOffset to endOffset), this, shiftType)?.first ?: return
        deleteRange(newStart, newEnd)
      }
    }
  }

  override fun deleteDryRun(range: FimRange): OperatedRange? {
    return when (range) {
      is FimRange.Block -> TODO()
      is FimRange.Character.Multiple -> TODO()
      is FimRange.Character.Range -> {
        val textToDelete = getText(range.offsetAbove(), range.offsetBelow())
        OperatedRange.Characters(textToDelete, range.offsetAbove(), range.offsetBelow())
      }
      is FimRange.Line.Multiple -> TODO()
      is FimRange.Line.Range -> {
        val startOffset = getLineRange(range.lineAbove()).first
        val endOffset = getLineRange(range.lineBelow()).second
        @Suppress("UNUSED_VARIABLE") val textToDelete = getText(startOffset, endOffset)
        TODO()
      }
      is FimRange.Line.Offsets -> {
        val lineAbove = getLine(range.offsetAbove())
        var startOffset = getLineRange(lineAbove).first
        val lineBelow = getLine(range.offsetBelow())
        var endOffset = getLineRange(lineBelow).second
        var shiftType = LineDeleteShift.NO_NL
        if (endOffset.point < fileSize() && charAt(endOffset.point.pointer) == '\n') {
          endOffset = (endOffset.point + 1).offset
          shiftType = LineDeleteShift.NL_ON_END
        } else if (startOffset.point > 0 && lfMakesNewLine) {
          startOffset = (startOffset.point - 1).offset
          shiftType = LineDeleteShift.NL_ON_START
        }
        val data = search((startOffset to endOffset), this, shiftType) ?: return null
        val (newStart, newEnd) = data.first
        shiftType = data.second
        val textToDelete = getText(newStart, newEnd)
        OperatedRange.Lines(
          textToDelete,
          EditorLine.Offset.init(lineAbove.line, this),
          lineBelow.line - lineAbove.line,
          shiftType
        )
      }
    }
  }
}

enum class LineDeleteShift {
  NL_ON_START,
  NL_ON_END,
  NO_NL,
}

class FimLogicalPosition(
  val line: Int,
  val column: Int,
  val leansForward: Boolean = false,
) : Comparable<FimLogicalPosition> {
  override fun compareTo(other: FimLogicalPosition): Int {
    return if (line != other.line) line - other.line else column - other.column
  }
}

data class FimVisualPosition(val line: Int, val column: Int, val leansRight: Boolean = false)
