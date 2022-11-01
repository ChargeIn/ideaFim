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

package com.flop.idea.fim.statistic

import com.intellij.internal.statistic.beans.MetricEvent
import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.key.ShortcutOwner
import com.flop.idea.fim.key.ShortcutOwnerInfo
import java.awt.event.InputEvent
import java.awt.event.InputEvent.CTRL_DOWN_MASK
import java.awt.event.InputEvent.SHIFT_DOWN_MASK
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

internal class ShortcutConflictState : ApplicationUsagesCollector() {

  override fun getGroup(): EventLogGroup = GROUP

  override fun getMetrics(): Set<MetricEvent> {
    val metrics = mutableSetOf<MetricEvent>()
    keyStrokes.forEach { keystroke ->
      getHandlersForShortcut(keystroke)
        .filter { !setOf(HandledModes.INSERT_UNDEFINED, HandledModes.NORMAL_UNDEFINED, HandledModes.VISUAL_AND_SELECT_UNDEFINED).contains(it) }
        .forEach { mode ->
          metrics += HANDLER.metric(keystroke.toReadableString(), mode)
        }
    }
    return metrics
  }

  private fun getHandlersForShortcut(shortcut: KeyStroke): List<HandledModes> {
    val modes = com.flop.idea.fim.FimPlugin.getKey().shortcutConflicts[shortcut] ?: return listOf(HandledModes.NORMAL_UNDEFINED, HandledModes.INSERT_UNDEFINED, HandledModes.VISUAL_AND_SELECT_UNDEFINED)

    return when (modes) {
      is ShortcutOwnerInfo.AllModes -> {
        when (modes.owner) {
          ShortcutOwner.IDE -> listOf(HandledModes.NORMAL_IDE, HandledModes.INSERT_IDE, HandledModes.VISUAL_AND_SELECT_IDE)
          ShortcutOwner.VIM -> listOf(HandledModes.NORMAL_VIM, HandledModes.INSERT_VIM, HandledModes.VISUAL_AND_SELECT_VIM)
          ShortcutOwner.UNDEFINED -> listOf(HandledModes.NORMAL_UNDEFINED, HandledModes.INSERT_UNDEFINED, HandledModes.VISUAL_AND_SELECT_UNDEFINED)
        }
      }

      is ShortcutOwnerInfo.PerMode -> {
        val result = mutableListOf<HandledModes>()
        when (modes.normal) {
          ShortcutOwner.IDE -> result.add(HandledModes.NORMAL_IDE)
          ShortcutOwner.VIM -> result.add(HandledModes.NORMAL_VIM)
          ShortcutOwner.UNDEFINED -> result.add(HandledModes.NORMAL_UNDEFINED)
        }
        when (modes.insert) {
          ShortcutOwner.IDE -> result.add(HandledModes.INSERT_IDE)
          ShortcutOwner.VIM -> result.add(HandledModes.INSERT_VIM)
          ShortcutOwner.UNDEFINED -> result.add(HandledModes.INSERT_UNDEFINED)
        }
        when (modes.visual) {
          ShortcutOwner.IDE -> result.add(HandledModes.VISUAL_AND_SELECT_IDE)
          ShortcutOwner.VIM -> result.add(HandledModes.VISUAL_AND_SELECT_VIM)
          ShortcutOwner.UNDEFINED -> result.add(HandledModes.VISUAL_AND_SELECT_UNDEFINED)
        }
        result
      }
    }
  }

  companion object {
    private val GROUP = EventLogGroup("fim.handlers", 1)

    private val keyStrokes = listOf(
      KeyStroke.getKeyStroke('1'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('2'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('3'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('4'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('5'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('6'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('7'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('8'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('9'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('0'.code, CTRL_DOWN_MASK),

      KeyStroke.getKeyStroke('1'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('2'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('3'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('4'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('5'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('6'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('7'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('8'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('9'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('0'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),

      KeyStroke.getKeyStroke('A'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('B'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('C'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('D'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('E'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('F'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('G'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('H'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('I'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('J'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('K'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('L'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('M'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('N'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('O'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('P'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('Q'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('R'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('S'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('T'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('U'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('V'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('W'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('X'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('Y'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('Z'.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke('['.code, CTRL_DOWN_MASK),
      KeyStroke.getKeyStroke(']'.code, CTRL_DOWN_MASK),

      KeyStroke.getKeyStroke('A'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('B'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('C'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('D'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('E'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('F'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('G'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('H'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('I'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('J'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('K'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('L'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('M'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('N'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('O'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('P'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('Q'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('R'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('S'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('T'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('U'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('V'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('W'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('X'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('Y'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('Z'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke('['.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
      KeyStroke.getKeyStroke(']'.code, CTRL_DOWN_MASK + SHIFT_DOWN_MASK),
    )
    private val KEY_STROKE = EventFields.String("key_stroke", keyStrokes.map { it.toReadableString() })
    private val HANDLER_MODE = EventFields.Enum<HandledModes>("handler")
    private val HANDLER = GROUP.registerEvent("fim.handler", KEY_STROKE, HANDLER_MODE)
  }
}

private fun KeyStroke.toReadableString(): String {
  val result = StringBuilder()
  val modifiers = this.modifiers
  if (modifiers > 0) {
    result.append(InputEvent.getModifiersExText(modifiers))
      .append("+")
  }
  result.append(KeyEvent.getKeyText(this.keyCode))
  return result.toString()
}

private enum class HandledModes {
  NORMAL_UNDEFINED,
  NORMAL_IDE,
  NORMAL_VIM,
  INSERT_UNDEFINED,
  INSERT_IDE,
  INSERT_VIM,
  VISUAL_AND_SELECT_UNDEFINED,
  VISUAL_AND_SELECT_IDE,
  VISUAL_AND_SELECT_VIM,
}
