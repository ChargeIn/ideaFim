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
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.inSelectMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString

/**
 * @author Alex Plate
 *
 * Handler for SHIFTED special keys except arrows, that are defined in `:h keymodel`
 * There are: <End>, <Home>, <PageUp> and <PageDown>
 *
 * This handler is used to properly handle there keys according to current `keymodel` and `selectmode` options
 *
 * Handler is called once for all carets
 */
abstract class ShiftedSpecialKeyHandler : FimActionHandler.ConditionalMulticaret() {
  final override fun execute(editor: FimEditor, context: ExecutionContext, cmd: Command, operatorArguments: OperatorArguments): Boolean {
    error("This method should not be executed")
  }

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    motion(editor, context, cmd, caret)
    return true
  }

  override fun runAsMulticaret(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val startSel = OptionConstants.keymodel_startsel in (injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.keymodelName) as FimString).value
    if (startSel && !editor.inVisualMode && !editor.inSelectMode) {
      if (OptionConstants.selectmode_key in (injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.selectmodeName) as FimString).value) {
        injector.visualMotionGroup.enterSelectMode(editor, FimStateMachine.SubMode.VISUAL_CHARACTER)
      } else {
        injector.visualMotionGroup
          .toggleVisual(editor, 1, 0, FimStateMachine.SubMode.VISUAL_CHARACTER)
      }
    }
    return true
  }

  /**
   * This method is called when `keymodel` doesn't contain `startsel`,
   * or contains one of `continue*` values but in different mode.
   */
  abstract fun motion(editor: FimEditor, context: ExecutionContext, cmd: Command, caret: FimCaret)
}

/**
 * Handler for SHIFTED arrow keys
 *
 * This handler is used to properly handle there keys according to current `keymodel` and `selectmode` options
 *
 * Handler is called once for all carets
 */
abstract class ShiftedArrowKeyHandler(private val runBothCommandsAsMulticaret: Boolean) : FimActionHandler.ConditionalMulticaret() {

  override fun runAsMulticaret(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val (inVisualMode, inSelectMode, withKey) = withKeyOrNot(editor)
    if (withKey) {
      if (!inVisualMode && !inSelectMode) {
        if (OptionConstants.selectmode_key in (injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.selectmodeName) as FimString).value) {
          injector.visualMotionGroup.enterSelectMode(editor, FimStateMachine.SubMode.VISUAL_CHARACTER)
        } else {
          injector.visualMotionGroup
            .toggleVisual(editor, 1, 0, FimStateMachine.SubMode.VISUAL_CHARACTER)
        }
      }
      return true
    } else {
      return runBothCommandsAsMulticaret
    }
  }

  private fun withKeyOrNot(editor: FimEditor): Triple<Boolean, Boolean, Boolean> {
    val keymodelOption =
      (injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.keymodelName) as FimString).value
    val startSel = OptionConstants.keymodel_startsel in keymodelOption
    val inVisualMode = editor.inVisualMode
    val inSelectMode = editor.inSelectMode

    val continueSelectSelection = OptionConstants.keymodel_continueselect in keymodelOption && inSelectMode
    val continueVisualSelection = OptionConstants.keymodel_continuevisual in keymodelOption && inVisualMode
    val withKey = startSel || continueSelectSelection || continueVisualSelection
    return Triple(inVisualMode, inSelectMode, withKey)
  }

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (runBothCommandsAsMulticaret) {
      val (_, _, withKey) = withKeyOrNot(editor)
      if (withKey) {
        motionWithKeyModel(editor, caret, context, cmd)
      } else {
        motionWithoutKeyModel(editor, context, cmd)
      }
    } else {
      motionWithKeyModel(editor, caret, context, cmd)
    }
    return true
  }

  final override fun execute(editor: FimEditor, context: ExecutionContext, cmd: Command, operatorArguments: OperatorArguments): Boolean {
    motionWithoutKeyModel(editor, context, cmd)
    return true
  }

  /**
   * This method is called when `keymodel` contains `startsel`, or one of `continue*` values in corresponding mode
   */
  abstract fun motionWithKeyModel(editor: FimEditor, caret: FimCaret, context: ExecutionContext, cmd: Command)

  /**
   * This method is called when `keymodel` doesn't contain `startsel`,
   * or contains one of `continue*` values but in different mode.
   */
  abstract fun motionWithoutKeyModel(editor: FimEditor, context: ExecutionContext, cmd: Command)
}

/**
 * Handler for NON-SHIFTED special keys, that are defined in `:h keymodel`
 * There are: cursor keys, <End>, <Home>, <PageUp> and <PageDown>
 *
 * This handler is used to properly handle there keys according to current `keymodel` and `selectmode` options
 *
 * Handler is called for each caret
 */
abstract class NonShiftedSpecialKeyHandler : MotionActionHandler.ForEachCaret() {
  final override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val keymodel = (
      injector.optionService.getOptionValue(
        OptionScope.GLOBAL,
        OptionConstants.keymodelName
      ) as FimString
      ).value.split(",")
    if (editor.inSelectMode && (OptionConstants.keymodel_stopsel in keymodel || OptionConstants.keymodel_stopselect in keymodel)) {
      editor.exitSelectModeNative(false)
    }
    if (editor.inVisualMode && (OptionConstants.keymodel_stopsel in keymodel || OptionConstants.keymodel_stopvisual in keymodel)) {
      editor.exitVisualModeNative()
    }

    return offset(editor, caret, context, operatorArguments.count1, operatorArguments.count0, argument).toMotionOrError()
  }

  /**
   * Calculate new offset for current [caret]
   */
  abstract fun offset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    count: Int,
    rawCount: Int,
    argument: Argument?,
  ): Int
}
