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

package com.flop.idea.fim.action

import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareToggleAction
import com.flop.idea.fim.helper.MessageHelper

/**
 * This class is used to handle the Fim Plugin enabled/disabled toggle. This is most likely used as a menu option
 * but could also be used as a toolbar item.
 */
class FimPluginToggleAction : DumbAwareToggleAction()/*, LightEditCompatible*/ {
  override fun isSelected(event: AnActionEvent): Boolean = com.flop.idea.fim.FimPlugin.isEnabled()

  override fun setSelected(event: AnActionEvent, b: Boolean) {
    com.flop.idea.fim.FimPlugin.setEnabled(b)
  }

  override fun update(e: AnActionEvent) {
    super.update(e)

    e.presentation.text = if (ActionPlaces.POPUP == e.place) {
      if (com.flop.idea.fim.FimPlugin.isEnabled()) MessageHelper.message("action.FimPluginToggle.enabled") else MessageHelper.message("action.FimPluginToggle.enable")
    } else MessageHelper.message("action.FimPluginToggle.text")
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT
}
