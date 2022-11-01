package com.flop.idea.fim.history

import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger

open class FimHistoryBase : FimHistory {
  val histories: MutableMap<String, HistoryBlock> = HashMap()

  override fun addEntry(key: String, text: String) {
    logger.debug { "Add entry '$text' to $key" }

    val block = blocks(key)
    block.addEntry(text)
  }

  override fun getEntries(key: String, first: Int, last: Int): List<HistoryEntry> {
    var myFirst = first
    var myLast = last
    val block = blocks(key)

    val entries = block.getEntries()
    val res = ArrayList<HistoryEntry>()
    if (myFirst < 0) {
      myFirst = if (-myFirst > entries.size) {
        Integer.MAX_VALUE
      } else {
        val entry = entries[entries.size + myFirst]
        entry.number
      }
    }
    if (myLast < 0) {
      myLast = if (-myLast > entries.size) {
        Integer.MIN_VALUE
      } else {
        val entry = entries[entries.size + myLast]
        entry.number
      }
    } else if (myLast == 0) {
      myLast = Integer.MAX_VALUE
    }

    logger.debug { "first=$myFirst\nlast=$myLast" }

    for (entry in entries) {
      if (entry.number in myFirst..myLast) {
        res.add(entry)
      }
    }

    return res
  }

  private fun blocks(key: String): HistoryBlock {
    return histories.getOrPut(key) { HistoryBlock() }
  }

  companion object {
    val logger = fimLogger<FimHistoryBase>()
  }
}
