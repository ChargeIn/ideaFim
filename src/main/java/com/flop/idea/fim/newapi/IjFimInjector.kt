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

import com.intellij.openapi.components.service
import com.intellij.openapi.components.serviceIfCreated
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.api.EngineEditorHelper
import com.flop.idea.fim.api.ExEntryPanel
import com.flop.idea.fim.api.ExecutionContextManager
import com.flop.idea.fim.api.NativeActionManager
import com.flop.idea.fim.api.SystemInfoService
import com.flop.idea.fim.api.FimActionExecutor
import com.flop.idea.fim.api.FimApplication
import com.flop.idea.fim.api.FimChangeGroup
import com.flop.idea.fim.api.FimClipboardManager
import com.flop.idea.fim.api.FimCommandGroup
import com.flop.idea.fim.api.FimDigraphGroup
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimEditorGroup
import com.flop.idea.fim.api.FimEnabler
import com.flop.idea.fim.api.FimExOutputPanel
import com.flop.idea.fim.api.FimExOutputPanelService
import com.flop.idea.fim.api.FimExtensionRegistrator
import com.flop.idea.fim.api.FimFile
import com.flop.idea.fim.api.FimInjectorBase
import com.flop.idea.fim.api.FimKeyGroup
import com.flop.idea.fim.api.FimLookupManager
import com.flop.idea.fim.api.FimMessages
import com.flop.idea.fim.api.FimMotionGroup
import com.flop.idea.fim.api.FimProcessGroup
import com.flop.idea.fim.api.FimRegexpService
import com.flop.idea.fim.api.FimSearchGroup
import com.flop.idea.fim.api.FimSearchHelper
import com.flop.idea.fim.api.FimStatistics
import com.flop.idea.fim.api.FimStorageService
import com.flop.idea.fim.api.FimStringParser
import com.flop.idea.fim.api.FimTemplateManager
import com.flop.idea.fim.api.FimVisualMotionGroup
import com.flop.idea.fim.api.FimrcFileState
import com.flop.idea.fim.api.FimscriptExecutor
import com.flop.idea.fim.api.FimscriptFunctionService
import com.flop.idea.fim.api.FimscriptParser
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.FimMachine
import com.flop.idea.fim.diagnostic.FimLogger
import com.flop.idea.fim.ex.ExOutputModel
import com.flop.idea.fim.extension.FimExtensionRegistrar
import com.flop.idea.fim.group.CommandGroup
import com.flop.idea.fim.group.TabService
import com.flop.idea.fim.group.FimWindowGroup
import com.flop.idea.fim.group.copy.PutGroup
import com.flop.idea.fim.group.copy.YankGroup
import com.flop.idea.fim.helper.CommandLineHelper
import com.flop.idea.fim.helper.IjActionExecutor
import com.flop.idea.fim.helper.IjEditorHelper
import com.flop.idea.fim.helper.IjFimStringParser
import com.flop.idea.fim.helper.UndoRedoHelper
import com.flop.idea.fim.helper.FimCommandLineHelper
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.history.FimHistory
import com.flop.idea.fim.macro.FimMacro
import com.flop.idea.fim.mark.FimMarkGroup
import com.flop.idea.fim.put.FimPut
import com.flop.idea.fim.register.FimRegisterGroup
import com.flop.idea.fim.ui.FimRcFileState
import com.flop.idea.fim.undo.FimUndoRedo
import com.flop.idea.fim.fimscript.Executor
import com.flop.idea.fim.fimscript.services.FunctionStorage
import com.flop.idea.fim.fimscript.services.OptionService
import com.flop.idea.fim.fimscript.services.PatternService
import com.flop.idea.fim.fimscript.services.VariableService
import com.flop.idea.fim.yank.FimYankGroup

class IjFimInjector : FimInjectorBase() {
  override fun <T : Any> getLogger(clazz: Class<T>): FimLogger = IjFimLogger(Logger.getInstance(clazz::class.java))

