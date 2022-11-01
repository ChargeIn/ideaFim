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

import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.helper.isCloseKeyStroke
import com.flop.idea.fim.helper.fimStateMachine
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.Toolkit
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * @author dhleong
 */
object ModalEntry {
  inline fun activate(editor: FimEditor, crossinline processor: (KeyStroke) -> Boolean) {

    // Firstly we pull the unfinished keys of the current mapping
    val mappingStack = KeyHandler.getInstance().keyStack
    var stroke = mappingStack.feedSomeStroke()
    while (stroke != null) {
      val result = processor(stroke)
      if (!result) {
        return
      }
      stroke = mappingStack.feedSomeStroke()
    }

    // Then start to accept user input
    val systemQueue = Toolkit.getDefaultToolkit().systemEventQueue
    val loop = systemQueue.createSecondaryLoop()

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(object : KeyEventDispatcher {
      override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        val stroke: KeyStroke
        if (e.id == KeyEvent.KEY_RELEASED) {
          stroke = KeyStroke.getKeyStrokeForEvent(e)
          if (!stroke.isCloseKeyStroke() && stroke.keyCode != KeyEvent.VK_ENTER) {
            return true
          }
        } else if (e.id == KeyEvent.KEY_TYPED) {
          stroke = KeyStroke.getKeyStrokeForEvent(e)
        } else {
          return true
        }
        if (editor.fimStateMachine.isRecording) {
          KeyHandler.getInstance().modalEntryKeys += stroke
        }
        if (!processor(stroke)) {
          KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this)
          loop.exit()
        }
        return true
      }
    })

    loop.enter()
  }
}
