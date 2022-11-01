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

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.FimMotionGroupBase
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.editorMode
import com.flop.idea.fim.helper.inBlockSubMode
import com.flop.idea.fim.helper.inSelectMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.isEndAllowed
import com.flop.idea.fim.helper.moveToInlayAwareOffset
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.helper.updateCaretsVisualAttributes
import com.flop.idea.fim.helper.fimLastColumn
import com.flop.idea.fim.helper.fimSelectionStart
import com.flop.idea.fim.newapi.IjFimEditor

/**
 * @author Alex Plate
 */

/**
 * Set selection for caret
 * This method doesn't change CommandState and operates only with caret and it's properties
 * if [moveCaretToSelectionEnd] is true, caret movement to [end] will be performed
 */
fun Caret.fimSetSelection(start: Int, end: Int = start, moveCaretToSelectionEnd: Boolean = false) {
  fimSelectionStart = start
  setVisualSelection(start, end, this)
  if (moveCaretToSelectionEnd && !editor.inBlockSubMode) moveToInlayAwareOffset(end)
}

/**
 * Move selection end to current caret position
 * This method is created only for Character and Line mode
 * @see fimMoveBlockSelectionToOffset for blockwise selection
 */
fun Caret.fimMoveSelectionToCaret() {
  if (!editor.inVisualMode && !editor.inSelectMode) error("Attempt to extent selection in non-visual mode")
  if (editor.inBlockSubMode) error("Move caret with [fimMoveBlockSelectionToOffset]")

  val startOffsetMark = fimSelectionStart

  setVisualSelection(startOffsetMark, offset, this)
}

/**
 * Move selection end to current primary caret position
 * This method is created only for block mode
 * @see fimMoveSelectionToCaret for character and line selection
 */
fun fimMoveBlockSelectionToOffset(editor: Editor, offset: Int) {
  val primaryCaret = editor.caretModel.primaryCaret
  val startOffsetMark = primaryCaret.fimSelectionStart

  setVisualSelection(startOffsetMark, offset, primaryCaret)
}

/**
 * Update selection according to new CommandState
 * This method should be used for switching from character to line wise selection and so on
 */
fun Caret.fimUpdateEditorSelection() {
  val startOffsetMark = fimSelectionStart
  setVisualSelection(startOffsetMark, offset, this)
}

/**
 * This works almost like [Caret.getLeadSelectionOffset], but fim-specific
 */
val Caret.fimLeadSelectionOffset: Int
  get() {
    val caretOffset = offset
    if (hasSelection()) {
      val selectionAdj = com.flop.idea.fim.FimPlugin.getVisualMotion().selectionAdj
      if (caretOffset != selectionStart && caretOffset != selectionEnd) {
        // Try to check if current selection is tweaked by fold region.
        val foldingModel = editor.foldingModel
        val foldRegion = foldingModel.getCollapsedRegionAtOffset(caretOffset)
        if (foldRegion != null) {
          if (foldRegion.startOffset == selectionStart) {
            return (selectionEnd - selectionAdj).coerceAtLeast(0)
          } else if (foldRegion.endOffset == selectionEnd) {
            return selectionStart
          }
        }
      }

      return if (editor.subMode == FimStateMachine.SubMode.VISUAL_LINE) {
        val selectionStartLine = editor.offsetToLogicalPosition(selectionStart).line
        val caretLine = editor.offsetToLogicalPosition(this.offset).line
        if (caretLine == selectionStartLine) {
          val column = editor.offsetToLogicalPosition(selectionEnd).column
          if (column == 0) (selectionEnd - 1).coerceAtLeast(0) else selectionEnd
        } else selectionStart
      } else if (editor.inBlockSubMode) {
        val selections = editor.caretModel.allCarets.map { it.selectionStart to it.selectionEnd }.sortedBy { it.first }
        val pCaret = editor.caretModel.primaryCaret
        when (pCaret.offset) {
          selections.first().first -> (selections.last().second - selectionAdj).coerceAtLeast(0)
          selections.first().second -> selections.last().first
          selections.last().first -> (selections.first().second - selectionAdj).coerceAtLeast(0)
          selections.last().second -> selections.first().first
          else -> selections.first().first
        }
      } else {
        if (caretOffset == selectionStart) (selectionEnd - selectionAdj).coerceAtLeast(0) else selectionStart
      }
    }
    return caretOffset
  }

