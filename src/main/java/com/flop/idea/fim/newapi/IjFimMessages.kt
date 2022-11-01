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

package com.flop.idea.fim.newapi

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.flop.idea.fim.api.FimMessagesBase
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.ui.ShowCmd
import java.awt.Toolkit

@Service
class IjFimMessages : FimMessagesBase() {

  private var message: String? = null
  private var error = false
  private var lastBeepTimeMillis = 0L

  override fun showStatusBarMessage(message: String?) {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      this.message = message
    }
    val pm = ProjectManager.getInstance()
    val projects = pm.openProjects
    for (project in projects) {
      val bar = WindowManager.getInstance().getStatusBar(project)
      if (bar != null) {
        if (message.isNullOrEmpty()) {
          bar.info = ""
        } else {
          bar.info = "VIM - $message"
        }
      }
    }
  }

  override fun getStatusBarMessage(): String? = message

  override fun indicateError() {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      error = true
    } else if (!injector.optionService.isSet(
        OptionScope.GLOBAL,
        OptionConstants.visualbellName,
        OptionConstants.visualbellName
      )
    ) {
      // Fim only allows a beep once every half second - :help 'visualbell'
      val currentTimeMillis = System.currentTimeMillis()
      if (currentTimeMillis - lastBeepTimeMillis > 500) {
        Toolkit.getDefaultToolkit().beep()
        lastBeepTimeMillis = currentTimeMillis
      }
    }
  }

  override fun clearError() {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      error = false
    }
  }

  override fun isError(): Boolean = error

  override fun message(key: String, vararg params: Any): String = MessageHelper.message(key, *params)
  override fun updateStatusBar() {
    ShowCmd.update()
  }
}
