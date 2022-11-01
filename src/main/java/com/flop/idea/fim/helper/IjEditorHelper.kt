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

package com.flop.idea.fim.helper

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.ReadOnlyFragmentModificationException
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.ex.util.EditorUtil
import com.intellij.openapi.util.text.StringUtil
import com.flop.idea.fim.api.EngineEditorHelper
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimVisualPosition
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.newapi.IjFimCaret
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import java.nio.CharBuffer

@Service
class IjEditorHelper : EngineEditorHelper {
  override fun normalizeOffset(editor: FimEditor, offset: Int, allowEnd: Boolean): Int {
    return com.flop.idea.fim.helper.EditorHelper.normalizeOffset((editor as IjFimEditor).editor, offset, allowEnd)
  }

  override fun normalizeOffset(editor: FimEditor, line: Int, offset: Int, allowEnd: Boolean): Int {
    return com.flop.idea.fim.helper.EditorHelper.normalizeOffset((editor as IjFimEditor).editor, line, offset, allowEnd)
  }

  override fun getText(editor: FimEditor, range: TextRange): String {
    return com.flop.idea.fim.helper.EditorHelper.getText((editor as IjFimEditor).editor, range)
  }

  override fun getOffset(editor: FimEditor, line: Int, column: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getOffset((editor as IjFimEditor).editor, line, column)
  }

  override fun logicalLineToVisualLine(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.logicalLineToVisualLine((editor as IjFimEditor).editor, line)
  }

  override fun normalizeVisualLine(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.normalizeVisualLine((editor as IjFimEditor).editor, line)
  }

  override fun normalizeVisualColumn(editor: FimEditor, line: Int, col: Int, allowEnd: Boolean): Int {
    return com.flop.idea.fim.helper.EditorHelper.normalizeVisualColumn((editor as IjFimEditor).editor, line, col, allowEnd)
  }

  override fun amountOfInlaysBeforeVisualPosition(editor: FimEditor, pos: FimVisualPosition): Int {
    return (editor as IjFimEditor).editor.amountOfInlaysBeforeVisualPosition(
      VisualPosition(
        pos.line,
        pos.column,
        pos.leansRight
      )
    )
  }

  override fun getVisualLineCount(editor: FimEditor): Int {
    return com.flop.idea.fim.helper.EditorHelper.getVisualLineCount(editor)
  }

  override fun prepareLastColumn(caret: FimCaret): Int {
    return com.flop.idea.fim.helper.EditorHelper.prepareLastColumn((caret as IjFimCaret).caret)
  }

  override fun updateLastColumn(caret: FimCaret, prevLastColumn: Int) {
    com.flop.idea.fim.helper.EditorHelper.updateLastColumn((caret as IjFimCaret).caret, prevLastColumn)
  }

  override fun getLineEndOffset(editor: FimEditor, line: Int, allowEnd: Boolean): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineEndOffset((editor as IjFimEditor).editor, line, allowEnd)
  }

  override fun getLineStartOffset(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineStartOffset((editor as IjFimEditor).editor, line)
  }

  override fun getLineStartForOffset(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset((editor as IjFimEditor).editor, line)
  }

  override fun getLineEndForOffset(editor: FimEditor, offset: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset((editor as IjFimEditor).editor, offset)
  }

  override fun visualLineToLogicalLine(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.visualLineToLogicalLine(editor.ij, line)
  }

  override fun normalizeLine(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.normalizeLine(editor.ij, line)
  }

  override fun getVisualLineAtTopOfScreen(editor: FimEditor): Int {
    return com.flop.idea.fim.helper.EditorHelper.getVisualLineAtTopOfScreen(editor.ij)
  }

  override fun getApproximateScreenWidth(editor: FimEditor): Int {
    return com.flop.idea.fim.helper.EditorHelper.getApproximateScreenWidth(editor.ij)
  }

  override fun handleWithReadonlyFragmentModificationHandler(editor: FimEditor, exception: Exception) {
    return EditorActionManager.getInstance()
      .getReadonlyFragmentModificationHandler(editor.ij.document)
      .handle(exception as ReadOnlyFragmentModificationException?)
  }

  override fun getLineBuffer(editor: FimEditor, line: Int): CharBuffer {
    return com.flop.idea.fim.helper.EditorHelper.getLineBuffer(editor.ij, line)
  }

  override fun getVisualLineAtBottomOfScreen(editor: FimEditor): Int {
    return com.flop.idea.fim.helper.EditorHelper.getVisualLineAtBottomOfScreen(editor.ij)
  }

  override fun pad(editor: FimEditor, context: ExecutionContext, line: Int, to: Int): String {
    return com.flop.idea.fim.helper.EditorHelper.pad(editor.ij, context.ij, line, to)
  }

  override fun getLineLength(editor: FimEditor, logicalLine: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineLength(editor.ij, logicalLine)
  }

  override fun getLineLength(editor: FimEditor): Int {
    return com.flop.idea.fim.helper.EditorHelper.getLineLength(editor.ij)
  }

  override fun getLineBreakCount(text: CharSequence): Int {
    return StringUtil.getLineBreakCount(text)
  }

  override fun inlayAwareOffsetToVisualPosition(editor: FimEditor, offset: Int): FimVisualPosition {
    return EditorUtil.inlayAwareOffsetToVisualPosition(editor.ij, offset).fim
  }

  override fun getVisualLineLength(editor: FimEditor, line: Int): Int {
    return com.flop.idea.fim.helper.EditorHelper.getVisualLineLength(editor.ij, line)
  }

  override fun getLeadingWhitespace(editor: FimEditor, line: Int): String {
    return com.flop.idea.fim.helper.EditorHelper.getLeadingWhitespace(editor.ij, line)
  }

  override fun anyNonWhitespace(editor: FimEditor, offset: Int, dir: Int): Boolean {
    return com.flop.idea.fim.helper.SearchHelper.anyNonWhitespace(editor.ij, offset, dir)
  }
}
