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

import com.intellij.icons.AllIcons
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.flop.idea.fim.api.FimrcFileState
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.key.MappingOwner
import com.flop.idea.fim.troubleshooting.Troubleshooter
import com.flop.idea.fim.ui.ReloadFloatingToolbarActionGroup.Companion.ACTION_GROUP
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import com.flop.idea.fim.fimscript.services.FimRcService
import com.flop.idea.fim.fimscript.services.FimRcService.VIMRC_FILE_NAME
import com.flop.idea.fim.fimscript.services.FimRcService.executeIdeaFimRc
import org.jetbrains.annotations.TestOnly

/**
 * This file contains a "reload ~/.ideafimrc file" action functionality.
 * This is small floating action in the top right corner of the editor that appears if user edits configuration file.
 *
 * Here you can find:
 * - Simplified snapshot of config file
 * - Floating bar
 * - Action / action group
 */

object FimRcFileState : FimrcFileState {
  // Hash of .ideafimrc parsed to Script class
  private var state: Int? = null

  // ModificationStamp. Can be taken only from document. Doesn't play a big role, but can help speed up [equalTo]
  private var modificationStamp = 0L

  override var filePath: String? = null

  private val saveStateListeners = ArrayList<() -> Unit>()

  fun saveFileState(filePath: String, text: String) {
    this.filePath = FileUtil.toSystemDependentName(filePath)
    val script = FimscriptParser.parse(text)
    state = script.hashCode()
    saveStateListeners.forEach { it() }
  }

  override fun saveFileState(filePath: String) {
    val fimRcFile = FimRcService.findIdeaFimRc()
    val ideaFimRcText = fimRcFile?.readText() ?: ""
    saveFileState(filePath, ideaFimRcText)
  }

  fun equalTo(document: Document): Boolean {
    val fileModificationStamp = document.modificationStamp
    if (fileModificationStamp == modificationStamp) return true

    val documentString = document.charsSequence.toString()
    val script = FimscriptParser.parse(documentString)
    if (script.hashCode() != state) {
      return false
    }

    modificationStamp = fileModificationStamp
    return true
  }

  @TestOnly
  fun clear() {
    state = null
    modificationStamp = 0
    filePath = null
  }

  fun whenFileStateSaved(action: () -> Unit) {
    if (filePath != null) {
      action()
    }
    saveStateListeners.add(action)
  }

  fun unregisterStateListener(action: () -> Unit) {
    saveStateListeners.remove(action)
  }
}

class ReloadFimRc : DumbAwareAction() {
  override fun update(e: AnActionEvent) {
    val editor = e.getData(PlatformDataKeys.EDITOR) ?: run {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE) ?: run {
      e.presentation.isEnabledAndVisible = false
      return
    }

    if (FimRcFileState.filePath != null && FileUtil.toSystemDependentName(virtualFile.path) != FimRcFileState.filePath) {
      e.presentation.isEnabledAndVisible = false
      return
    } else if (FimRcFileState.filePath == null && !virtualFile.path.endsWith(VIMRC_FILE_NAME)) {
      // This if is about showing the reload icon if the IJ opens with .ideafimrc file opened.
      // At this moment FimRcFileState is not yet initialized.
      // XXX: I believe the proper solution would be to get rid of this if branch and update the action when
      //    `filePath` is set, but I wasn't able to make it work, the icon just doesn't appear. Maybe the action group
      //    or the toolbar should be updated along with the action.
      e.presentation.isEnabledAndVisible = false
      return
    }

    // XXX: Actually, it worth to add e.presentation.description, but it doesn't work because of some reason
    val sameDoc = FimRcFileState.equalTo(editor.document)
    e.presentation.icon = if (sameDoc) com.flop.idea.fim.icons.FimIcons.IDEAFIM else AllIcons.Actions.BuildLoadChanges
    e.presentation.text = if (sameDoc) MessageHelper.message("action.no.changes.text")
    else MessageHelper.message("action.reload.text")

    e.presentation.isEnabledAndVisible = true
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  override fun actionPerformed(e: AnActionEvent) {
    val editor = e.getData(PlatformDataKeys.EDITOR) ?: return
    FileDocumentManager.getInstance().saveDocumentAsIs(editor.document)
    injector.keyGroup.removeKeyMapping(MappingOwner.IdeaFim.InitScript)
    Troubleshooter.instance.removeByType("old-action-notation-in-mappings")
    executeIdeaFimRc()
  }
}

class ReloadFloatingToolbar : AbstractFloatingToolbarProvider(ACTION_GROUP) {
  override val autoHideable: Boolean = false

  override fun register(component: FloatingToolbarComponent, parentDisposable: Disposable) {
    super.register(component, parentDisposable)
    val action = {
      component.scheduleShow()
    }
    FimRcFileState.whenFileStateSaved(action)
    Disposer.register(parentDisposable) {
      FimRcFileState.unregisterStateListener(action)
    }
  }
}

class ReloadFloatingToolbarActionGroup : DefaultActionGroup() {
  companion object {
    const val ACTION_GROUP = "IdeaFim.ReloadFimRc.group"
  }
}
