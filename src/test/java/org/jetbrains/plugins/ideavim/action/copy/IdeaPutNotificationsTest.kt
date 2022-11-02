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

package org.jetbrains.plugins.ideafim.action.copy

import com.intellij.notification.ActionCenter
import com.intellij.notification.EventLog
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.group.NotificationService
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption
import org.jetbrains.plugins.ideafim.rangeOf

/**
 * @author Alex Plate
 */
class IdeaPutNotificationsTest : FimOptionTestCase(OptionConstants.clipboardName) {
  @FimOptionTestConfiguration(FimTestOption(OptionConstants.clipboardName, OptionValueType.STRING, ""))
  fun `test notification exists if no ideaput`() {
    val before = "${c}I found it in a legendary land"
    configureByText(before)
    appReadySetup(false)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(myFixture.editor.fim, before rangeOf "legendary", SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("p"))

    val notification = ActionCenter.getNotifications(myFixture.project, true).last()
    try {
      assertEquals(NotificationService.IDEAFIM_NOTIFICATION_TITLE, notification.title)
      assertTrue(OptionConstants.clipboard_ideaput in notification.content)
      assertEquals(2, notification.actions.size)
    } finally {
      notification.expire()
    }
  }

  @FimOptionTestConfiguration(
    FimTestOption(
      OptionConstants.clipboardName,
      OptionValueType.STRING,
      OptionConstants.clipboard_ideaput
    )
  )
  fun `test no notification on ideaput`() {
    val before = "${c}I found it in a legendary land"
    configureByText(before)
    appReadySetup(false)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(myFixture.editor.fim, before rangeOf "legendary", SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("p"))

    val notifications = ActionCenter.getNotifications(myFixture.project, true)
    assertTrue(notifications.isEmpty() || notifications.last().isExpired || OptionConstants.clipboard_ideaput !in notifications.last().content)
  }

  @FimOptionTestConfiguration(FimTestOption(OptionConstants.clipboardName, OptionValueType.STRING, ""))
  fun `test no notification if already was`() {
    val before = "${c}I found it in a legendary land"
    configureByText(before)
    appReadySetup(true)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(myFixture.editor.fim, before rangeOf "legendary", SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("p"))

    val notifications = EventLog.getLogModel(myFixture.project).notifications
    assertTrue(notifications.isEmpty() || notifications.last().isExpired || OptionConstants.clipboard_ideaput !in notifications.last().content)
  }

  private fun appReadySetup(notifierEnabled: Boolean) {
    EventLog.markAllAsRead(myFixture.project)
    com.flop.idea.fim.FimPlugin.getFimState().isIdeaPutNotified = notifierEnabled
  }
}
