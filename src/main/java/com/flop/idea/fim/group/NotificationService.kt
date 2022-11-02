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

package com.flop.idea.fim.group

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.actions.OpenFileAction
import com.intellij.ide.actions.RevealFileAction
import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.SystemInfo
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.key.ShortcutOwner
import com.flop.idea.fim.key.ShortcutOwnerInfo
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.statistic.ActionTracker
import com.flop.idea.fim.ui.FimEmulationConfigurable
import com.flop.idea.fim.fimscript.services.IjFimOptionService
import com.flop.idea.fim.fimscript.services.FimRcService
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.KeyStroke

/**
 * @author Alex Plate
 *
 * This service is can be used as application level and as project level service.
 * If project is null, this means that this is an application level service and notification will be shown for all projects
 */
class NotificationService(private val project: Project?) {
  // This constructor is used to create an applicationService
  @Suppress("unused")
  constructor() : this(null)

  fun notifyAboutIdeaPut() {
    val notification = Notification(
      IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE,
      """Add <code>ideaput</code> to <code>clipboard</code> option to perform a put via the IDE<br/><b><code>set clipboard+=ideaput</code></b>""",
      NotificationType.INFORMATION
    )

    notification.addAction(OpenIdeaFimRcAction(notification))

    notification.addAction(
      AppendToIdeaFimRcAction(
        notification,
        "set clipboard+=ideaput",
        "ideaput"
      ) { com.flop.idea.fim.FimPlugin.getOptionService().appendValue(OptionScope.GLOBAL, OptionConstants.clipboardName, OptionConstants.clipboard_ideaput, OptionConstants.clipboardName) }
    )

    notification.notify(project)
  }

  fun notifyAboutIdeaJoin() {
    val notification = Notification(
      IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE,
      """Put <b><code>set ideajoin</code></b> into your <code>~/.ideafimrc</code> to perform a join via the IDE""",
      NotificationType.INFORMATION
    )

    notification.addAction(OpenIdeaFimRcAction(notification))

    notification.addAction(
      AppendToIdeaFimRcAction(
        notification,
        "set ideajoin",
        IjFimOptionService.ideajoinName
      ) { com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, IjFimOptionService.ideajoinName) }
    )

