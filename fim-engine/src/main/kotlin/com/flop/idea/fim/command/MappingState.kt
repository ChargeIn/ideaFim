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

package com.flop.idea.fim.command

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.diagnostic.trace
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import java.awt.event.ActionListener
import javax.swing.KeyStroke
import javax.swing.Timer

class MappingState {
  // Map command depth. 0 - if it is not a map command. 1 - regular map command. 2+ - nested map commands
  private var mapDepth = 0

  fun isExecutingMap(): Boolean {
    return mapDepth > 0
  }

  fun startMapExecution() {
    ++mapDepth
  }

  fun stopMapExecution() {
    --mapDepth
  }

  val keys: Iterable<KeyStroke>
    get() = keyList

  var mappingMode = MappingMode.NORMAL

  private val timer = Timer((injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.timeoutlenName) as FimInt).value, null)
  private var keyList = mutableListOf<KeyStroke>()

  init {
    timer.isRepeats = false
  }

  fun startMappingTimer(actionListener: ActionListener) {
    timer.initialDelay = (injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.timeoutlenName) as FimInt).value
    timer.actionListeners.forEach { timer.removeActionListener(it) }
    timer.addActionListener(actionListener)
    timer.start()
  }

  fun stopMappingTimer() {
    LOG.trace { "Stop mapping timer" }
    timer.stop()
    timer.actionListeners.forEach { timer.removeActionListener(it) }
  }

  fun addKey(key: KeyStroke) {
    keyList.add(key)
  }

  fun detachKeys(): List<KeyStroke> {
    val currentKeys = keyList
    keyList = mutableListOf()
    return currentKeys
  }

  fun resetMappingSequence() {
    LOG.trace("Reset mapping sequence")
    stopMappingTimer()
    keyList.clear()
    // NOTE: We intentionally don't reset mapping mode here
  }

  companion object {
    private val LOG = fimLogger<MappingState>()
  }
}
