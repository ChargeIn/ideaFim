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

package com.flop.idea.fim.handler

import com.flop.idea.fim.action.change.FimRepeater
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimMotionGroupBase
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.group.visual.FimBlockSelection
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.group.visual.FimSimpleSelection
import com.flop.idea.fim.group.visual.VisualChange
import com.flop.idea.fim.group.visual.VisualOperation
import com.flop.idea.fim.helper.inBlockSubMode
import com.flop.idea.fim.helper.inRepeatMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.fimStateMachine

/**
 * @author Alex Plate
 *
 * Base class for visual operation handlers.
 *
 * Use subclasses of this handler:
 *  - [VisualOperatorActionHandler.SingleExecution]
 *  - [VisualOperatorActionHandler.ForEachCaret]
 */
sealed class VisualOperatorActionHandler : EditorActionHandlerBase(false) {
  /**
   * Base class for visual operation handlers.
   * This handler executes an action for each caret. That means that if you have 5 carets,
   *   [executeAction] will be called 5 times.
   * @see [VisualOperatorActionHandler.SingleExecution] for only one execution.
   */
  abstract class ForEachCaret : VisualOperatorActionHandler() {

    /**
     * Execute an action for current [caret].
     * The selection offsets and type should be takes from [range] because this [caret] doesn't have this selection
     *   anymore in time of action execution (and editor is in normal mode, not visual).
     *
     * This method is executed once for each caret except case with block selection. If there is block selection,
     *   the method will be executed only once with [Caret#primaryCaret].
     */
    abstract fun executeAction(
      editor: FimEditor,
      caret: FimCaret,
      context: ExecutionContext,
      cmd: Command,
      range: FimSelection,
      operatorArguments: OperatorArguments,
    ): Boolean

    /**
     * This method executes before [executeAction] and only once for all carets.
     * [caretsAndSelections] contains a map of all current carets and corresponding selections.
     *   If there is block selection, only one caret is in [caretsAndSelections].
     */
    open fun beforeExecution(
      editor: FimEditor,
      context: ExecutionContext,
      cmd: Command,
      caretsAndSelections: Map<FimCaret, FimSelection>,
    ) = true

    /**
     * This method executes after [executeAction] and only once for all carets.
     * [res] has true if ALL executions of [executeAction] returned true.
     */
    open fun afterExecution(editor: FimEditor, context: ExecutionContext, cmd: Command, res: Boolean) {}
  }

  /**
   * Base class for visual operation handlers.
   * This handler executes an action only once for all carets. That means that if you have 5 carets,
   *   [executeForAllCarets] will be called 1 time.
   * @see [VisualOperatorActionHandler.ForEachCaret] for per-caret execution
   */
  abstract class SingleExecution : VisualOperatorActionHandler() {
    /**
     * Execute an action
     * [caretsAndSelections] contains a map of all current carets and corresponding selections.
     *   If there is block selection, only one caret is in [caretsAndSelections].
     *
     * This method is executed once for all carets.
     */
    abstract fun executeForAllCarets(
      editor: FimEditor,
      context: ExecutionContext,
      cmd: Command,
      caretsAndSelections: Map<FimCaret, FimSelection>,
      operatorArguments: OperatorArguments,
    ): Boolean
  }