    notification.addAction(HelpLink(ideajoinExamplesUrl))
    notification.notify(project)
  }

  fun enableRepeatingMode() = Messages.showYesNoDialog(
    "Do you want to enable repeating keys in macOS on press and hold?\n\n" +
      "(You can do it manually by running 'defaults write -g " +
      "ApplePressAndHoldEnabled 0' in the console).",
    IDEAFIM_NOTIFICATION_TITLE,
    Messages.getQuestionIcon()
  )

  fun noFimrcAsDefault() {
    val notification = IDEAFIM_STICKY_GROUP.createNotification(
      IDEAFIM_NOTIFICATION_TITLE,
      "The ~/.fimrc file is no longer read by default, use ~/.ideafimrc instead. You can read it from your " +
        "~/.ideafimrc using this command:<br/><br/>" +
        "<code>source ~/.fimrc</code>",
      NotificationType.INFORMATION
    )
    notification.notify(project)
  }

  fun notifyAboutShortcutConflict(keyStroke: KeyStroke) {
    val conflicts = com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts
    val allValuesAreUndefined =
      conflicts.values.all { it is ShortcutOwnerInfo.PerMode || (it is ShortcutOwnerInfo.AllModes && it.owner == ShortcutOwner.UNDEFINED) }
    val shortcutText = KeymapUtil.getShortcutText(KeyboardShortcut(keyStroke, null))
    val message = if (allValuesAreUndefined) {
      "<b>$shortcutText</b> is defined as a shortcut for both Fim and IntelliJ IDEA. It is now used by Fim, but you can change this."
    } else {
      "<b>$shortcutText</b> is used as a Fim command"
    }

    conflicts[keyStroke] = ShortcutOwnerInfo.allFim
    val notification = Notification(
      IDEAFIM_NOTIFICATION_ID,
      IDEAFIM_NOTIFICATION_TITLE,
      message,
      NotificationType.INFORMATION
    )
    notification.addAction(object : DumbAwareAction("Use as IDE Shortcut") {
      override fun actionPerformed(e: AnActionEvent) {
        conflicts[keyStroke] = ShortcutOwnerInfo.allIde
        notification.expire()
      }
    })
    notification.addAction(object : DumbAwareAction("Configure…") {
      override fun actionPerformed(e: AnActionEvent) {
        notification.expire()
        ShowSettingsUtil.getInstance().showSettingsDialog(project, FimEmulationConfigurable::class.java)
      }
    })
    notification.notify(project)
  }

  fun notifySubscribedToEap() {
    Notification(
      IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE,
      """You are successfully subscribed to IdeaFim EAP releases.""",
      NotificationType.INFORMATION
    ).notify(project)
  }

  fun notifyEapFinished() {
    Notification(
      IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE,
      """You have finished the Early Access Program. Please reinstall IdeaFim to get the stable version.""",
      NotificationType.INFORMATION
    ).notify(project)
  }

  fun notifyActionId(id: String?) {
    ActionIdNotifier.notifyActionId(id, project)
  }

  object ActionIdNotifier {
    private var notification: Notification? = null
    private const val NO_ID = "<i>Cannot detect action id</i>"

    fun notifyActionId(id: String?, project: Project?) {

      notification?.expire()

      val content = if (id != null) "Action id: $id" else NO_ID
      Notification(IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE, content, NotificationType.INFORMATION).let {
        notification = it
        it.whenExpired { notification = null }
        it.setContent(it.content + "<br><br><small>Use ${ActionCenter.getToolwindowName()} to see previous ids</small>")

        it.addAction(StopTracking())

        if (id != null) it.addAction(CopyActionId(id, project))

        it.notify(project)
      }

      if (id != null) {
        ActionTracker.logTrackedAction(id)
      }
    }

    class CopyActionId(val id: String?, val project: Project?) : DumbAwareAction(MessageHelper.message("action.copy.action.id.text")) {
      override fun actionPerformed(e: AnActionEvent) {
        CopyPasteManager.getInstance().setContents(StringSelection(id ?: ""))
        if (id != null) {
          ActionTracker.logCopiedAction(id)
        }
        notification?.expire()

        val content = if (id == null) "No action id" else "Action id copied: $id"
        Notification(IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE, content, NotificationType.INFORMATION).let {
          notification = it
          it.whenExpired { notification = null }
          it.addAction(StopTracking())
          it.notify(project)
        }
      }

      override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = id != null
      }

      override fun getActionUpdateThread() = ActionUpdateThread.BGT
    }

    class StopTracking : DumbAwareAction("Stop Tracking") {
      override fun actionPerformed(e: AnActionEvent) {
        com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.trackactionidsName)
        notification?.expire()
      }
    }
  }

  @Suppress("DialogTitleCapitalization")
  class OpenIdeaFimRcAction(private val notification: Notification?) : DumbAwareAction(
    if (FimRcService.findIdeaFimRc() != null) "Open ~/.ideafimrc" else "Create ~/.ideafimrc"
  )/*, LightEditCompatible*/ {
    override fun actionPerformed(e: AnActionEvent) {
      val eventProject = e.project
      if (eventProject != null) {
        val ideaFimRc = FimRcService.findOrCreateIdeaFimRc()
        if (ideaFimRc != null) {
          OpenFileAction.openFile(ideaFimRc.path, eventProject)
          // Do not expire a notification. The user should see what they are entering
          return
        }
      }
      notification?.expire()
      createIdeaFimRcManually(
        "Cannot create configuration file.<br/>Please create <code>~/.ideafimrc</code> manually",
        eventProject
      )
    }

    override fun update(e: AnActionEvent) {
      super.update(e)
      val actionText = if (FimRcService.findIdeaFimRc() != null) "Open ~/.ideafimrc" else "Create ~/.ideafimrc"
      e.presentation.text = actionText
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT
  }

  @Suppress("DialogTitleCapitalization")
  private inner class AppendToIdeaFimRcAction(
    val notification: Notification,
    val appendableText: String,
    val optionName: String,
    val enableOption: () -> Unit,
  ) : AnAction("Append to ~/.ideafimrc") {
    override fun actionPerformed(e: AnActionEvent) {
      val eventProject = e.project
      enableOption()
      if (eventProject != null) {
        val ideaFimRc = FimRcService.findOrCreateIdeaFimRc()
        if (ideaFimRc != null && ideaFimRc.canWrite()) {
          ideaFimRc.appendText(appendableText)
          notification.expire()
          val successNotification = Notification(
            IDEAFIM_NOTIFICATION_ID,
            IDEAFIM_NOTIFICATION_TITLE,
            "<code>$optionName</code> is enabled",
            NotificationType.INFORMATION
          )
          successNotification.addAction(OpenIdeaFimRcAction(successNotification))
          successNotification.notify(project)
          return
        }
      }
      notification.expire()
      createIdeaFimRcManually(
        "Option is enabled, but the file is not modified<br/>Please modify <code>~/.ideafimrc</code> manually",
        project
      )
    }
  }

  private inner class HelpLink(val link: String) : AnAction("", "", AllIcons.Actions.Help) {
    override fun actionPerformed(e: AnActionEvent) {
      BrowserUtil.browse(link)
    }
  }

  companion object {
    val IDEAFIM_STICKY_GROUP: NotificationGroup =
      NotificationGroupManager.getInstance().getNotificationGroup("ideafim-sticky")
    const val IDEAFIM_NOTIFICATION_ID = "ideafim"
    const val IDEAFIM_NOTIFICATION_TITLE = "IdeaFim"
    const val ideajoinExamplesUrl = "https://jb.gg/f9zji9"

    private fun createIdeaFimRcManually(message: String, project: Project?) {
      val notification =
        Notification(IDEAFIM_NOTIFICATION_ID, IDEAFIM_NOTIFICATION_TITLE, message, NotificationType.WARNING)
      var actionName =
        if (SystemInfo.isMac) "Reveal Home in Finder" else "Show Home in " + RevealFileAction.getFileManagerName()
      if (!File(System.getProperty("user.home")).exists()) {
        actionName = ""
      }
      notification.addAction(object : AnAction(actionName) {
        override fun actionPerformed(e: AnActionEvent) {
          val homeDir = File(System.getProperty("user.home"))
          RevealFileAction.openDirectory(homeDir)
          notification.expire()
        }
      })
      notification.notify(project)
    }
  }
}
