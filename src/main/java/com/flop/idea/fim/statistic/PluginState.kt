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
import com.intellij.internal.statistic.eventLog.events.VarargEventId
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.ui.JoinEap

internal class PluginState : ApplicationUsagesCollector() {

  override fun getGroup(): EventLogGroup = GROUP

  override fun getMetrics(): Set<MetricEvent> {
    return setOf(
      PLUGIN_STATE.metric(
        PLUGIN_ENABLED with com.flop.idea.fim.FimPlugin.isEnabled(),
        IS_EAP with JoinEap.eapActive(),
        ENABLED_EXTENSIONS with enabledExtensions.toList(),
      )
    )
  }

  companion object {
    private val GROUP = EventLogGroup("fim.common", 1)

    val extensionNames = listOf("textobj-entire", "argtextobj", "ReplaceWithRegister", "fim-paragraph-motion", "highlightedyank", "multiple-cursors", "exchange", "NERDTree", "surround", "commentary", "matchit", "textobj-indent")
    val enabledExtensions = HashSet<String>()

    private val PLUGIN_ENABLED = EventFields.Boolean("is_plugin_enabled")
    private val IS_EAP = EventFields.Boolean("is_EAP_active")
    private val ENABLED_EXTENSIONS = EventFields.StringList("enabled_extensions", extensionNames)

    private val PLUGIN_STATE: VarargEventId = GROUP.registerVarargEvent(
      "fim.common",
      PLUGIN_ENABLED,
      IS_EAP,
      ENABLED_EXTENSIONS,
    )
  }
}
