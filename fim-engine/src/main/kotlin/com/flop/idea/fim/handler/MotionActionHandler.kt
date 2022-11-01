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

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimCaretListener
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.helper.inBlockSubMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.isEndAllowed

/**
 * @author Alex Plate
 *
 * Base class for motion handlers.
 * @see [MotionActionHandler.SingleExecution] and [MotionActionHandler.ForEachCaret]
 */
sealed class MotionActionHandler : EditorActionHandlerBase(false) {

  /**
   * Base class for motion handlers.
   * This handler executes an action for each caret. That means that if you have 5 carets, [getOffset] will be
   *   called 5 times.
   * @see [MotionActionHandler.SingleExecution] for only one execution
   */
  abstract class ForEachCaret : MotionActionHandler() {

    /**
     * This method should return new offset for [caret]
     * It executes once for each [caret]. That means that if you have 5 carets, [getOffset] will be
     *   called 5 times.
     * The method executes only once it there is block selection.
     */
    abstract fun getOffset(
      editor: FimEditor,
      caret: FimCaret,
      context: ExecutionContext,
      argument: Argument?,
      operatorArguments: OperatorArguments,
    ): Motion

    /**
     * This method is called before [getOffset] once for each [caret].
     * The method executes only once it there is block selection.
     */
    open fun preOffsetComputation(editor: FimEditor, caret: FimCaret, context: ExecutionContext, cmd: Command): Boolean = true

    /**
     * This method is called after [getOffset], but before caret motion.
     *
     * The method executes for each caret, but only once it there is block selection.
     */
    open fun preMove(editor: FimEditor, caret: FimCaret, context: ExecutionContext, cmd: Command) {}

    /**
     * This method is called after [getOffset] and after caret motion.
     *
     * The method executes for each caret, but only once it there is block selection.
     */
    open fun postMove(editor: FimEditor, caret: FimCaret, context: ExecutionContext, cmd: Command) {}
  }

  /**
   * Base class for motion handlers.
   * This handler executes an action only once for all carets. That means that if you have 5 carets,
   *   [getOffset] will be called 1 time.
   * @see [MotionActionHandler.ForEachCaret] for per-caret execution
   */
  abstract class SingleExecution : MotionActionHandler() {
    /**
     * This method should return new offset for primary caret
     * It executes once for all carets. That means that if you have 5 carets, [getOffset] will be
     *   called 1 time.
     */
    abstract fun getOffset(
      editor: FimEditor,
      context: ExecutionContext,
      argument: Argument?,
      operatorArguments: OperatorArguments
    ): Motion

    /**
     * This method is called before [getOffset].
     * The method executes only once.
     */
    open fun preOffsetComputation(editor: FimEditor, context: ExecutionContext, cmd: Command): Boolean = true

    /**
     * This method is called after [getOffset], but before caret motion.
     *
     * The method executes only once.
     */
    open fun preMove(editor: FimEditor, context: ExecutionContext, cmd: Command) = Unit

    /**
     * This method is called after [getOffset] and after caret motion.
     *
     * The method executes only once it there is block selection.
     */
    open fun postMove(editor: FimEditor, context: ExecutionContext, cmd: Command) = Unit
  }

  abstract val motionType: MotionType

  final override val type: Command.Type = Command.Type.MOTION

  fun getHandlerOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    return when (this) {
      is SingleExecution -> getOffset(editor, context, argument, operatorArguments)
      is ForEachCaret -> getOffset(editor, caret, context, argument, operatorArguments)
    }
  }

  final override fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val blockSubmodeActive = editor.inBlockSubMode

    when (this) {
      is SingleExecution -> run {
        if (!preOffsetComputation(editor, context, cmd)) return@run

        val offset = getOffset(editor, context, cmd.argument, operatorArguments)

        when (offset) {
          is Motion.AbsoluteOffset -> {
            var resultOffset = offset.offset
            if (resultOffset < 0) {
              logger.error("Offset is less than 0. $resultOffset. ${this.javaClass.name}")
            }
            if (CommandFlags.FLAG_SAVE_JUMP in cmd.flags) {
              injector.markGroup.saveJumpLocation(editor)
            }
            if (!editor.isEndAllowed) {
              resultOffset = injector.engineEditorHelper.normalizeOffset(editor, resultOffset, false)
            }
            preMove(editor, context, cmd)
            editor.primaryCaret().moveToOffset(resultOffset)
            postMove(editor, context, cmd)
          }
          is Motion.Error -> injector.messages.indicateError()
          is Motion.NoMotion -> Unit
        }
      }
      is ForEachCaret -> run {
        when {
          blockSubmodeActive || editor.carets().size == 1 -> {
            val primaryCaret = editor.primaryCaret()
            doExecuteForEach(editor, primaryCaret, context, cmd, operatorArguments)
          }
          else -> {
            try {
              editor.addCaretListener(CaretMergingWatcher)
              editor.forEachCaret { caret ->
                doExecuteForEach(
                  editor,
                  caret,
                  context,
                  cmd,
                  operatorArguments
                )
              }
            } finally {
              editor.removeCaretListener(CaretMergingWatcher)
            }
          }
        }
      }
    }

    return true
  }

  private fun doExecuteForEach(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments
  ) {
    this as ForEachCaret
    if (!preOffsetComputation(editor, caret, context, cmd)) return

    val offset = getOffset(editor, caret, context, cmd.argument, operatorArguments)

    when (offset) {
      is Motion.AbsoluteOffset -> {
        var resultMotion = offset.offset
        if (resultMotion < 0) {
          logger.error("Offset is less than 0. $resultMotion. ${this.javaClass.name}")
        }
        if (CommandFlags.FLAG_SAVE_JUMP in cmd.flags) {
          injector.markGroup.saveJumpLocation(editor)
        }
        if (!editor.isEndAllowed) {
          resultMotion = injector.engineEditorHelper.normalizeOffset(editor, resultMotion, false)
        }
        preMove(editor, caret, context, cmd)
        caret.moveToOffset(resultMotion)
        val postMoveCaret = if (editor.inBlockSubMode) editor.primaryCaret() else caret
        postMove(editor, postMoveCaret, context, cmd)
      }
      is Motion.Error -> injector.messages.indicateError()
      is Motion.NoMotion -> Unit
    }
  }

  private object CaretMergingWatcher : FimCaretListener {
    override fun caretRemoved(caret: FimCaret?) {
      caret ?: return
      val editor = caret.editor
      val caretToDelete = caret
      if (editor.inVisualMode) {
        for (fimCaret in editor.carets()) {
          val curCaretStart = fimCaret.selectionStart
          val curCaretEnd = fimCaret.selectionEnd
          val caretStartBetweenCur = caretToDelete.selectionStart in curCaretStart until curCaretEnd
          val caretEndBetweenCur = caretToDelete.selectionEnd in curCaretStart + 1..curCaretEnd
          if (caretStartBetweenCur || caretEndBetweenCur) {
            // Okay, caret is being removed because of merging
            val fimSelectionStart = caretToDelete.fimSelectionStart
            fimCaret.fimSelectionStart = fimSelectionStart
          }
        }
      }
    }
  }

  companion object {
    val logger = fimLogger<MotionActionHandler>()
  }
}
