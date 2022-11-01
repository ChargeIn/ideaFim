package com.flop.idea.fim.api

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.FimMachine
import com.flop.idea.fim.diagnostic.FimLogger
import com.flop.idea.fim.group.TabService
import com.flop.idea.fim.group.FimWindowGroup
import com.flop.idea.fim.helper.FimCommandLineHelper
import com.flop.idea.fim.history.FimHistory
import com.flop.idea.fim.macro.FimMacro
import com.flop.idea.fim.mark.FimMarkGroup
import com.flop.idea.fim.put.FimPut
import com.flop.idea.fim.register.FimRegisterGroup
import com.flop.idea.fim.undo.FimUndoRedo
import com.flop.idea.fim.fimscript.services.OptionService
import com.flop.idea.fim.fimscript.services.VariableService
import com.flop.idea.fim.yank.FimYankGroup

interface FimInjector {
  // [FINISHED] Fully moved to fim-engine. Should we remove it from injector?
  val parser: FimStringParser
  // [FINISHED] Can't be fully moved to fim-engine
  val messages: FimMessages
  // [FINISHED] Fully moved to fim-engine. Only state left in the IJ
  // Let's keep the state saver as is until we'll figure out how to implement this in fleet.
  val registerGroup: FimRegisterGroup
  val registerGroupIfCreated: FimRegisterGroup?
  // [FINISHED] Can't be fully moved to fim-engine.
  // Lots of interaction with EX panel. Let's refactor it when figure out how it works in fleet.
  val processGroup: FimProcessGroup
  // [FINISHED] Can't be fully moved to fim-engine.
  // A lot of interaction with IJ.
  val application: FimApplication
  // [FINISHED] Can't be fully moved to fim-engine.
  // Getting contextes. Need to clarify how it works in fleet before refactoring.
  val executionContextManager: ExecutionContextManager
  // [FINISHED] Fully moved to fim-engine except one method that iterates with IJ.
  // Need to check how it would work in fleet before moving this method.
  val digraphGroup: FimDigraphGroup
  // [FINISHED] Fully moved to fim-engine. Should we remove it from injector?
  val fimMachine: FimMachine
  // [FINISHED] Can't be fully moved to fim-engine.
  val enabler: FimEnabler

  // TODO We should somehow state that [OptionServiceImpl] can be used from any implementation
  // [UNFINISHED] !! in progress
  val optionService: OptionService
  // [FINISHED] Can't be fully moved to fim-engine.
  val nativeActionManager: NativeActionManager
  // [FINISHED] Can't be fully moved to fim-engine.
  val keyGroup: FimKeyGroup
  // [FINISHED] Only state left in the IJ && some IJ specifics
  val markGroup: FimMarkGroup
  // [FINISHED] Only IJ staff left
  val visualMotionGroup: FimVisualMotionGroup
  // [FINISHED] Class moved to fim-engine, but it's attached to Editor using IJ things
  fun commandStateFor(editor: FimEditor): FimStateMachine
  // [FINISHED] Class moved to fim-engine, but it's attached to Editor using IJ things
  /**
   * COMPATIBILITY-LAYER: Added new method with Any
   * Please see: https://jb.gg/zo8n0r
   */
  fun commandStateFor(editor: Any): FimStateMachine
  // !! in progress
  val engineEditorHelper: EngineEditorHelper
  // [FINISHED] Only IJ staff
  val editorGroup: FimEditorGroup
  // [FINISHED] Fully moved to fim-engine. Should we remove it from injector?
  val commandGroup: FimCommandGroup
  // !! in progress
  val changeGroup: FimChangeGroup
  // Can't be fully moved to fim-engine.
  val actionExecutor: FimActionExecutor
  // Can't be fully moved to fim-engine.
  val exEntryPanel: ExEntryPanel
  // Can't be fully moved to fim-engine.
  val exOutputPanel: FimExOutputPanelService
  // Can't be fully moved to fim-engine.
  val clipboardManager: FimClipboardManager
  // Only state left in the IJ
  val historyGroup: FimHistory
  // !! in progress
  val extensionRegistrator: FimExtensionRegistrator
  // Can't be fully moved to fim-engine.
  val tabService: TabService
  // !! in progress
  val regexpService: FimRegexpService

  // !! in progress
  val searchHelper: FimSearchHelper
  // !! in progress
  val motion: FimMotionGroup
  // Can't be fully moved to fim-engine.
  val lookupManager: FimLookupManager
  // Can't be fully moved to fim-engine.
  val templateManager: FimTemplateManager
  // !! in progress
  val searchGroup: FimSearchGroup
  // Can't be fully moved to fim-engine.
  val statisticsService: FimStatistics
  // !! in progress
  val put: FimPut
  // Can't be fully moved to fim-engine.
  val window: FimWindowGroup
  // !! in progress
  val yank: FimYankGroup
  // !! in progress
  val file: FimFile
  // !! in progress
  val macro: FimMacro
  // !! in progress
  val undo: FimUndoRedo
  // !! in progress
  val commandLineHelper: FimCommandLineHelper

  // Can't be fully moved to fim-engine.
  val fimscriptExecutor: FimscriptExecutor
  // Can't be fully moved to fim-engine.
  val fimscriptParser: FimscriptParser
  // !! in progress
  val variableService: VariableService
  // !! in progress
  val functionService: FimscriptFunctionService
  // Can't be fully moved to fim-engine.
  val fimrcFileState: FimrcFileState

  val systemInfoService: SystemInfoService
  val fimStorageService: FimStorageService

  /**
   * Please use fimLogger() function
   */
  fun <T : Any> getLogger(clazz: Class<T>): FimLogger
}

lateinit var injector: FimInjector
