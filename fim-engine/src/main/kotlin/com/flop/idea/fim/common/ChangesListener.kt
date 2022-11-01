package com.flop.idea.fim.common

interface ChangesListener {
  fun documentChanged(change: Change)

  class Change(val oldFragment: String, val newFragment: String, val offset: Int)
}
