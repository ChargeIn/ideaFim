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

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.openapi.wm.impl.status.widget.StatusBarWidgetsManager
import com.intellij.util.Consumer
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.helper.EngineStringHelper
import com.flop.idea.fim.helper.FimNlsSafe
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionChangeListener
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import org.jetbrains.annotations.NonNls
import java.awt.Component
import java.awt.event.MouseEvent

object ShowCmd {
  // https://github.com/fim/fim/blob/b376ace1aeaa7614debc725487d75c8f756dd773/src/fim.h#L1721
  private const val SHOWCMD_COLS = 10

  @NonNls
  internal const val ID = "IdeaFim::ShowCmd"

  @NlsSafe
  internal const val displayName = "IdeaFim showcmd"

  fun update() {
    val windowManager = WindowManager.getInstance()
    ProjectManager.getInstance().openProjects.forEach {
      val statusBar = windowManager.getStatusBar(it)
      statusBar.updateWidget(ID)
    }
  }

  fun getWidgetText(editor: Editor?): String {
    // Fim only shows the last 10 characters. See normal.c:add_to_showcmd
    // https://github.com/fim/fim/blob/b376ace1aeaa7614debc725487d75c8f756dd773/src/normal.c#L1885-L1890
    return getFullText(editor).takeLast(SHOWCMD_COLS)
  }

  fun getFullText(editor: Editor?): String {
    if (!com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.showcmdName) || editor == null || editor.isDisposed) return ""

    val editorState = editor.fim.fimStateMachine
    return EngineStringHelper.toPrintableCharacters(editorState.commandBuilder.keys + editorState.mappingState.keys)
  }
}

object ShowCmdOptionChangeListener : OptionChangeListener<FimDataType> {
  override fun processGlobalValueChange(oldValue: FimDataType?) {
    ShowCmd.update()

    val extension = StatusBarWidgetFactory.EP_NAME.findExtension(ShowCmdStatusBarWidgetFactory::class.java) ?: return
    val projectManager = ProjectManager.getInstanceIfCreated() ?: return
    for (project in projectManager.openProjects) {
      val statusBarWidgetsManager = project.getService(StatusBarWidgetsManager::class.java) ?: continue
      statusBarWidgetsManager.updateWidget(extension)
    }
  }
}

class ShowCmdStatusBarWidgetFactory : StatusBarWidgetFactory/*, LightEditCompatible*/ {
  override fun getId() = ShowCmd.ID

  override fun getDisplayName(): String = ShowCmd.displayName

  override fun disposeWidget(widget: StatusBarWidget) {
    // Nothing
  }

  override fun isAvailable(project: Project): Boolean = com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.showcmdName)

  override fun createWidget(project: Project): StatusBarWidget = Widget(project)

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true

  // Should be configured via `set showcmd`
  override fun isConfigurable(): Boolean = false
}

// `:help 'showcmd'`
// Widget shows:
// * Partial command, as it's being typed
// * When selecting characters within a line, the number of characters
//   * Tabs are shown as one char
//   * If the number of bytes is different, this is also shown: "2-6"
// * When selecting more than one line, the number of lines
// * When selecting a block, the size in screen characters: {lines}x{columns}
//
// We only need to show partial commands, since the standard PositionPanel shows the other information already, with
// the exception of "{lines}x{columns}" (it shows "x carets" instead)
class Widget(project: Project) :
  EditorBasedWidget(project),
  StatusBarWidget.Multiframe,
  StatusBarWidget.TextPresentation {

  override fun ID() = ShowCmd.ID

  override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

  override fun getClickConsumer(): Consumer<MouseEvent>? = null

  @FimNlsSafe
  override fun getTooltipText(): String {
    var count = ShowCmd.getFullText(this.editor)
    if (count.isNotBlank()) count = ": $count"
    return "${ShowCmd.displayName}$count"
  }

  override fun getText(): String = ShowCmd.getWidgetText(editor)

  override fun getAlignment() = Component.CENTER_ALIGNMENT

  // Multiframe#copy to show the widget on popped out editors
  override fun copy(): StatusBarWidget = Widget(myProject)

  override fun selectionChanged(event: FileEditorManagerEvent) {
    // Update when changing selected editor
    myStatusBar?.updateWidget(ShowCmd.ID)
  }
}
