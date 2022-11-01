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
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.components.Service
import com.intellij.openapi.util.Computable
import com.intellij.util.ExceptionUtil
import com.flop.idea.fim.api.FimApplicationBase
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.helper.RunnableHelper
import java.awt.Component
import java.awt.Toolkit
import java.awt.Window
import java.awt.event.KeyEvent
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

@Service
class IjFimApplication : FimApplicationBase() {
  override fun isMainThread(): Boolean {
    return ApplicationManager.getApplication().isDispatchThread
  }

  override fun invokeLater(action: () -> Unit, editor: FimEditor) {
    ApplicationManager.getApplication()
      .invokeLater(action, ModalityState.stateForComponent((editor as IjFimEditor).editor.component))
  }

  override fun invokeLater(action: () -> Unit) {
    ApplicationManager.getApplication().invokeLater(action)
  }

  override fun isUnitTest(): Boolean {
    return ApplicationManager.getApplication().isUnitTestMode
  }

  override fun postKey(stroke: KeyStroke, editor: FimEditor) {
    val component: Component = SwingUtilities.getAncestorOfClass(Window::class.java, editor.ij.component)
    val event = createKeyEvent(stroke, component)
    ApplicationManager.getApplication().invokeLater {
      if (logger.isDebug()) {
        logger.debug("posting $event")
      }
      Toolkit.getDefaultToolkit().systemEventQueue.postEvent(event)
    }
  }

  override fun localEditors(): List<FimEditor> {
    return com.flop.idea.fim.helper.localEditors().map { IjFimEditor(it) }
  }

  override fun runWriteCommand(editor: FimEditor, name: String?, groupId: Any?, command: Runnable) {
    RunnableHelper.runWriteCommand((editor as IjFimEditor).editor.project, command, name, groupId)
  }

  override fun runReadCommand(editor: FimEditor, name: String?, groupId: Any?, command: Runnable) {
    RunnableHelper.runReadCommand((editor as IjFimEditor).editor.project, command, name, groupId)
  }

  override fun <T> runWriteAction(action: () -> T): T {
    return ApplicationManager.getApplication().runWriteAction(Computable(action))
  }

  override fun <T> runReadAction(action: () -> T): T {
    return ApplicationManager.getApplication().runReadAction(Computable(action))
  }

  override fun currentStackTrace(): String {
    return ExceptionUtil.currentStackTrace()
  }

  override fun runAfterGotFocus(runnable: Runnable) {
    com.flop.idea.fim.helper.runAfterGotFocus(runnable)
  }

  private fun createKeyEvent(stroke: KeyStroke, component: Component): KeyEvent {
    return KeyEvent(
      component,
      if (stroke.keyChar == KeyEvent.CHAR_UNDEFINED) KeyEvent.KEY_PRESSED else KeyEvent.KEY_TYPED,
      System.currentTimeMillis(), stroke.modifiers, stroke.keyCode, stroke.keyChar
    )
  }

  companion object {
    private val logger = fimLogger<IjFimApplication>()
  }
}
