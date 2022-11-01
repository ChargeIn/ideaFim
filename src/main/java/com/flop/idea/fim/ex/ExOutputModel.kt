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
package com.flop.idea.fim.ex

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.api.FimExOutputPanel
import com.flop.idea.fim.helper.fimExOutput
import com.flop.idea.fim.ui.ExOutputPanel

/**
 * @author vlan
 */
class ExOutputModel private constructor(private val myEditor: Editor) : FimExOutputPanel {
  override var text: String? = null
    private set

  override fun output(text: String) {
    this.text = text
    if (!ApplicationManager.getApplication().isUnitTestMode) {
      com.flop.idea.fim.ui.ExOutputPanel.getInstance(myEditor).setText(text)
    }
  }

  override fun clear() {
    text = null
    if (!ApplicationManager.getApplication().isUnitTestMode) {
      com.flop.idea.fim.ui.ExOutputPanel.getInstance(myEditor).deactivate(false)
    }
  }

  companion object {
    @JvmStatic
    fun getInstance(editor: Editor): ExOutputModel {
      var model = editor.fimExOutput
      if (model == null) {
        model = ExOutputModel(editor)
        editor.fimExOutput = model
      }
      return model
    }
  }
}
