package com.flop.idea.fim.api

import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.EditorLine
import com.flop.idea.fim.common.LiveRange
import com.flop.idea.fim.common.Offset
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.group.visual.VisualChange
import com.flop.idea.fim.register.Register
import javax.swing.KeyStroke

// TODO: 29.12.2021 Split interface to mutable and immutable
interface FimCaret {
  val registerStorage: CaretRegisterStorage
  val editor: FimEditor
  val offset: Offset
  var fimLastColumn: Int
  val inlayAwareVisualColumn: Int
  val selectionStart: Int
  val selectionEnd: Int
  var fimSelectionStart: Int
  val fimLeadSelectionOffset: Int
  var fimLastVisualOperatorRange: VisualChange?
  val fimLine: Int
  val isPrimary: Boolean
  fun moveToOffset(offset: Int)
  fun moveToOffsetNative(offset: Int)
  fun moveToLogicalPosition(logicalPosition: FimLogicalPosition)
  fun offsetForLineStartSkipLeading(line: Int): Int
  fun getLine(): EditorLine.Pointer
  fun hasSelection(): Boolean
  fun fimSetSystemSelectionSilently(start: Int, end: Int)
  val isValid: Boolean
  fun moveToInlayAwareOffset(newOffset: Int)
  fun fimSetSelection(start: Int, end: Int = start, moveCaretToSelectionEnd: Boolean = false)
  fun getLogicalPosition(): FimLogicalPosition
  fun getVisualPosition(): FimVisualPosition
  val visualLineStart: Int
  fun updateEditorSelection()
  var fimInsertStart: LiveRange
  fun moveToVisualPosition(position: FimVisualPosition)
  fun setNativeSelection(start: Offset, end: Offset)
  fun removeNativeSelection()
}

interface CaretRegisterStorage {
  // todo methods shouldn't have caret in signature
  /**
   * Stores text to caret's recordable (named/numbered/unnamed) register
   */
  fun storeText(caret: FimCaret, editor: FimEditor, range: TextRange, type: SelectionType, isDelete: Boolean): Boolean

  /**
   * Gets text from caret's recordable register
   * If the register is not recordable - global text state will be returned
   */
  fun getRegister(caret: FimCaret, r: Char): Register?

  fun setKeys(caret: FimCaret, register: Char, keys: List<KeyStroke>)
  fun saveRegister(caret: FimCaret, r: Char, register: Register)
}
