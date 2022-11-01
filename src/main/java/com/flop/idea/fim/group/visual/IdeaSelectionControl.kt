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

package com.flop.idea.fim.group.visual

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.EditorDataContext
import com.flop.idea.fim.helper.editorMode
import com.flop.idea.fim.helper.exitSelectMode
import com.flop.idea.fim.helper.exitVisualMode
import com.flop.idea.fim.helper.hasVisualSelection
import com.flop.idea.fim.helper.inInsertMode
import com.flop.idea.fim.helper.inNormalMode
import com.flop.idea.fim.helper.inSelectMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.isIdeaFimDisabledHere
import com.flop.idea.fim.helper.isTemplateActive
import com.flop.idea.fim.helper.popAllModes
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.listener.FimListenerManager
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.options.helpers.IdeaRefactorModeHelper

object IdeaSelectionControl {
  /**
   * This method should be in sync with [predictMode]
   *
   * Control unexpected (non fim) selection change and adjust a mode to it. The new mode is not enabled immediately,
   *   but with some delay (using [FimVisualTimer])
   *
   * See [FimVisualTimer] to more info.
   */
  fun controlNonFimSelectionChange(
    editor: Editor,
    selectionSource: FimListenerManager.SelectionSource = FimListenerManager.SelectionSource.OTHER,
  ) {
    FimVisualTimer.singleTask(editor.editorMode) { initialMode ->

      if (editor.isIdeaFimDisabledHere) return@singleTask

      logger.debug("Adjust non-fim selection. Source: $selectionSource, initialMode: $initialMode")

      // Perform logic in one of the next cases:
      //  - There was no selection and now it is
      //  - There was a selection and now it doesn't exist
      //  - There was a selection and now it exists as well (transforming char selection to line selection, for example)
      if (initialMode?.hasVisualSelection == false && !editor.selectionModel.hasSelection(true)) {
        logger.trace { "Exiting without selection adjusting" }
        return@singleTask
      }

      if (editor.selectionModel.hasSelection(true)) {
        if (dontChangeMode(editor)) {
          IdeaRefactorModeHelper.correctSelection(editor)
          logger.trace { "Selection corrected for refactoring" }
          return@singleTask
        }

        logger.debug("Some carets have selection. State before adjustment: ${editor.fim.fimStateMachine.toSimpleString()}")

        editor.popAllModes()

        activateMode(editor, chooseSelectionMode(editor, selectionSource, true))
      } else {
        logger.debug("None of carets have selection. State before adjustment: ${editor.fim.fimStateMachine.toSimpleString()}")
        if (editor.inVisualMode) editor.exitVisualMode()
        if (editor.inSelectMode) editor.exitSelectMode(false)

        if (editor.inNormalMode) {
          activateMode(editor, chooseNonSelectionMode(editor))
        }
      }

      KeyHandler.getInstance().reset(editor.fim)
      logger.debug("${editor.editorMode} is enabled")
    }
  }

  /**
   * This method should be in sync with [controlNonFimSelectionChange]
   *
   * Predict the mode after changing visual selection. The prediction will be correct if there is only one sequential
   *   visual change (e.g. somebody executed "extract selection" action. The prediction can be wrong in case of
   *   multiple sequential visual changes (e.g. "technical" visual selection during typing in japanese)
   *
   * This method is created to improve user experience. It allows avoiding delay in some operations
   *   (because [controlNonFimSelectionChange] is not executed immediately)
   */
  fun predictMode(editor: Editor, selectionSource: FimListenerManager.SelectionSource): FimStateMachine.Mode {
    if (editor.selectionModel.hasSelection(true)) {
      if (dontChangeMode(editor)) return editor.editorMode
      return chooseSelectionMode(editor, selectionSource, false)
    } else {
      return chooseNonSelectionMode(editor)
    }
  }

  private fun activateMode(editor: Editor, mode: FimStateMachine.Mode) {
    when (mode) {
      FimStateMachine.Mode.VISUAL -> com.flop.idea.fim.FimPlugin.getVisualMotion()
        .enterVisualMode(editor.fim, com.flop.idea.fim.FimPlugin.getVisualMotion().autodetectVisualSubmode(editor.fim))
      FimStateMachine.Mode.SELECT -> com.flop.idea.fim.FimPlugin.getVisualMotion()
        .enterSelectMode(editor.fim, com.flop.idea.fim.FimPlugin.getVisualMotion().autodetectVisualSubmode(editor.fim))
      FimStateMachine.Mode.INSERT -> com.flop.idea.fim.FimPlugin.getChange().insertBeforeCursor(
        editor.fim,
        EditorDataContext.init(editor).fim
      )
      FimStateMachine.Mode.COMMAND -> Unit
      else -> error("Unexpected mode: $mode")
    }
  }

  private fun dontChangeMode(editor: Editor): Boolean =
    editor.isTemplateActive() && (IdeaRefactorModeHelper.keepMode() || editor.editorMode.hasVisualSelection)

  private fun chooseNonSelectionMode(editor: Editor): FimStateMachine.Mode {
    val templateActive = editor.isTemplateActive()
    if (templateActive && editor.inNormalMode || editor.inInsertMode) {
      return FimStateMachine.Mode.INSERT
    }
    return FimStateMachine.Mode.COMMAND
  }

  private fun chooseSelectionMode(
    editor: Editor,
    selectionSource: FimListenerManager.SelectionSource,
    logReason: Boolean,
  ): FimStateMachine.Mode {
    val selectmode = (com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.LOCAL(IjFimEditor(editor)), OptionConstants.selectmodeName) as FimString).value
    return when {
      editor.isOneLineMode -> {
        if (logReason) logger.debug("Enter select mode. Reason: one line mode")
        FimStateMachine.Mode.SELECT
      }
      selectionSource == FimListenerManager.SelectionSource.MOUSE && OptionConstants.selectmode_mouse in selectmode -> {
        if (logReason) logger.debug("Enter select mode. Selection source is mouse and selectMode option has mouse")
        FimStateMachine.Mode.SELECT
      }
      editor.isTemplateActive() && IdeaRefactorModeHelper.selectMode() -> {
        if (logReason) logger.debug("Enter select mode. Template is active and selectMode has template")
        FimStateMachine.Mode.SELECT
      }
      selectionSource == FimListenerManager.SelectionSource.OTHER &&
        OptionConstants.selectmode_ideaselection in (com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, OptionConstants.selectmodeName) as FimString).value -> {
        if (logReason) logger.debug("Enter select mode. Selection source is OTHER and selectMode has refactoring")
        FimStateMachine.Mode.SELECT
      }
      else -> {
        if (logReason) logger.debug("Enter visual mode")
        FimStateMachine.Mode.VISUAL
      }
    }
  }

  private val logger = Logger.getInstance(IdeaSelectionControl::class.java)
}