  final override fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments
  ): Boolean {
    logger.info("Execute visual command $cmd")

    editor.fimChangeActionSwitchMode = null

    val selections = editor.collectSelections() ?: return false
    logger.debug { "Count of selection segments: ${selections.size}" }
    logger.debug { selections.values.joinToString("\n") { fimSelection -> "Caret: $fimSelection" } }

    val commandWrapper = VisualStartFinishWrapper(editor, cmd, this)
    commandWrapper.start()

    val res = arrayOf(true)
    when (this) {
      is SingleExecution -> {
        res[0] = executeForAllCarets(editor, context, cmd, selections, operatorArguments)
      }
      is ForEachCaret -> {
        logger.debug("Calling 'before execution'")
        if (!beforeExecution(editor, context, cmd, selections)) {
          logger.debug("Before execution block returned false. Stop further processing")
          return false
        }

        when {
          selections.keys.isEmpty() -> return false
          selections.keys.size == 1 -> res[0] =
            executeAction(
              editor,
              selections.keys.first(),
              context,
              cmd,
              selections.values.first(),
              operatorArguments
            )
          else -> editor.forEachNativeCaret(
            { currentCaret ->
              val range = selections.getValue(currentCaret)
              val loopRes = executeAction(editor, currentCaret, context, cmd, range, operatorArguments)
              res[0] = loopRes and res[0]
            },
            true
          )
        }

        logger.debug("Calling 'after execution'")
        afterExecution(editor, context, cmd, res[0])
      }
    }

    commandWrapper.finish(res[0])

    editor.fimChangeActionSwitchMode?.let {
      injector.changeGroup.processPostChangeModeSwitch(editor, context, it)
    }

    return res[0]
  }

  private fun FimEditor.collectSelections(): Map<FimCaret, FimSelection>? {
    return when {
      !this.inVisualMode && this.inRepeatMode -> {
        if (this.fimLastSelectionType == SelectionType.BLOCK_WISE) {
          val primaryCaret = primaryCaret()
          val range = primaryCaret.fimLastVisualOperatorRange ?: return null
          val end = VisualOperation.calculateRange(this, range, 1, primaryCaret)
          mapOf(
            primaryCaret to FimBlockSelection(
              primaryCaret.offset.point,
              end,
              this, range.columns >= FimMotionGroupBase.LAST_COLUMN
            )
          )
        } else {
          val carets = mutableMapOf<FimCaret, FimSelection>()
          this.nativeCarets().forEach { caret ->
            val range = caret.fimLastVisualOperatorRange ?: return@forEach
            val end = VisualOperation.calculateRange(this, range, 1, caret)
            carets += caret to FimSelection.create(caret.offset.point, end, range.type, this)
          }
          carets.toMap()
        }
      }
      this.inBlockSubMode -> {
        val primaryCaret = primaryCaret()
        mapOf(
          primaryCaret to FimBlockSelection(
            primaryCaret.fimSelectionStart,
            primaryCaret.offset.point,
            this, primaryCaret.fimLastColumn >= FimMotionGroupBase.LAST_COLUMN
          )
        )
      }
      else -> this.nativeCarets().associateWith { caret ->
        val subMode = this.fimStateMachine.subMode
        FimSimpleSelection.createWithNative(
          caret.fimSelectionStart,
          caret.offset.point,
          caret.selectionStart,
          caret.selectionEnd,
          SelectionType.fromSubMode(subMode),
          this
        )
      }
    }
  }

  private class VisualStartFinishWrapper(
    private val editor: FimEditor,
    private val cmd: Command,
    private val visualOperatorActionHandler: VisualOperatorActionHandler
  ) {
    private val visualChanges = mutableMapOf<FimCaret, VisualChange?>()

    fun start() {
      logger.debug("Preparing visual command")
      editor.fimKeepingVisualOperatorAction = CommandFlags.FLAG_EXIT_VISUAL !in cmd.flags

      editor.forEachCaret {
        val change =
          if (this@VisualStartFinishWrapper.editor.inVisualMode && !this@VisualStartFinishWrapper.editor.inRepeatMode) {
            VisualOperation.getRange(this@VisualStartFinishWrapper.editor, it, this@VisualStartFinishWrapper.cmd.flags)
          } else null
        this@VisualStartFinishWrapper.visualChanges[it] = change
      }
      logger.debug { visualChanges.values.joinToString("\n") { "Caret: $visualChanges" } }

      // If this is a mutli key change then exit visual now
      if (CommandFlags.FLAG_MULTIKEY_UNDO in cmd.flags || CommandFlags.FLAG_EXIT_VISUAL in cmd.flags) {
        logger.debug("Exit visual before command executing")
        editor.exitVisualModeNative()
      }
    }

    fun finish(res: Boolean) {
      logger.debug("Finish visual command. Result: $res")

      if (visualOperatorActionHandler.id != "FimVisualOperatorAction" ||
        injector.keyGroup.operatorFunction?.postProcessSelection() != false
      ) {
        if (CommandFlags.FLAG_MULTIKEY_UNDO !in cmd.flags && CommandFlags.FLAG_EXPECT_MORE !in cmd.flags) {
          logger.debug("Not multikey undo - exit visual")
          editor.exitVisualModeNative()
        }
      }

      if (res) {
        FimRepeater.saveLastChange(cmd)
        FimRepeater.repeatHandler = false
        editor.forEachCaret { caret ->
          val visualChange = visualChanges[caret]
          if (visualChange != null) {
            caret.fimLastVisualOperatorRange = visualChange
          }
        }
        editor.forEachCaret({ it.fimLastColumn = it.getVisualPosition().column })
      }

      editor.fimKeepingVisualOperatorAction = false
    }
  }

  private companion object {
    val logger = fimLogger<VisualOperatorActionHandler>()
  }
}