  override val actionExecutor: FimActionExecutor
    get() = service<IjActionExecutor>()
  override val exEntryPanel: ExEntryPanel
    get() = service<IjExEntryPanel>()
  override val exOutputPanel: FimExOutputPanelService
    get() = object : FimExOutputPanelService {
      override fun getPanel(editor: FimEditor): FimExOutputPanel {
        return ExOutputModel.getInstance(editor.ij)
      }
    }
  override val historyGroup: FimHistory
    get() = service<com.flop.idea.fim.group.HistoryGroup>()
  override val extensionRegistrator: FimExtensionRegistrator
    get() = FimExtensionRegistrar
  override val tabService: TabService
    get() = service()
  override val regexpService: FimRegexpService
    get() = PatternService
  override val clipboardManager: FimClipboardManager
    get() = service<IjClipboardManager>()
  override val searchHelper: FimSearchHelper
    get() = service<IjFimSearchHelper>()
  override val motion: FimMotionGroup
    get() = service<com.flop.idea.fim.group.MotionGroup>()
  override val lookupManager: FimLookupManager
    get() = service<IjFimLookupManager>()
  override val templateManager: FimTemplateManager
    get() = service<IjTemplateManager>()
  override val searchGroup: FimSearchGroup
    get() = service<com.flop.idea.fim.group.SearchGroup>()
  override val put: FimPut
    get() = service<PutGroup>()
  override val window: FimWindowGroup
    get() = service<com.flop.idea.fim.group.WindowGroup>()
  override val yank: FimYankGroup
    get() = service<YankGroup>()
  override val file: FimFile
    get() = service<com.flop.idea.fim.group.FileGroup>()
  override val macro: FimMacro
    get() = service<com.flop.idea.fim.group.MacroGroup>()
  override val undo: FimUndoRedo
    get() = service<UndoRedoHelper>()
  override val commandLineHelper: FimCommandLineHelper
    get() = service<CommandLineHelper>()
  override val nativeActionManager: NativeActionManager
    get() = service<IjNativeActionManager>()
  override val messages: FimMessages
    get() = service<IjFimMessages>()
  override val registerGroup: FimRegisterGroup
    get() = service()
  override val registerGroupIfCreated: FimRegisterGroup?
    get() = serviceIfCreated()
  override val changeGroup: FimChangeGroup
    get() = service()
  override val processGroup: FimProcessGroup
    get() = service()
  override val keyGroup: FimKeyGroup
    get() = service()
  override val markGroup: FimMarkGroup
    get() = service<com.flop.idea.fim.group.MarkGroup>()
  override val application: FimApplication
    get() = service<IjFimApplication>()
  override val executionContextManager: ExecutionContextManager
    get() = service<IjExecutionContextManager>()
  override val fimMachine: FimMachine
    get() = service<FimMachineImpl>()
  override val enabler: FimEnabler
    get() = service<IjFimEnabler>()
  override val digraphGroup: FimDigraphGroup
    get() = service()
  override val visualMotionGroup: FimVisualMotionGroup
    get() = service()
  override val statisticsService: FimStatistics
    get() = service()
  override val commandGroup: FimCommandGroup
    get() = service<CommandGroup>()

  override val functionService: FimscriptFunctionService
    get() = FunctionStorage
  override val variableService: VariableService
    get() = service()
  override val fimrcFileState: FimrcFileState
    get() = FimRcFileState
  override val fimscriptExecutor: FimscriptExecutor
    get() = service<Executor>()
  override val fimscriptParser: FimscriptParser
    get() = com.flop.idea.fim.fimscript.parser.FimscriptParser

  override val optionService: OptionService
    get() = service()
  override val parser: FimStringParser
    get() = service<IjFimStringParser>()

  override val systemInfoService: SystemInfoService
    get() = service()
  override val fimStorageService: FimStorageService
    get() = service()

  override fun commandStateFor(editor: FimEditor): FimStateMachine {
    var res = editor.ij.fimStateMachine
    if (res == null) {
      res = FimStateMachine(editor)
      editor.ij.fimStateMachine = res
    }
    return res
  }

  override fun commandStateFor(editor: Any): FimStateMachine {
    return when (editor) {
      is FimEditor -> this.commandStateFor(editor)
      is Editor -> this.commandStateFor(IjFimEditor(editor))
      else -> error("Unexpected type: $editor")
    }
  }

  override val engineEditorHelper: EngineEditorHelper
    get() = service<IjEditorHelper>()
  override val editorGroup: FimEditorGroup
    get() = service<com.flop.idea.fim.group.EditorGroup>()
}
