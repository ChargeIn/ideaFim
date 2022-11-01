package com.flop.idea.fim.api

interface FimSelectionModel {
  val selectionStart: Int
  val selectionEnd: Int

  fun hasSelection(): Boolean
}
