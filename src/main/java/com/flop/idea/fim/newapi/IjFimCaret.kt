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

package com.flop.idea.fim.newapi

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.VisualPosition
import com.flop.idea.fim.api.CaretRegisterStorage
import com.flop.idea.fim.api.CaretRegisterStorageBase
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimCaretBase
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.api.FimVisualPosition
import com.flop.idea.fim.common.EditorLine
import com.flop.idea.fim.common.LiveRange
import com.flop.idea.fim.common.Offset
import com.flop.idea.fim.common.offset
import com.flop.idea.fim.group.visual.VisualChange
import com.flop.idea.fim.group.visual.fimLeadSelectionOffset
import com.flop.idea.fim.group.visual.fimSetSelection
import com.flop.idea.fim.group.visual.fimSetSystemSelectionSilently
import com.flop.idea.fim.group.visual.fimUpdateEditorSelection
import com.flop.idea.fim.helper.inlayAwareVisualColumn
import com.flop.idea.fim.helper.moveToInlayAwareOffset
import com.flop.idea.fim.helper.registerStorage
import com.flop.idea.fim.helper.fimInsertStart
import com.flop.idea.fim.helper.fimLastColumn
import com.flop.idea.fim.helper.fimLastVisualOperatorRange
import com.flop.idea.fim.helper.fimLine
import com.flop.idea.fim.helper.fimSelectionStart

class IjFimCaret(val caret: Caret) : FimCaretBase() {
  override val registerStorage: CaretRegisterStorage
    get() {
      var storage = this.caret.registerStorage
      if (storage == null) {
        storage = CaretRegisterStorageBase()
        this.caret.registerStorage = storage
      }
      return storage
    }
  override val editor: FimEditor
    get() = IjFimEditor(caret.editor)
  override val offset: Offset
    get() = caret.offset.offset
  override var fimLastColumn: Int
    get() = caret.fimLastColumn
    set(value) {
      caret.fimLastColumn = value
    }
  override val inlayAwareVisualColumn: Int
    get() = caret.inlayAwareVisualColumn
  override val selectionStart: Int
    get() = caret.selectionStart
  override val selectionEnd: Int
    get() = caret.selectionEnd
  override var fimSelectionStart: Int
    get() = this.caret.fimSelectionStart
    set(value) {
      this.caret.fimSelectionStart = value
    }
  override val fimLeadSelectionOffset: Int
    get() = this.caret.fimLeadSelectionOffset
  override var fimLastVisualOperatorRange: VisualChange?
    get() = this.caret.fimLastVisualOperatorRange
    set(value) {
      this.caret.fimLastVisualOperatorRange = value
    }
  override val fimLine: Int
    get() = this.caret.fimLine
  override val isPrimary: Boolean
    get() = editor.primaryCaret().ij == this.caret

  override fun moveToOffset(offset: Int) {
    // TODO: 17.12.2021 Unpack internal actions
    com.flop.idea.fim.group.MotionGroup.moveCaret(caret.editor, caret, offset)
  }

  override fun moveToOffsetNative(offset: Int) {
    caret.moveToOffset(offset)
  }

  override fun moveToLogicalPosition(logicalPosition: FimLogicalPosition) {
    this.caret.moveToLogicalPosition(LogicalPosition(logicalPosition.line, logicalPosition.column, logicalPosition.leansForward))
  }

  override fun offsetForLineStartSkipLeading(line: Int): Int {
    return com.flop.idea.fim.FimPlugin.getMotion().moveCaretToLineStartSkipLeading(editor, line)
  }

  override fun getLine(): EditorLine.Pointer {
    return EditorLine.Pointer.init(caret.logicalPosition.line, editor)
  }

  override fun hasSelection(): Boolean {
    return caret.hasSelection()
  }

  override fun fimSetSystemSelectionSilently(start: Int, end: Int) {
    caret.fimSetSystemSelectionSilently(start, end)
  }

  override val isValid: Boolean
    get() {
      return caret.isValid
    }

  override fun moveToInlayAwareOffset(newOffset: Int) {
    caret.moveToInlayAwareOffset(newOffset)
  }

  override fun fimSetSelection(start: Int, end: Int, moveCaretToSelectionEnd: Boolean) {
    caret.fimSetSelection(start, end, moveCaretToSelectionEnd)
  }

  override fun getLogicalPosition(): FimLogicalPosition {
    val logicalPosition = caret.logicalPosition
    return FimLogicalPosition(logicalPosition.line, logicalPosition.column, logicalPosition.leansForward)
  }

  override fun getVisualPosition(): FimVisualPosition {
    val visualPosition = caret.visualPosition
    return FimVisualPosition(visualPosition.line, visualPosition.column, visualPosition.leansRight)
  }

  override val visualLineStart: Int
    get() = caret.visualLineStart

  override fun updateEditorSelection() {
    caret.fimUpdateEditorSelection()
  }

  override var fimInsertStart: LiveRange
    get() = caret.fimInsertStart.fim
    set(value) {
      caret.fimInsertStart = value.ij
    }

  override fun moveToVisualPosition(position: FimVisualPosition) {
    caret.moveToVisualPosition(VisualPosition(position.line, position.column, position.leansRight))
  }

  override fun setNativeSelection(start: Offset, end: Offset) {
    caret.setSelection(start.point, end.point)
  }

  override fun removeNativeSelection() {
    caret.removeSelection()
  }

  override fun equals(other: Any?): Boolean = this.caret == (other as? IjFimCaret)?.caret

  override fun hashCode(): Int = this.caret.hashCode()
}

val FimCaret.ij: Caret
  get() = (this as IjFimCaret).caret

val Caret.fim: FimCaret
  get() = IjFimCaret(this)
