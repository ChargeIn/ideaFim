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

import com.intellij.openapi.components.Service
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimSearchHelperBase
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.SearchOptions
import java.util.*

@Service
class IjFimSearchHelper : FimSearchHelperBase() {
  override fun findNextParagraph(editor: FimEditor, caret: FimCaret, count: Int, allowBlanks: Boolean): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextParagraph(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
      allowBlanks
    )
  }

  override fun findNextSentenceStart(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    countCurrent: Boolean,
    requireAll: Boolean,
  ): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextSentenceStart(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count, countCurrent, requireAll
    )
  }

  override fun findSection(editor: FimEditor, caret: FimCaret, type: Char, dir: Int, count: Int): Int {
    return com.flop.idea.fim.helper.SearchHelper.findSection(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      type,
      dir,
      count,
    )
  }

  override fun findNextCamelEnd(editor: FimEditor, caret: FimCaret, count: Int): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextCamelEnd(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
    )
  }

  override fun findNextSentenceEnd(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    countCurrent: Boolean,
    requireAll: Boolean,
  ): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextSentenceEnd(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
      countCurrent,
      requireAll,
    )
  }

  override fun findNextCamelStart(editor: FimEditor, caret: FimCaret, count: Int): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextCamelStart(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
    )
  }

  override fun findMethodEnd(editor: FimEditor, caret: FimCaret, count: Int): Int {
    return com.flop.idea.fim.helper.SearchHelper.findMethodEnd(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
    )
  }

  override fun findMethodStart(editor: FimEditor, caret: FimCaret, count: Int): Int {
    return com.flop.idea.fim.helper.SearchHelper.findMethodStart(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
    )
  }

  override fun findUnmatchedBlock(editor: FimEditor, caret: FimCaret, type: Char, count: Int): Int {
    return com.flop.idea.fim.helper.SearchHelper.findUnmatchedBlock(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      type,
      count,
    )
  }

  override fun findNextWordEnd(editor: FimEditor, caret: FimCaret, count: Int, bigWord: Boolean): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextWordEnd(
      (editor as IjFimEditor).editor,
      (caret as IjFimCaret).caret,
      count,
      bigWord,
    )
  }

  override fun findNextWordEnd(
    chars: CharSequence,
    pos: Int,
    size: Int,
    count: Int,
    bigWord: Boolean,
    spaceWords: Boolean,
  ): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextWordEnd(chars, pos, size, count, bigWord, spaceWords)
  }

  override fun findPattern(
    editor: FimEditor,
    pattern: String?,
    startOffset: Int,
    count: Int,
    searchOptions: EnumSet<SearchOptions>?,
  ): TextRange? {
    return com.flop.idea.fim.helper.SearchHelper.findPattern(editor.ij, pattern, startOffset, count, searchOptions)
  }

  override fun findNextCharacterOnLine(editor: FimEditor, caret: FimCaret, count: Int, ch: Char): Int {
    return com.flop.idea.fim.helper.SearchHelper.findNextCharacterOnLine(editor.ij, caret.ij, count, ch)
  }

  override fun findWordUnderCursor(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    dir: Int,
    isOuter: Boolean,
    isBig: Boolean,
    hasSelection: Boolean,
  ): TextRange {
    return com.flop.idea.fim.helper.SearchHelper.findWordUnderCursor(editor.ij, caret.ij, count, dir, isOuter, isBig, hasSelection)
  }

  override fun findSentenceRange(editor: FimEditor, caret: FimCaret, count: Int, isOuter: Boolean): TextRange {
    return com.flop.idea.fim.helper.SearchHelper.findSentenceRange(editor.ij, caret.ij, count, isOuter)
  }

  override fun findParagraphRange(editor: FimEditor, caret: FimCaret, count: Int, isOuter: Boolean): TextRange? {
    return com.flop.idea.fim.helper.SearchHelper.findParagraphRange(editor.ij, caret.ij, count, isOuter)
  }

  override fun findBlockTagRange(editor: FimEditor, caret: FimCaret, count: Int, isOuter: Boolean): TextRange? {
    return com.flop.idea.fim.helper.SearchHelper.findBlockTagRange(editor.ij, caret.ij, count, isOuter)
  }

  override fun findBlockQuoteInLineRange(
    editor: FimEditor,
    caret: FimCaret,
    quote: Char,
    isOuter: Boolean,
  ): TextRange? {
    return com.flop.idea.fim.helper.SearchHelper.findBlockQuoteInLineRange(editor.ij, caret.ij, quote, isOuter)
  }

  override fun findBlockRange(
    editor: FimEditor,
    caret: FimCaret,
    type: Char,
    count: Int,
    isOuter: Boolean,
  ): TextRange? {
    return com.flop.idea.fim.helper.SearchHelper.findBlockRange(editor.ij, caret.ij, type, count, isOuter)
  }
}
