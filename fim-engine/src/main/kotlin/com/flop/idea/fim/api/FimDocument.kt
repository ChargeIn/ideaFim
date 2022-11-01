package com.flop.idea.fim.api

import com.flop.idea.fim.common.ChangesListener
import com.flop.idea.fim.common.LiveRange
import com.flop.idea.fim.common.Offset

interface FimDocument {
  fun addChangeListener(listener: ChangesListener)
  fun removeChangeListener(listener: ChangesListener)
  fun getOffsetGuard(offset: Offset): LiveRange?
}
