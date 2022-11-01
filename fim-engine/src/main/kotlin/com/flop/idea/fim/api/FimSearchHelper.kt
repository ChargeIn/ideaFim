package com.flop.idea.fim.api

import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.SearchOptions
import java.util.*

interface FimSearchHelper {
  fun findNextParagraph(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    allowBlanks: Boolean,
  ): Int

  fun findNextSentenceStart(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    countCurrent: Boolean,
    requireAll: Boolean,
  ): Int

  fun findSection(
    editor: FimEditor,
    caret: FimCaret,
    type: Char,
    dir: Int,
    count: Int,
  ): Int

  fun findNextCamelEnd(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
  ): Int

  fun findNextSentenceEnd(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    countCurrent: Boolean,
    requireAll: Boolean,
  ): Int

  fun findNextCamelStart(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
  ): Int

  fun findMethodEnd(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
  ): Int

  fun findMethodStart(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
  ): Int

  fun findUnmatchedBlock(
    editor: FimEditor,
    caret: FimCaret,
    type: Char,
    count: Int,
  ): Int

  fun findNextWordEnd(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    bigWord: Boolean,
  ): Int

  fun findNextWordEnd(
    chars: CharSequence,
    pos: Int,
    size: Int,
    count: Int,
    bigWord: Boolean,
    spaceWords: Boolean,
  ): Int

  fun findNextWord(editor: FimEditor, searchFrom: Int, count: Int, bigWord: Boolean): Long

  fun findPattern(
    editor: FimEditor,
    pattern: String?,
    startOffset: Int,
    count: Int,
    searchOptions: EnumSet<SearchOptions>?,
  ): TextRange?

  fun findNextCharacterOnLine(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    ch: Char,
  ): Int

  fun findWordUnderCursor(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    dir: Int,
    isOuter: Boolean,
    isBig: Boolean,
    hasSelection: Boolean,
  ): TextRange

  fun findSentenceRange(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    isOuter: Boolean,
  ): TextRange

  fun findParagraphRange(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    isOuter: Boolean,
  ): TextRange?

  fun findBlockTagRange(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    isOuter: Boolean,
  ): TextRange?

  fun findBlockQuoteInLineRange(
    editor: FimEditor,
    caret: FimCaret,
    quote: Char,
    isOuter: Boolean,
  ): TextRange?

  fun findBlockRange(
    editor: FimEditor,
    caret: FimCaret,
    type: Char,
    count: Int,
    isOuter: Boolean,
  ): TextRange?
}
