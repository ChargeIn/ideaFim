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

package com.flop.idea.fim.ui

import com.intellij.ide.BrowserUtil
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.openapi.util.NlsActions
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.Consumer
import com.flop.idea.fim.group.NotificationService
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.options.OptionChangeListener
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.services.IjFimOptionService
import org.jetbrains.annotations.NonNls
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.SwingConstants

@NonNls
const val STATUS_BAR_ICON_ID = "IdeaFim-Icon"
const val STATUS_BAR_DISPLAY_NAME = "IdeaFim"

class StatusBarIconFactory : StatusBarWidgetFactory/*, LightEditCompatible*/ {

  override fun getId(): String = STATUS_BAR_ICON_ID

  override fun getDisplayName(): String = STATUS_BAR_DISPLAY_NAME

  override fun disposeWidget(widget: StatusBarWidget) {
    // Nothing
  }

  override fun isAvailable(project: Project): Boolean {
    val ideaStatusIconValue = (com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, IjFimOptionService.ideastatusiconName) as FimString).value
    return ideaStatusIconValue != IjFimOptionService.ideastatusicon_disabled
  }

  override fun createWidget(project: Project): StatusBarWidget {
    com.flop.idea.fim.FimPlugin.getOptionService().addListener(
      IjFimOptionService.ideastatusiconName,
      object : OptionChangeListener<FimDataType> {
        override fun processGlobalValueChange(oldValue: FimDataType?) {
          updateAll()
        }
      }
    )
    return FimStatusBar()
  }

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

  /* Use can configure this icon using ideastatusicon option, but we should still keep the option to remove
  * the icon via IJ because this option is hard to discover */
  override fun isConfigurable(): Boolean = true

  private fun updateAll() {
    val projectManager = ProjectManager.getInstanceIfCreated() ?: return
    for (project in projectManager.openProjects) {
      val statusBarWidgetsManager = project.getService(StatusBarWidgetsManager::class.java) ?: continue
      statusBarWidgetsManager.updateWidget(this)
    }

    updateIcon()
  }

  companion object {
    fun updateIcon() {
      val projectManager = ProjectManager.getInstanceIfCreated() ?: return
      for (project in projectManager.openProjects) {
        val statusBar = WindowManager.getInstance().getStatusBar(project) ?: continue
        statusBar.updateWidget(STATUS_BAR_ICON_ID)
      }
    }
  }
}

class FimStatusBar : StatusBarWidget, StatusBarWidget.IconPresentation {

  override fun ID(): String = STATUS_BAR_ICON_ID

  override fun install(statusBar: StatusBar) {
    // Nothing
  }

  override fun dispose() {
    // Nothing
  }

  override fun getTooltipText() = STATUS_BAR_DISPLAY_NAME

  override fun getIcon(): Icon {
    val ideaStatusIconValue = (com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, IjFimOptionService.ideastatusiconName) as FimString).value
    if (ideaStatusIconValue == IjFimOptionService.ideastatusicon_gray) return com.flop.idea.fim.icons.FimIcons.IDEAFIM_DISABLED
    return if (com.flop.idea.fim.FimPlugin.isEnabled()) com.flop.idea.fim.icons.FimIcons.IDEAFIM else com.flop.idea.fim.icons.FimIcons.IDEAFIM_DISABLED
  }

  override fun getClickConsumer() = Consumer<MouseEvent> { event ->
    val component = event.component
    val popup = FimActionsPopup.getPopup(DataManager.getInstance().getDataContext(component))
    val dimension = popup.content.preferredSize

    val at = Point(0, -dimension.height)
    popup.show(RelativePoint(component, at))
  }

  override fun getPresentation(): StatusBarWidget.WidgetPresentation = this
}

class FimActions : DumbAwareAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    FimActionsPopup.getPopup(e.dataContext).showCenteredInCurrentWindow(project)
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    e.presentation.isEnabledAndVisible = project != null && !project.isDisposed
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT
}

private object FimActionsPopup {
  fun getPopup(dataContext: DataContext): ListPopup {
    val actions = getActions()
    val popup = JBPopupFactory.getInstance()
      .createActionGroupPopup(
        STATUS_BAR_DISPLAY_NAME, actions,
        dataContext, JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false,
        ActionPlaces.POPUP
      )
    popup.setAdText(MessageHelper.message("popup.advertisement.version", com.flop.idea.fim.FimPlugin.getVersion()), SwingConstants.CENTER)

    return popup
  }

  private fun getActions(): DefaultActionGroup {
    val actionGroup = DefaultActionGroup()
    actionGroup.isPopup = true

    actionGroup.add(ActionManager.getInstance().getAction("FimPluginToggle"))
    actionGroup.addSeparator()
    actionGroup.add(NotificationService.OpenIdeaFimRcAction(null))
    actionGroup.add(ShortcutConflictsSettings)
    actionGroup.addSeparator()
    return actionGroup
  }
}

private class HelpLink(
  @NlsActions.ActionText name: String,
  val link: String,
  icon: Icon?,
) : DumbAwareAction(name, null, icon)/*, LightEditCompatible*/ {
  override fun actionPerformed(e: AnActionEvent) {
    BrowserUtil.browse(link)
  }
}

private object ShortcutConflictsSettings : DumbAwareAction(MessageHelper.message("action.settings.text"))/*, LightEditCompatible*/ {
  override fun actionPerformed(e: AnActionEvent) {
    ShowSettingsUtil.getInstance().showSettingsDialog(e.project, FimEmulationConfigurable::class.java)
  }
}

internal object JoinEap : DumbAwareAction()/*, LightEditCompatible*/ {
  private const val EAP_LINK = "https://plugins.jetbrains.com/plugins/eap/ideafim"

  fun eapActive() = EAP_LINK in UpdateSettings.getInstance().storedPluginHosts

  override fun actionPerformed(e: AnActionEvent) {
    if (eapActive()) {
      UpdateSettings.getInstance().storedPluginHosts -= EAP_LINK
      com.flop.idea.fim.FimPlugin.getNotifications(e.project).notifyEapFinished()
    } else {
      UpdateSettings.getInstance().storedPluginHosts += EAP_LINK
      com.flop.idea.fim.FimPlugin.getNotifications(e.project).notifySubscribedToEap()
    }
  }

  override fun update(e: AnActionEvent) {
    if (eapActive()) {
      e.presentation.text = MessageHelper.message("action.finish.eap.text")
    } else {
      e.presentation.text = MessageHelper.message("action.subscribe.to.eap.text")
    }
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
