package com.flop.idea.fim.api

import com.flop.idea.fim.action.motion.leftright.TillCharacterMotionType
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.handler.Motion

interface FimMotionGroup {
  var lastFTCmd: TillCharacterMotionType
  var lastFTChar: Char

  fun getVerticalMotionOffset(editor: FimEditor, caret: FimCaret, count: Int): Int
  fun moveCaretToLineEnd(editor: FimEditor, caret: FimCaret): Int
  fun moveCaretToLineEnd(editor: FimEditor, line: Int, allowPastEnd: Boolean): Int
  fun moveCaretToLineStart(editor: FimEditor, line: Int): Int

  /**
   * This moves the caret to the start of the next/previous word/WORD.
   *
   * @param editor  The editor to move in
   * @param count   The number of words to skip
   * @param bigWord If true then find WORD, if false then find word
   * @return position
   */
  fun findOffsetOfNextWord(editor: FimEditor, searchFrom: Int, count: Int, bigWord: Boolean): Motion
  fun getOffsetOfHorizontalMotion(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    allowPastEnd: Boolean,
  ): Int

  fun moveCaretToLineStartSkipLeading(editor: FimEditor, line: Int): Int
  fun moveCaretToLineStartSkipLeadingOffset(
    editor: FimEditor,
    caret: FimCaret,
    linesOffset: Int,
  ): Int

  fun scrollCaretIntoView(editor: FimEditor)
  fun scrollFullPageDown(editor: FimEditor, caret: FimCaret, pages: Int): Boolean
  fun scrollFullPageUp(editor: FimEditor, caret: FimCaret, pages: Int): Boolean
  fun scrollFullPage(editor: FimEditor, caret: FimCaret, pages: Int): Boolean
  fun moveCaretToMatchingPair(editor: FimEditor, caret: FimCaret): Int
  fun moveCaretToLinePercent(editor: FimEditor, caret: FimCaret, count: Int): Int
  fun moveCaretToLineWithStartOfLineOption(editor: FimEditor, logicalLine: Int, caret: FimCaret): Int

  /**
   * This moves the caret next to the next/previous matching character on the current line
   *
   * @param caret  The caret to be moved
   * @param count  The number of occurrences to move to
   * @param ch     The character to search for
   * @param editor The editor to search in
   * @return True if [count] character matches were found, false if not
   */
  fun moveCaretToBeforeNextCharacterOnLine(editor: FimEditor, caret: FimCaret, count: Int, ch: Char): Int

  /**
   * This moves the caret to the next/previous matching character on the current line
   *
   * @param caret  The caret to be moved
   * @param count  The number of occurrences to move to
   * @param ch     The character to search for
   * @param editor The editor to search in
   * @return True if [count] character matches were found, false if not
   */
  fun moveCaretToNextCharacterOnLine(editor: FimEditor, caret: FimCaret, count: Int, ch: Char): Int
  fun setLastFTCmd(lastFTCmd: TillCharacterMotionType, lastChar: Char)
  fun moveCaretToLineStart(
    editor: FimEditor,
    caret: FimCaret,
  ): Int

  fun moveCaretToLineEndOffset(
    editor: FimEditor,
    caret: FimCaret,
    cntForward: Int,
    allowPastEnd: Boolean,
  ): Int

  fun moveCaretToMiddleColumn(editor: FimEditor, caret: FimCaret): Motion
  fun moveCaretToLineScreenEnd(editor: FimEditor, caret: FimCaret, allowEnd: Boolean): Motion
  fun moveCaretToLineEndSkipLeadingOffset(editor: FimEditor, caret: FimCaret, linesOffset: Int): Int
  fun repeatLastMatchChar(editor: FimEditor, caret: FimCaret, count: Int): Int
  fun moveCaretToLineScreenStartSkipLeading(editor: FimEditor, caret: FimCaret): Int
  fun moveCaretToLineScreenStart(editor: FimEditor, caret: FimCaret): Motion
  fun moveCaretToLineStartSkipLeading(editor: FimEditor, caret: FimCaret): Int
  fun moveCaretToColumn(editor: FimEditor, caret: FimCaret, count: Int, allowEnd: Boolean): Motion
  fun scrollLineToMiddleScreenLine(editor: FimEditor, rawCount: Int, start: Boolean): Boolean
  fun scrollLine(editor: FimEditor, lines: Int): Boolean
  fun scrollLineToLastScreenLine(editor: FimEditor, rawCount: Int, start: Boolean): Boolean
  fun scrollCaretColumnToLastScreenColumn(editor: FimEditor): Boolean
  fun scrollColumns(editor: FimEditor, columns: Int): Boolean
  fun scrollScreen(editor: FimEditor, caret: FimCaret, rawCount: Int, down: Boolean): Boolean
  fun moveCaret(editor: FimEditor, caret: FimCaret, offset: Int)
  fun getMotionRange(editor: FimEditor, caret: FimCaret, context: ExecutionContext, argument: Argument, operatorArguments: OperatorArguments): TextRange?
  fun moveCaretToLineWithSameColumn(editor: FimEditor, logicalLine: Int, caret: FimCaret): Int
  fun scrollLineToFirstScreenLine(editor: FimEditor, rawCount: Int, start: Boolean): Boolean
  fun scrollCaretColumnToFirstScreenColumn(fimEditor: FimEditor): Boolean
  fun moveCaretToFirstScreenLine(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    normalizeToScreen: Boolean
  ): Int

  fun moveCaretToLastScreenLine(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    normalizeToScreen: Boolean
  ): Int

  fun moveCaretToMiddleScreenLine(editor: FimEditor, caret: FimCaret): Int
  fun moveCaretToFileMark(editor: FimEditor, ch: Char, toLineStart: Boolean): Int
  fun moveCaretToMark(editor: FimEditor, ch: Char, toLineStart: Boolean): Int
  fun moveCaretToJump(editor: FimEditor, count: Int): Int
  fun moveCaretGotoNextTab(editor: FimEditor, context: ExecutionContext, rawCount: Int): Int
  fun moveCaretGotoPreviousTab(editor: FimEditor, context: ExecutionContext, rawCount: Int): Int
}
