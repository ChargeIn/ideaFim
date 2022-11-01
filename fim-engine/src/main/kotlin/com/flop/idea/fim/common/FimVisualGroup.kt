package com.flop.idea.fim.group.visual

import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString

fun charToNativeSelection(editor: FimEditor, start: Int, end: Int, mode: FimStateMachine.Mode): Pair<Int, Int> {
  val (nativeStart, nativeEnd) = sort(start, end)
  val lineEnd = editor.lineEndForOffset(nativeEnd)
  val adj =
    if (isExclusiveSelection() || nativeEnd == lineEnd || mode == FimStateMachine.Mode.SELECT) 0 else 1
  val adjEnd = (nativeEnd + adj).coerceAtMost(editor.fileSize().toInt())
  return nativeStart to adjEnd
}

/**
 * Convert fim's selection start and end to corresponding native selection.
 *
 * Adds caret adjustment or extends to line start / end in case of linewise selection
 */
fun lineToNativeSelection(editor: FimEditor, start: Int, end: Int): Pair<Int, Int> {
  val (nativeStart, nativeEnd) = sort(start, end)
  val lineStart = editor.lineStartForOffset(nativeStart)
  // Extend to \n char of line to fill full line with selection
  val lineEnd = (editor.lineEndForOffset(nativeEnd) + 1).coerceAtMost(editor.fileSize().toInt())
  return lineStart to lineEnd
}

fun <T : Comparable<T>> sort(a: T, b: T) = if (a > b) b to a else a to b

private fun isExclusiveSelection(): Boolean {
  return (
    injector.optionService.getOptionValue(
      OptionScope.GLOBAL,
      OptionConstants.selectionName
    ) as FimString
    ).value == "exclusive"
}

fun blockToNativeSelection(
  editor: FimEditor,
  start: Int,
  end: Int,
  mode: FimStateMachine.Mode,
): Pair<FimLogicalPosition, FimLogicalPosition> {
  var blockStart = editor.offsetToLogicalPosition(start)
  var blockEnd = editor.offsetToLogicalPosition(end)
  if (!isExclusiveSelection() && mode != FimStateMachine.Mode.SELECT) {
    if (blockStart.column > blockEnd.column) {
      if (blockStart.column < editor.lineLength(blockStart.line)) {
        blockStart = FimLogicalPosition(blockStart.line, blockStart.column + 1)
      }
    } else {
      if (blockEnd.column < editor.lineLength(blockEnd.line)) {
        blockEnd = FimLogicalPosition(blockEnd.line, blockEnd.column + 1)
      }
    }
  }
  return blockStart to blockEnd
}
