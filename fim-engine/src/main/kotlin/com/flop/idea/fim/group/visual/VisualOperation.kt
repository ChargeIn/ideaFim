package com.flop.idea.fim.group.visual

import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.api.FimMotionGroupBase
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.helper.inBlockSubMode
import com.flop.idea.fim.helper.subMode
import java.util.*
import kotlin.math.min

object VisualOperation {
  /**
   * Get [VisualChange] of current visual operation
   */
  fun getRange(editor: FimEditor, caret: FimCaret, cmdFlags: EnumSet<CommandFlags>): VisualChange {
    var (start, end) = caret.run {
      if (editor.inBlockSubMode) sort(fimSelectionStart, offset.point) else sort(selectionStart, selectionEnd)
    }
    val type = SelectionType.fromSubMode(editor.subMode)

    start = injector.engineEditorHelper.normalizeOffset(editor, start, false)
    end = injector.engineEditorHelper.normalizeOffset(editor, end, false)
    val sp = editor.offsetToLogicalPosition(start)
    val ep = editor.offsetToLogicalPosition(end)
    var lines = ep.line - sp.line + 1
    if (type == SelectionType.LINE_WISE && ep.column == 0 && lines > 0) lines--

    if (CommandFlags.FLAG_MOT_LINEWISE in cmdFlags) return VisualChange(lines, ep.column, SelectionType.LINE_WISE)

    val chars = if (editor.primaryCaret().fimLastColumn == FimMotionGroupBase.LAST_COLUMN) {
      FimMotionGroupBase.LAST_COLUMN
    } else when (type) {
      SelectionType.LINE_WISE -> ep.column
      SelectionType.CHARACTER_WISE -> if (lines > 1) ep.column - injector.visualMotionGroup.selectionAdj else ep.column - sp.column
      SelectionType.BLOCK_WISE -> ep.column - sp.column + 1
    }

    return VisualChange(lines, chars, type)
  }

  /**
   * Calculate end offset of [VisualChange]
   */
  fun calculateRange(editor: FimEditor, range: VisualChange, count: Int, caret: FimCaret): Int {
    var (lines, chars, type) = range
    if (type == SelectionType.LINE_WISE || type == SelectionType.BLOCK_WISE || lines > 1) {
      lines *= count
    }
    if (type == SelectionType.CHARACTER_WISE && lines == 1 || type == SelectionType.BLOCK_WISE) {
      chars *= count
    }
    val sp = caret.getLogicalPosition()
    val linesDiff = (lines - 1).coerceAtLeast(0)
    val endLine = (sp.line + linesDiff).coerceAtMost(editor.lineCount() - 1)

    return when (type) {
      SelectionType.LINE_WISE -> injector.motion.moveCaretToLineWithSameColumn(editor, endLine, caret)
      SelectionType.CHARACTER_WISE -> when {
        lines > 1 -> injector.motion.moveCaretToLineStart(editor, endLine) + min(editor.lineLength(endLine), chars)
        else -> injector.engineEditorHelper.normalizeOffset(editor, sp.line, caret.offset.point + chars - 1, true)
      }
      SelectionType.BLOCK_WISE -> {
        val endColumn = min(editor.lineLength(endLine), sp.column + chars - 1)
        editor.logicalPositionToOffset(FimLogicalPosition(endLine, endColumn))
      }
    }
  }
}
