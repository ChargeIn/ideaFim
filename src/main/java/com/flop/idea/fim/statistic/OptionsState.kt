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
import com.intellij.internal.statistic.eventLog.events.BooleanEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.EventPair
import com.intellij.internal.statistic.eventLog.events.StringEventField
import com.intellij.internal.statistic.eventLog.events.VarargEventId
import com.intellij.internal.statistic.service.fus.collectors.ApplicationUsagesCollector
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.services.IjFimOptionService

internal class OptionsState : ApplicationUsagesCollector() {

  override fun getGroup(): EventLogGroup = GROUP

  override fun getMetrics(): Set<MetricEvent> {
    val optionService = com.flop.idea.fim.FimPlugin.getOptionService()

    return setOf(
      OPTIONS.metric(
        IDEAJOIN withOption IjFimOptionService.ideajoinName,
        IDEAMARKS withOption IjFimOptionService.ideamarksName,
        IDEAREFACTOR withOption IjFimOptionService.idearefactormodeName,
        IDEAPUT with optionService.contains(OptionScope.GLOBAL, OptionConstants.clipboardName, OptionConstants.clipboard_ideaput),
        IDEASTATUSICON withOption IjFimOptionService.ideastatusiconName,
        IDEAWRITE withOption IjFimOptionService.ideawriteName,
        IDEASELECTION with optionService.contains(OptionScope.GLOBAL, OptionConstants.selectmodeName, "ideaselection"),
        IDEAVIMSUPPORT with optionService.getValues(OptionScope.GLOBAL, IjFimOptionService.ideafimsupportName)!!
      )
    )
  }

  private infix fun BooleanEventField.withOption(name: String): EventPair<Boolean> {
    return this.with(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, name))
  }

  private infix fun StringEventField.withOption(name: String): EventPair<String?> {
    return this.with(com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, name).asString())
  }

  companion object {
    private val GROUP = EventLogGroup("fim.options", 1)

    private val IDEAJOIN = BooleanEventField(IjFimOptionService.ideajoinName)
    private val IDEAMARKS = BooleanEventField(IjFimOptionService.ideamarksName)
    private val IDEAREFACTOR = EventFields.String(IjFimOptionService.ideamarksName, IjFimOptionService.ideaRefactorModeValues.toList())
    private val IDEAPUT = BooleanEventField("ideaput")
    private val IDEASTATUSICON = EventFields.String(IjFimOptionService.ideastatusiconName, IjFimOptionService.ideaStatusIconValues.toList())
    private val IDEAWRITE = EventFields.String(IjFimOptionService.ideawriteName, IjFimOptionService.ideaWriteValues.toList())
    private val IDEASELECTION = BooleanEventField("ideaselection")
    private val IDEAVIMSUPPORT = EventFields.StringList(IjFimOptionService.ideafimsupportName, IjFimOptionService.ideafimsupportValues.toList())

    private val OPTIONS: VarargEventId = GROUP.registerVarargEvent(
      "fim.options",
      IDEAJOIN,
      IDEAMARKS,
      IDEAREFACTOR,
      IDEAPUT,
      IDEASTATUSICON,
      IDEAWRITE,
      IDEASELECTION,
      IDEAVIMSUPPORT,
    )
  }
}
