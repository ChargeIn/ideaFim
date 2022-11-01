package com.flop.idea.fim.history

interface FimHistory {
  fun addEntry(key: String, text: String)
  fun getEntries(key: String, first: Int, last: Int): List<HistoryEntry>
}
