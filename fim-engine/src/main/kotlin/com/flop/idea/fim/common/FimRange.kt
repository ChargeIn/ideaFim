package com.flop.idea.fim.common

import com.flop.idea.fim.api.LineDeleteShift
import com.flop.idea.fim.api.MutableFimEditor
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.api.toType
import com.flop.idea.fim.mark.FimMarkConstants
import kotlin.math.max
import kotlin.math.min

/**
 * Back direction range is possible. `start` is not lower than `end`.
 * TODO: How to show it in code and namings?
 *    How to separate methods that return "starting from" line and "the above line"
 *
 * TODO: How to represent "till last column"
 *
 * [FimRange] includes selection type (line, character, block)
 *
 * Range normalizations (check if line and offsets really exist) are performed in editor implementations.
 */
sealed class FimRange {
  sealed class Line : FimRange() {
    class Range(val startLine: EditorLine.Pointer, val endLine: EditorLine.Pointer) : Line() {
      fun lineAbove(): EditorLine.Pointer = listOf(startLine, endLine).minByOrNull { it.line }!!
      fun lineBelow(): EditorLine.Pointer = listOf(startLine, endLine).maxByOrNull { it.line }!!
    }

    class Multiple(val lines: List<Int>) : Line()

    // TODO: 11.01.2022 How converting offsets to lines work?
    class Offsets(val startOffset: Offset, val endOffset: Offset) : Line() {
      fun offsetAbove(): Offset = min(startOffset.point, endOffset.point).offset
      fun offsetBelow(): Offset = max(startOffset.point, endOffset.point).offset
    }
  }

  sealed class Character : FimRange() {
    class Range(val range: FimTextRange) : Character() {
      fun offsetAbove(): Offset = min(range.start.point, range.end.point).offset
      fun offsetBelow(): Offset = max(range.start.point, range.end.point).offset
    }

    class Multiple(val ranges: List<FimTextRange>) : Character()
  }

  class Block(val start: Offset, val end: Offset) : FimRange()
}

/**
 * `start` is not lower than `end`
 */
data class FimTextRange(
  val start: Offset,
  val end: Offset,
) {
  init {
    if (start.point > end.point) {
      println()
    }
  }
}

infix fun Int.including(another: Int): FimTextRange {
  return FimTextRange(this.offset, another.offset)
}

data class Offset(val point: Int)
data class Pointer(val point: Int)

val Int.offset: Offset
  get() = Offset(this)
val Int.pointer: Pointer
  get() = Pointer(this)

interface FimMachine {
  fun delete(range: FimRange, editor: FimEditor, caret: FimCaret): OperatedRange?
}

abstract class FimMachineBase : FimMachine {
  /**
   * The information I'd like to know after the deletion:
   * - What range is deleted?
   * - What text is deleted?
   * - Does text have a new line character at the end?
   * - At what offset?
   * - What caret?
   */
  override fun delete(range: FimRange, editor: FimEditor, caret: FimCaret): OperatedRange? {
    // Update the last column before we delete, or we might be retrieving the data for a line that no longer exists
    caret.fimLastColumn = caret.inlayAwareVisualColumn

    val operatedText = editor.deleteDryRun(range) ?: return null

    val normalizedRange = operatedText.toNormalizedTextRange(editor)
    caret.registerStorage.storeText(caret, editor, normalizedRange, operatedText.toType(), true)
    (editor as MutableFimEditor).delete(range)

    val start = normalizedRange.startOffset
    injector.markGroup.setMark(editor, FimMarkConstants.MARK_CHANGE_POS, start)
    injector.markGroup.setChangeMarks(editor, TextRange(start, start + 1))

    return operatedText
  }
}

fun OperatedRange.toNormalizedTextRange(editor: FimEditor): TextRange {
  return when (this) {
    is OperatedRange.Block -> TODO()
    is OperatedRange.Lines -> {
      // TODO: 11.01.2022 This is unsafe
      val startOffset = editor.getLineStartOffset(this.lineAbove.line)
      val endOffset = editor.getLineEndOffset(lineAbove.line + linesOperated, true)
      TextRange(startOffset, endOffset)
    }
    is OperatedRange.Characters -> TextRange(this.leftOffset.point, this.rightOffset.point)
  }
}

sealed class EditorLine private constructor(val line: Int) {
  class Pointer(line: Int) : EditorLine(line) {
    companion object {
      fun init(line: Int, forEditor: FimEditor): Pointer {
        if (line < 0) error("")
        if (line >= forEditor.lineCount()) error("")
        return Pointer(line)
      }
    }
  }

  class Offset(line: Int) : EditorLine(line) {

    fun toPointer(forEditor: FimEditor): Pointer {
      return Pointer.init(line.coerceAtMost(forEditor.lineCount() - 1), forEditor)
    }

    companion object {
      fun init(line: Int, forEditor: FimEditor): Offset {
        if (line < 0) error("")
        // TODO: 28.12.2021 Is this logic correct?
        //   IJ has an additional line
        if (line > forEditor.lineCount()) error("")
        return Offset(line)
      }
    }
  }
}

sealed class OperatedRange {
  class Lines(
    val text: CharSequence,
    val lineAbove: EditorLine.Offset,
    val linesOperated: Int,
    val shiftType: LineDeleteShift,
  ) : OperatedRange()

  class Characters(val text: CharSequence, val leftOffset: Offset, val rightOffset: Offset) : OperatedRange()
  class Block : OperatedRange() {
    init {
      TODO()
    }
  }
}
