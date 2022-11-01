package com.flop.idea.fim.api

import com.flop.idea.fim.common.TextRange

interface FimClipboardManager {
  /**
   * Returns the string currently on the system clipboard.
   *
   * @return The clipboard string or null if data isn't plain text
   */
  fun getClipboardTextAndTransferableData(): Pair<String, List<Any>?>?

  /**
   * Puts the supplied text into the system clipboard
   */
  fun setClipboardText(text: String, rawText: String = text, transferableData: List<Any>): Any?

  fun getTransferableData(fimEditor: FimEditor, textRange: TextRange, text: String): List<Any>

  fun preprocessText(
    fimEditor: FimEditor,
    textRange: TextRange,
    text: String,
    transferableData: List<*>,
  ): String
}
