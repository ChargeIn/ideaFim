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
package com.flop.idea.fim

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.ActionPlan
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.intellij.openapi.editor.actionSystem.TypedActionHandlerEx
import com.intellij.openapi.progress.ProcessCanceledException
import com.flop.idea.fim.helper.EditorDataContext
import com.flop.idea.fim.helper.inInsertMode
import com.flop.idea.fim.helper.isIdeaFimDisabledHere
import com.flop.idea.fim.key.KeyHandlerKeeper
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * Accepts all regular keystrokes and passes them on to the Fim key handler.
 *
 * IDE shortcut keys used by Fim commands are handled by [com.flop.idea.fim.action.FimShortcutKeyAction].
 */
class FimTypedActionHandler(origHandler: TypedActionHandler) : TypedActionHandlerEx {
  private val handler = KeyHandler.getInstance()
  private val traceTime = com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideatracetimeName)

  init {
    KeyHandlerKeeper.getInstance().originalHandler = origHandler
  }

  override fun beforeExecute(editor: Editor, charTyped: Char, context: DataContext, plan: ActionPlan) {
    LOG.trace("Before execute for typed action")
    if (editor.isIdeaFimDisabledHere) {
      LOG.trace("IdeaFim disabled here, finish")
      (KeyHandlerKeeper.getInstance().originalHandler as? TypedActionHandlerEx)?.beforeExecute(editor, charTyped, context, plan)
      return
    }

    LOG.trace("Executing before execute")
    val modifiers = if (charTyped == ' ' && FimKeyListener.isSpaceShift) KeyEvent.SHIFT_DOWN_MASK else 0
    val keyStroke = KeyStroke.getKeyStroke(charTyped, modifiers)

    /* Invoked before acquiring a write lock and actually handling the keystroke.
     *
     * Drafts an optional [ActionPlan] that will be used as a base for zero-latency rendering in editor.
     */
    if (editor.inInsertMode) {
      val originalHandler = KeyHandlerKeeper.getInstance().originalHandler
      if (originalHandler is TypedActionHandlerEx) {
        originalHandler.beforeExecute(editor, keyStroke.keyChar, context, plan)
      }
    }
  }

  override fun execute(editor: Editor, charTyped: Char, context: DataContext) {
    LOG.trace("Execute for typed action")
    if (editor.isIdeaFimDisabledHere) {
      LOG.trace("IdeaFim disabled here, finish")
      KeyHandlerKeeper.getInstance().originalHandler.execute(editor, charTyped, context)
      return
    }

    try {
      LOG.trace("Executing typed action")
      val modifiers = if (charTyped == ' ' && FimKeyListener.isSpaceShift) KeyEvent.SHIFT_DOWN_MASK else 0
      val keyStroke = KeyStroke.getKeyStroke(charTyped, modifiers)
      val startTime = if (traceTime) System.currentTimeMillis() else null
      handler.handleKey(editor.fim, keyStroke, EditorDataContext.init(editor, context).fim)
      if (startTime != null) {
        val duration = System.currentTimeMillis() - startTime
        LOG.info("FimTypedAction '$charTyped': $duration ms")
      }
    } catch (e: ProcessCanceledException) {
      // Nothing
    } catch (e: Throwable) {
      LOG.error(e)
    }
  }

  companion object {
    private val LOG = logger<FimTypedActionHandler>()
  }
}

/**
 * A nasty workaround to handle `<S-Space>` events. Probably all the key events should go trough this listener.
 */
object FimKeyListener : KeyAdapter() {

  var isSpaceShift = false

  override fun keyPressed(e: KeyEvent) {
    isSpaceShift = e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK != 0 && e.keyChar == ' '
  }
}