fun moveCaretOneCharLeftFromSelectionEnd(editor: Editor, predictedMode: FimStateMachine.Mode) {
  if (predictedMode != FimStateMachine.Mode.VISUAL) {
    if (!predictedMode.isEndAllowed) {
      editor.caretModel.allCarets.forEach { caret ->
        val lineEnd = com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(editor, caret.offset)
        val lineStart = com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(editor, caret.offset)
        if (caret.offset == lineEnd && lineEnd != lineStart) caret.moveToInlayAwareOffset(caret.offset - 1)
      }
    }
    return
  }
  editor.caretModel.allCarets.forEach { caret ->
    if (caret.hasSelection() && caret.selectionEnd == caret.offset) {
      if (caret.selectionEnd <= 0) return@forEach
      if (com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(editor, caret.selectionEnd - 1) != caret.selectionEnd - 1 &&
        caret.selectionEnd > 1 && editor.document.text[caret.selectionEnd - 1] == '\n'
      ) {
        caret.moveToInlayAwareOffset(caret.selectionEnd - 2)
      } else {
        caret.moveToInlayAwareOffset(caret.selectionEnd - 1)
      }
    }
  }
}

private fun setVisualSelection(selectionStart: Int, selectionEnd: Int, caret: Caret) {
  val (start, end) = if (selectionStart > selectionEnd) selectionEnd to selectionStart else selectionStart to selectionEnd
  val editor = caret.editor
  val subMode = editor.subMode
  val mode = editor.editorMode
  val fimEditor = IjFimEditor(editor)
  when (subMode) {
    FimStateMachine.SubMode.VISUAL_CHARACTER -> {
      val (nativeStart, nativeEnd) = charToNativeSelection(fimEditor, start, end, mode)
      caret.fimSetSystemSelectionSilently(nativeStart, nativeEnd)
    }
    FimStateMachine.SubMode.VISUAL_LINE -> {
      val (nativeStart, nativeEnd) = lineToNativeSelection(fimEditor, start, end)
      caret.fimSetSystemSelectionSilently(nativeStart, nativeEnd)
    }
    FimStateMachine.SubMode.VISUAL_BLOCK -> {
      editor.caretModel.removeSecondaryCarets()

      // Set system selection
      val (blockStart, blockEnd) = blockToNativeSelection(fimEditor, selectionStart, selectionEnd, mode)
      val lastColumn = editor.caretModel.primaryCaret.fimLastColumn
      fimEditor.fimSetSystemBlockSelectionSilently(blockStart, blockEnd)

      // We've just added secondary carets again, hide them to better emulate block selection
      editor.updateCaretsVisualAttributes()

      for (aCaret in editor.caretModel.allCarets) {
        if (!aCaret.isValid) continue
        val line = aCaret.logicalPosition.line
        val lineEndOffset = com.flop.idea.fim.helper.EditorHelper.getLineEndOffset(editor, line, true)
        val lineStartOffset = com.flop.idea.fim.helper.EditorHelper.getLineStartOffset(editor, line)

        // Extend selection to line end if it was made with `$` command
        if (lastColumn >= FimMotionGroupBase.LAST_COLUMN) {
          aCaret.fimSetSystemSelectionSilently(aCaret.selectionStart, lineEndOffset)
          val newOffset = (lineEndOffset - com.flop.idea.fim.FimPlugin.getVisualMotion().selectionAdj).coerceAtLeast(lineStartOffset)
          aCaret.moveToInlayAwareOffset(newOffset)
        }
        val visualPosition = editor.offsetToVisualPosition(aCaret.selectionEnd)
        if (aCaret.offset == aCaret.selectionEnd && visualPosition != aCaret.visualPosition) {
          // Put right caret position for tab character
          aCaret.moveToVisualPosition(visualPosition)
        }
        if (mode != FimStateMachine.Mode.SELECT &&
          !com.flop.idea.fim.helper.EditorHelper.isLineEmpty(editor, line, false) &&
          aCaret.offset == aCaret.selectionEnd &&
          aCaret.selectionEnd - 1 >= lineStartOffset &&
          aCaret.selectionEnd - aCaret.selectionStart != 0
        ) {
          // Move all carets one char left in case if it's on selection end
          aCaret.moveToVisualPosition(VisualPosition(visualPosition.line, visualPosition.column - 1))
        }
      }

      editor.caretModel.primaryCaret.moveToInlayAwareOffset(selectionEnd)
    }
    else -> Unit
  }
}
