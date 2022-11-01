package com.flop.idea.fim.api

import com.flop.idea.fim.common.TextRange
import java.nio.CharBuffer

interface EngineEditorHelper {
  fun normalizeOffset(editor: FimEditor, offset: Int, allowEnd: Boolean): Int
  fun normalizeOffset(editor: FimEditor, line: Int, offset: Int, allowEnd: Boolean): Int
  fun getText(editor: FimEditor, range: TextRange): String
  fun getOffset(editor: FimEditor, line: Int, column: Int): Int
  fun logicalLineToVisualLine(editor: FimEditor, line: Int): Int
  fun normalizeVisualLine(editor: FimEditor, line: Int): Int
  fun normalizeVisualColumn(editor: FimEditor, line: Int, col: Int, allowEnd: Boolean): Int
  fun amountOfInlaysBeforeVisualPosition(editor: FimEditor, pos: FimVisualPosition): Int
  fun getVisualLineCount(editor: FimEditor): Int
  fun prepareLastColumn(caret: FimCaret): Int
  fun updateLastColumn(caret: FimCaret, prevLastColumn: Int)
  fun getLineEndOffset(editor: FimEditor, line: Int, allowEnd: Boolean): Int
  fun getLineStartOffset(editor: FimEditor, line: Int): Int
  fun getLineStartForOffset(editor: FimEditor, line: Int): Int
  fun getLineEndForOffset(editor: FimEditor, offset: Int): Int
  fun visualLineToLogicalLine(editor: FimEditor, line: Int): Int
  fun normalizeLine(editor: FimEditor, line: Int): Int
  fun getVisualLineAtTopOfScreen(editor: FimEditor): Int
  fun getApproximateScreenWidth(editor: FimEditor): Int
  fun handleWithReadonlyFragmentModificationHandler(editor: FimEditor, exception: java.lang.Exception)
  fun getLineBuffer(editor: FimEditor, line: Int): CharBuffer
  fun getVisualLineAtBottomOfScreen(editor: FimEditor): Int
  fun pad(editor: FimEditor, context: ExecutionContext, line: Int, to: Int): String
  fun getLineLength(editor: FimEditor, logicalLine: Int): Int
  fun getLineLength(editor: FimEditor): Int
  fun getLineBreakCount(text: CharSequence): Int
  fun inlayAwareOffsetToVisualPosition(editor: FimEditor, offset: Int): FimVisualPosition
  fun getVisualLineLength(editor: FimEditor, line: Int): Int
  fun getLeadingWhitespace(editor: FimEditor, line: Int): String
  fun anyNonWhitespace(editor: FimEditor, offset: Int, dir: Int): Boolean
}

fun FimEditor.endsWithNewLine(): Boolean {
  val textLength = this.fileSize().toInt()
  if (textLength == 0) return false
  return this.text()[textLength - 1] == '\n'
}
