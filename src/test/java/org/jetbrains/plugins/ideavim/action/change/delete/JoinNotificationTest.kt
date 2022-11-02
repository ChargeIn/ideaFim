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

@file:Suppress("RemoveCurlyBracesFromTemplate")

package org.jetbrains.plugins.ideafim.action.change.delete

import com.intellij.notification.ActionCenter
import com.intellij.notification.EventLog
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.group.NotificationService
import com.flop.idea.fim.fimscript.services.IjFimOptionService
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

/**
 * @author Alex Plate
 */
class JoinNotificationTest : FimOptionTestCase(IjFimOptionService.ideajoinName) {
  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideajoinName, OptionValueType.NUMBER, "0"))
  fun `test notification shown for no ideajoin`() {
    val before = "I found${c} it\n in a legendary land"
    configureByText(before)
    appReadySetup(false)
    typeText(injector.parser.parseKeys("J"))

    val notification = ActionCenter.getNotifications(myFixture.project, true).last()
    try {
      assertEquals(NotificationService.IDEAFIM_NOTIFICATION_TITLE, notification.title)
      assertTrue(IjFimOptionService.ideajoinName in notification.content)
      assertEquals(3, notification.actions.size)
    } finally {
      notification.expire()
    }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideajoinName, OptionValueType.NUMBER, "1"))
  fun `test notification not shown for ideajoin`() {
    val before = "I found${c} it\n in a legendary land"
    configureByText(before)
    appReadySetup(false)
    typeText(injector.parser.parseKeys("J"))

    val notifications = ActionCenter.getNotifications(myFixture.project, true)
    assertTrue(notifications.isEmpty() || notifications.last().isExpired || IjFimOptionService.ideajoinName !in notifications.last().content)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideajoinName, OptionValueType.NUMBER, "0"))
  fun `test notification not shown if was shown already`() {
    val before = "I found${c} it\n in a legendary land"
    configureByText(before)
    appReadySetup(true)
    typeText(injector.parser.parseKeys("J"))

    val notifications = EventLog.getLogModel(myFixture.project).notifications
    assertTrue(notifications.isEmpty() || notifications.last().isExpired || IjFimOptionService.ideajoinName !in notifications.last().content)
  }

  private fun appReadySetup(notifierEnabled: Boolean) {
    EventLog.markAllAsRead(myFixture.project)
    com.flop.idea.fim.FimPlugin.getFimState().isIdeaJoinNotified = notifierEnabled
  }
}
