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

package com.flop.idea.fim.listener

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.LookupManagerListener
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction
import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.TemplateEditingAdapter
import com.intellij.codeInsight.template.TemplateManagerListener
import com.intellij.codeInsight.template.impl.TemplateState
import com.intellij.find.FindModelListener
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.AnActionResult
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.actionSystem.impl.ProxyShortcutSet
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.util.TextRange
import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.EditorDataContext
import com.flop.idea.fim.helper.inNormalMode
import com.flop.idea.fim.helper.isIdeaFimDisabledHere
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.options.helpers.IdeaRefactorModeHelper
import org.jetbrains.annotations.NonNls
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * @author Alex Plate
 */
object IdeaSpecifics {
  class FimActionListener : AnActionListener {
    @NonNls
    private val surrounderItems = listOf("if", "if / else", "for")
    private val surrounderAction =
      "com.intellij.codeInsight.generation.surroundWith.SurroundWithHandler\$InvokeSurrounderAction"
    private var editor: Editor? = null
    private var completionPrevDocumentLength: Int? = null
    private var completionPrevDocumentOffset: Int? = null
    override fun beforeActionPerformed(action: AnAction, event: AnActionEvent) {
      if (!com.flop.idea.fim.FimPlugin.isEnabled()) return

      val hostEditor = event.dataContext.getData(CommonDataKeys.HOST_EDITOR)
      if (hostEditor != null) {
        editor = hostEditor
      }

      if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.trackactionidsName)) {
        val id: String? = ActionManager.getInstance().getId(action) ?: (action.shortcutSet as? ProxyShortcutSet)?.actionId
        com.flop.idea.fim.FimPlugin.getNotifications(event.dataContext.getData(CommonDataKeys.PROJECT)).notifyActionId(id)
      }

      if (hostEditor != null && action is ChooseItemAction && hostEditor.fimStateMachine?.isRecording == true) {
        val lookup = LookupManager.getActiveLookup(hostEditor)
        if (lookup != null) {
          val charsToRemove = hostEditor.caretModel.primaryCaret.offset - lookup.lookupStart

          val register = com.flop.idea.fim.FimPlugin.getRegister()
          val backSpace = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0)
          repeat(charsToRemove) {
            register.recordKeyStroke(backSpace)
          }

          completionPrevDocumentLength = hostEditor.document.textLength - charsToRemove
          completionPrevDocumentOffset = lookup.lookupStart
        }
      }
    }

    override fun afterActionPerformed(action: AnAction, event: AnActionEvent, result: AnActionResult) {
      if (!com.flop.idea.fim.FimPlugin.isEnabled()) return

      val editor = editor
      if (editor != null && action is ChooseItemAction && editor.fimStateMachine?.isRecording == true) {
        val prevDocumentLength = completionPrevDocumentLength
        val prevDocumentOffset = completionPrevDocumentOffset

        if (prevDocumentLength != null && prevDocumentOffset != null) {
          val register = com.flop.idea.fim.FimPlugin.getRegister()
          val addedTextLength = editor.document.textLength - prevDocumentLength
          val caretShift = addedTextLength - (editor.caretModel.primaryCaret.offset - prevDocumentOffset)
          val leftArrow = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)

          register.recordText(editor.document.getText(TextRange(prevDocumentOffset, prevDocumentOffset + addedTextLength)))
          repeat(caretShift.coerceAtLeast(0)) {
            register.recordKeyStroke(leftArrow)
          }
        }

        this.completionPrevDocumentLength = null
        this.completionPrevDocumentOffset = null
      }

      //region Enter insert mode after surround with if
      if (surrounderAction == action.javaClass.name && surrounderItems.any {
        action.templatePresentation.text.endsWith(
            it
          )
      }
      ) {
        editor?.let {
          val commandState = it.fim.fimStateMachine
          while (commandState.mode != FimStateMachine.Mode.COMMAND) {
            commandState.popModes()
          }
          com.flop.idea.fim.FimPlugin.getChange().insertBeforeCursor(it.fim, event.dataContext.fim)
          KeyHandler.getInstance().reset(it.fim)
        }
      }
      //endregion

      this.editor = null
    }
  }

  //region Enter insert mode for surround templates without selection
  class FimTemplateManagerListener : TemplateManagerListener {
    override fun templateStarted(state: TemplateState) {
      if (!com.flop.idea.fim.FimPlugin.isEnabled()) return
      val editor = state.editor ?: return

      state.addTemplateStateListener(object : TemplateEditingAdapter() {
        override fun currentVariableChanged(
          templateState: TemplateState,
          template: Template?,
          oldIndex: Int,
          newIndex: Int,
        ) {
          if (IdeaRefactorModeHelper.keepMode()) {
            IdeaRefactorModeHelper.correctSelection(editor)
          }
        }
      })

      if (IdeaRefactorModeHelper.keepMode()) {
        IdeaRefactorModeHelper.correctSelection(editor)
      } else {
        if (!editor.selectionModel.hasSelection()) {
          // Enable insert mode if there is no selection in template
          // Template with selection is handled by [com.flop.idea.fim.group.visual.VisualMotionGroup.controlNonFimSelectionChange]
          if (editor.inNormalMode) {
            com.flop.idea.fim.FimPlugin.getChange().insertBeforeCursor(editor.fim, EditorDataContext.init(editor).fim)
            KeyHandler.getInstance().reset(editor.fim)
          }
        }
      }
    }
  }
  //endregion

  //region Register shortcuts for lookup and perform partial reset
  class LookupTopicListener : LookupManagerListener {
    override fun activeLookupChanged(oldLookup: Lookup?, newLookup: Lookup?) {
      if (!com.flop.idea.fim.FimPlugin.isEnabled()) return

      // Lookup opened
      if (oldLookup == null && newLookup is LookupImpl) {
        if (newLookup.editor.isIdeaFimDisabledHere) return

        com.flop.idea.fim.FimPlugin.getKey().registerShortcutsForLookup(newLookup)
      }

      // Lookup closed
      if (oldLookup != null && newLookup == null) {
        val editor = oldLookup.editor
        if (editor.isIdeaFimDisabledHere) return
        // VIM-1858
        KeyHandler.getInstance().partialReset(editor.fim)
      }
    }
  }
  //endregion

  //region Hide Fim search highlights when showing IntelliJ search results
  class FimFindModelListener : FindModelListener {
    override fun findNextModelChanged() {
      if (!com.flop.idea.fim.FimPlugin.isEnabled()) return
      com.flop.idea.fim.FimPlugin.getSearch().clearSearchHighlight()
    }
  }
  //endregion
}

//region Find action ID
class FindActionIdAction : DumbAwareToggleAction() {
  override fun isSelected(e: AnActionEvent): Boolean = com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.trackactionidsName)

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.trackactionidsName, FimInt(if (state) 1 else 0))
  }
}
//endregion
