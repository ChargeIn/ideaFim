package com.flop.idea.fim.history

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt

class HistoryBlock {
  private val entries: MutableList<HistoryEntry> = ArrayList()

  private var counter = 0

  fun addEntry(text: String) {
    for (i in entries.indices) {
      val entry = entries[i]
      if (text == entry.entry) {
        entries.removeAt(i)
        break
      }
    }
    entries.add(HistoryEntry(++counter, text))
    if (entries.size > maxLength()) {
      entries.removeAt(0)
    }
  }

  fun getEntries(): List<HistoryEntry> {
    return entries
  }

  companion object {
    private fun maxLength(): Int {
      return (
        injector.optionService
          .getOptionValue(
            OptionScope.GLOBAL, OptionConstants.historyName,
            OptionConstants.historyName
          ) as FimInt
        ).value
    }
  }
}
