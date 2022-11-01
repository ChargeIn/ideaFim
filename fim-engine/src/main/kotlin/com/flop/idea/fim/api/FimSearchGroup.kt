package com.flop.idea.fim.api

import com.flop.idea.fim.common.Direction
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.ex.ranges.LineRange
import com.flop.idea.fim.regexp.CharPointer
import com.flop.idea.fim.fimscript.model.FimLContext

interface FimSearchGroup {
  var lastSearchPattern: String?
  var lastSubstitutePattern: String?
  fun findUnderCaret(editor: FimEditor): TextRange?
  fun searchBackward(editor: FimEditor, offset: Int, count: Int): TextRange?
  fun getNextSearchRange(editor: FimEditor, count: Int, forwards: Boolean): TextRange?
  fun processSearchRange(
    editor: FimEditor,
    pattern: String,
    patternOffset: Int,
    startOffset: Int,
    direction: Direction,
  ): Int

  fun searchNext(editor: FimEditor, caret: FimCaret, count: Int): Int
  fun searchPrevious(editor: FimEditor, caret: FimCaret, count: Int): Int
  fun processSearchCommand(editor: FimEditor, command: String, startOffset: Int, dir: Direction): Int
  fun searchWord(editor: FimEditor, caret: FimCaret, count: Int, whole: Boolean, dir: Direction): Int
  fun processSubstituteCommand(
    editor: FimEditor,
    caret: FimCaret,
    range: LineRange,
    excmd: String,
    exarg: String,
    parent: FimLContext,
  ): Boolean
  // TODO rewrite this
  fun search_regcomp(pat: CharPointer?, which_pat: Int, patSave: Int): Pair<Boolean, Triple<Any, String, Any>>
  fun findDecimalNumber(line: String): Int?
  fun clearSearchHighlight()

  // Matching the values defined in Fim. Do not change these values, they are used as indexes
  companion object {
    val RE_SEARCH = 0 // Save/use search pattern

    val RE_SUBST = 1 // Save/use substitute pattern

    val RE_BOTH = 2 // Save to both patterns

    val RE_LAST = 2 // Use last used pattern if "pat" is NULL
  }
}
