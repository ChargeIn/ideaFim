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

package com.flop.idea.fim.action.motion.select

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.pushSelectMode
import com.flop.idea.fim.helper.pushVisualMode
import com.flop.idea.fim.helper.fimStateMachine

/**
 * @author Alex Plate
 */

class SelectToggleVisualMode : FimActionHandler.SingleExecution() {

  override val type: Command.Type = Command.Type.OTHER_READONLY

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    toggleMode(editor)
    return true
  }

  companion object {
    fun toggleMode(editor: FimEditor) {
      val commandState = editor.fimStateMachine
      val subMode = commandState.subMode
      val mode = commandState.mode
      commandState.popModes()
      if (mode.inVisualMode) {
        commandState.pushSelectMode(subMode, mode)
        if (subMode != FimStateMachine.SubMode.VISUAL_LINE) {
          editor.nativeCarets().forEach {
            if (it.offset.point + injector.visualMotionGroup.selectionAdj == it.selectionEnd) {
              it.moveToInlayAwareOffset(it.offset.point + injector.visualMotionGroup.selectionAdj)
            }
          }
        }
      } else {
        commandState.pushVisualMode(subMode, mode)
        if (subMode != FimStateMachine.SubMode.VISUAL_LINE) {
          editor.nativeCarets().forEach {
            if (it.offset.point == it.selectionEnd && it.visualLineStart <= it.offset.point - injector.visualMotionGroup.selectionAdj) {
              it.moveToInlayAwareOffset(it.offset.point - injector.visualMotionGroup.selectionAdj)
            }
          }
        }
      }
    }
  }
}
