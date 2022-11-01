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

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.vfs.VirtualFileManager
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.LineDeleteShift
import com.flop.idea.fim.api.MutableLinearEditor
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimCaretListener
import com.flop.idea.fim.api.FimDocument
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.api.FimSelectionModel
import com.flop.idea.fim.api.FimVisualPosition
import com.flop.idea.fim.api.VirtualFile
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.EditorLine
import com.flop.idea.fim.common.LiveRange
import com.flop.idea.fim.common.Offset
import com.flop.idea.fim.common.Pointer
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.common.offset
import com.flop.idea.fim.group.visual.fimSetSystemBlockSelectionSilently
import com.flop.idea.fim.helper.exitInsertMode
import com.flop.idea.fim.helper.exitSelectMode
import com.flop.idea.fim.helper.exitVisualMode
import com.flop.idea.fim.helper.fileSize
import com.flop.idea.fim.helper.getTopLevelEditor
import com.flop.idea.fim.helper.inBlockSubMode
import com.flop.idea.fim.helper.isTemplateActive
import com.flop.idea.fim.helper.updateCaretsVisualAttributes
import com.flop.idea.fim.helper.updateCaretsVisualPosition
import com.flop.idea.fim.helper.fimChangeActionSwitchMode
import com.flop.idea.fim.helper.fimKeepingVisualOperatorAction
import com.flop.idea.fim.helper.fimLastSelectionType
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class IjFimEditor(editor: Editor) : MutableLinearEditor() {

  // All the editor actions should be performed with top level editor!!!
  // Be careful: all the EditorActionHandler implementation should correctly process InjectedEditors
  // TBH, I don't like the names. Need to think a bit more about this
  val editor = editor.getTopLevelEditor()
  val originalEditor = editor

  override val lfMakesNewLine: Boolean = true
  override var fimChangeActionSwitchMode: FimStateMachine.Mode?
    get() = editor.fimChangeActionSwitchMode
    set(value) {
      editor.fimChangeActionSwitchMode = value
    }
  override var fimKeepingVisualOperatorAction: Boolean
    get() = editor.fimKeepingVisualOperatorAction
    set(value) {
      editor.fimKeepingVisualOperatorAction = value
    }

  override fun fileSize(): Long = editor.fileSize.toLong()

  override fun text(): CharSequence {
    return editor.document.charsSequence
  }

  override fun lineCount(): Int {
    val lineCount = editor.document.lineCount
    return lineCount.coerceAtLeast(1)
  }

  override fun nativeLineCount(): Int {
    return editor.document.lineCount
  }

  override fun deleteRange(leftOffset: Offset, rightOffset: Offset) {
    editor.document.deleteString(leftOffset.point, rightOffset.point)
  }

  override fun addLine(atPosition: EditorLine.Offset): EditorLine.Pointer {
    val offset: Int = if (atPosition.line < lineCount()) {

      // The new line character is inserted before the new line char of the previous line. So it works line an enter
      //   on a line end. I believe that the correct implementation would be to insert the new line char after the
      //   \n of the previous line, however at the moment this won't update the mark on this line.
      //   https://youtrack.jetbrains.com/issue/IDEA-286587

      val lineStart = (editor.document.getLineStartOffset(atPosition.line) - 1).coerceAtLeast(0)
      val guard = editor.document.getOffsetGuard(lineStart)
      if (guard != null && guard.endOffset == lineStart + 1) {
        // Dancing around guarded blocks. It may happen that this concrete position is locked, but the next
        //   (after the new line character) is not. In this case we can actually insert the line after this
        //   new line char
        // Such thing is often used in pycharm notebooks.
        lineStart + 1
      } else {
        lineStart
      }
    } else {
      fileSize().toInt()
    }
    editor.document.insertString(offset, "\n")
    return EditorLine.Pointer.init(atPosition.line, this)
  }

  override fun insertText(atPosition: Offset, text: CharSequence) {
    editor.document.insertString(atPosition.point, text)
  }

  // TODO: 30.12.2021 Is end offset inclusive?
  override fun getLineRange(line: EditorLine.Pointer): Pair<Offset, Offset> {
    // TODO: 30.12.2021 getLineEndOffset returns the same value for "xyz" and "xyz\n"
    return editor.document.getLineStartOffset(line.line).offset to editor.document.getLineEndOffset(line.line).offset
  }

  override fun getLine(offset: Offset): EditorLine.Pointer {
    return EditorLine.Pointer.init(editor.offsetToLogicalPosition(offset.point).line, this)
  }

  override fun charAt(offset: Pointer): Char {
    return editor.document.charsSequence[offset.point]
  }

  override fun carets(): List<FimCaret> {
    return if (editor.inBlockSubMode) {
      listOf(IjFimCaret(editor.caretModel.primaryCaret))
    } else {
      editor.caretModel.allCarets.map { IjFimCaret(it) }
    }
  }

  override fun nativeCarets(): List<FimCaret> {
    return editor.caretModel.allCarets.map { IjFimCaret(it) }
  }

  @Suppress("ideafimRunForEachCaret")
  override fun forEachCaret(action: (FimCaret) -> Unit) {
    forEachCaret(action, false)
  }

  override fun forEachCaret(action: (FimCaret) -> Unit, reverse: Boolean) {
    if (editor.inBlockSubMode) {
      action(IjFimCaret(editor.caretModel.primaryCaret))
    } else {
      editor.caretModel.runForEachCaret({ action(IjFimCaret(it)) }, reverse)
    }
  }

  override fun forEachNativeCaret(action: (FimCaret) -> Unit) {
    forEachNativeCaret(action, false)
  }

  override fun forEachNativeCaret(action: (FimCaret) -> Unit, reverse: Boolean) {
    editor.caretModel.runForEachCaret({ action(IjFimCaret(it)) }, reverse)
  }

  override fun primaryCaret(): FimCaret {
    return IjFimCaret(editor.caretModel.primaryCaret)
  }

  override fun currentCaret(): FimCaret {
    return IjFimCaret(editor.caretModel.currentCaret)
  }

  override fun charsSequence(): CharSequence {
    return editor.document.charsSequence
  }

  override fun isWritable(): Boolean {
    val modificationAllowed = EditorModificationUtil.checkModificationAllowed(editor)
    val writeRequested = EditorModificationUtil.requestWriting(editor)
    return modificationAllowed && writeRequested
  }

  override fun isDocumentWritable(): Boolean {
    return editor.document.isWritable
  }

  override fun isOneLineMode(): Boolean {
    return editor.isOneLineMode
  }

  override fun getText(left: Offset, right: Offset): CharSequence {
    return editor.document.charsSequence.subSequence(left.point, right.point)
  }

  override fun search(pair: Pair<Offset, Offset>, editor: FimEditor, shiftType: LineDeleteShift): Pair<Pair<Offset, Offset>, LineDeleteShift>? {
    val ijEditor = (editor as IjFimEditor).editor
    return when (shiftType) {
      LineDeleteShift.NO_NL -> if (pair.noGuard(ijEditor)) return pair to shiftType else null
      LineDeleteShift.NL_ON_END -> {
        if (pair.noGuard(ijEditor)) return pair to shiftType

        pair.shift(-1, -1) {
          if (this.noGuard(ijEditor)) return this to LineDeleteShift.NL_ON_START
        }

        pair.shift(shiftEnd = -1) {
          if (this.noGuard(ijEditor)) return this to LineDeleteShift.NO_NL
        }

        null
      }
      LineDeleteShift.NL_ON_START -> {
        if (pair.noGuard(ijEditor)) return pair to shiftType

        pair.shift(shiftStart = 1) {
          if (this.noGuard(ijEditor)) return this to LineDeleteShift.NO_NL
        }

        null
      }
    }
  }

  override fun updateCaretsVisualAttributes() {
    editor.updateCaretsVisualAttributes()
  }

  override fun updateCaretsVisualPosition() {
    editor.updateCaretsVisualPosition()
  }

  override fun lineEndForOffset(offset: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(editor, offset)
  }

  override fun lineStartForOffset(offset: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(editor, offset)
  }

  override fun offsetToVisualPosition(offset: Int): FimVisualPosition {
    return editor.offsetToVisualPosition(offset).let { FimVisualPosition(it.line, it.column, it.leansRight) }
  }

  override fun offsetToLogicalPosition(offset: Int): FimLogicalPosition {
    return editor.offsetToLogicalPosition(offset).let { FimLogicalPosition(it.line, it.column, it.leansForward) }
  }

  override fun logicalPositionToOffset(position: FimLogicalPosition): Int {
    val logicalPosition = LogicalPosition(position.line, position.column, position.leansForward)
    return editor.logicalPositionToOffset(logicalPosition)
  }

  override fun getLineText(line: Int): String {
    return com.flop.idea.fim.helper.EditorHelper.getLineText(this.editor, line)
  }

  override fun getVirtualFile(): VirtualFile? {
    val vf = com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor)
    return vf?.let {
      object : VirtualFile {
        override val path = vf.path
      }
    }
  }

  override fun deleteString(range: TextRange) {
    editor.document.deleteString(range.startOffset, range.endOffset)
  }

  override fun getText(range: TextRange): String {
    return editor.document.getText(com.intellij.openapi.util.TextRange(range.startOffset, range.endOffset))
  }

  override fun lineLength(line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineLength(editor, line)
  }

  override fun getSelectionModel(): FimSelectionModel {
    return object : FimSelectionModel {
      private val sm = editor.selectionModel
      override val selectionStart = sm.selectionStart
      override val selectionEnd = sm.selectionEnd

      override fun hasSelection(): Boolean {
        return sm.hasSelection()
      }
    }
  }

  override fun removeCaret(caret: FimCaret) {
    editor.caretModel.removeCaret((caret as IjFimCaret).caret)
  }

  override fun removeSecondaryCarets() {
    editor.caretModel.removeSecondaryCarets()
  }

  override fun fimSetSystemBlockSelectionSilently(start: FimLogicalPosition, end: FimLogicalPosition) {
    val startPosition = LogicalPosition(start.line, start.column, start.leansForward)
    val endPosition = LogicalPosition(end.line, end.column, end.leansForward)
    editor.selectionModel.fimSetSystemBlockSelectionSilently(startPosition, endPosition)
  }

  override fun getLineStartOffset(line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineStartOffset(editor, line)
  }

  override fun getLineEndOffset(line: Int, allowEnd: Boolean): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineEndOffset(editor, line, allowEnd)
  }

  override fun getLineEndOffset(line: Int): Int {
    return editor.document.getLineEndOffset(line)
  }

  override fun getLineEndForOffset(offset: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(editor, offset)
  }

  val listenersMap: MutableMap<FimCaretListener, CaretListener> = mutableMapOf()

  override fun addCaretListener(listener: FimCaretListener) {
    val caretListener = object : CaretListener {
      override fun caretRemoved(event: CaretEvent) {
        listener.caretRemoved(event.caret?.fim)
      }
    }
    listenersMap[listener] = caretListener
    editor.caretModel.addCaretListener(caretListener)
  }

  override fun removeCaretListener(listener: FimCaretListener) {
    val caretListener = listenersMap.remove(listener) ?: error("Existing listener expected")
    editor.caretModel.removeCaretListener(caretListener)
  }

  override fun isDisposed(): Boolean {
    return editor.isDisposed
  }

  override fun removeSelection() {
    editor.selectionModel.removeSelection()
  }

  override fun getPath(): String? {
    return com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor)?.path
  }

  override fun extractProtocol(): String? {
    return com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor)?.getUrl()?.let { VirtualFileManager.extractProtocol(it) }
  }

  override fun visualPositionToOffset(position: FimVisualPosition): Offset {
    return editor.visualPositionToOffset(VisualPosition(position.line, position.column, position.leansRight)).offset
  }

  override fun exitInsertMode(context: ExecutionContext, operatorArguments: OperatorArguments) {
    editor.exitInsertMode(context.ij, operatorArguments)
  }

  override fun exitSelectModeNative(adjustCaret: Boolean) {
    this.exitSelectMode(adjustCaret)
  }

  override fun exitVisualModeNative() {
    this.editor.exitVisualMode()
  }

  override fun startGuardedBlockChecking() {
    val doc = editor.document
    doc.startGuardedBlockChecking()
  }

  override fun stopGuardedBlockChecking() {
    val doc = editor.document
    doc.stopGuardedBlockChecking()
  }

  override var fimLastSelectionType: SelectionType?
    get() = editor.fimLastSelectionType
    set(value) {
      editor.fimLastSelectionType = value
    }

  override fun isTemplateActive(): Boolean {
    return editor.isTemplateActive()
  }

  override fun hasUnsavedChanges(): Boolean {
    return com.flop.idea.fim.helper.EditorHelper.hasUnsavedChanges(this.editor)
  }

  override fun createLiveMarker(start: Offset, end: Offset): LiveRange {
    return editor.document.createRangeMarker(start.point, end.point).fim
  }

  override var insertMode: Boolean
    get() = (editor as? EditorEx)?.isInsertMode ?: false
    set(value) {
      (editor as? EditorEx)?.isInsertMode = value
    }

  override val document: FimDocument
    get() = IjFimDocument(editor.document)

  private fun Pair<Offset, Offset>.noGuard(editor: Editor): Boolean {
    return editor.document.getRangeGuard(this.first.point, this.second.point) == null
  }

  private inline fun Pair<Offset, Offset>.shift(
    shiftStart: Int = 0,
    shiftEnd: Int = 0,
    action: Pair<Offset, Offset>.() -> Unit,
  ) {
    val data =
      (this.first.point + shiftStart).coerceAtLeast(0).offset to (this.second.point + shiftEnd).coerceAtLeast(0).offset
    data.action()
  }
  override fun equals(other: Any?): Boolean {
    error("equals and hashCode should not be used with IjFimEditor")
  }

  override fun hashCode(): Int {
    error("equals and hashCode should not be used with IjFimEditor")
  }
}

val Editor.fim: IjFimEditor
  get() = IjFimEditor(this)
val FimEditor.ij: Editor
  get() = (this as IjFimEditor).editor
