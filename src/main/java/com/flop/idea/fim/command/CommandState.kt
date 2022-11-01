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

package com.flop.idea.fim.command

import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim

/**
 * COMPATIBILITY-LAYER: Additional class
 * Please see: https://jb.gg/zo8n0r
 */
class CommandState(private val machine: FimStateMachine) {

  val isOperatorPending: Boolean
    get() = machine.isOperatorPending

  val mode: CommandState.Mode
    get() = machine.mode.ij

  val commandBuilder: CommandBuilder
    get() = machine.commandBuilder

  val mappingState: MappingState
    get() = machine.mappingState

  enum class Mode {
    // Basic modes
    COMMAND, VISUAL, SELECT, INSERT, CMD_LINE, /*EX*/

    // Additional modes
    OP_PENDING, REPLACE /*, VISUAL_REPLACE*/, INSERT_NORMAL, INSERT_VISUAL, INSERT_SELECT
  }

  enum class SubMode {
    NONE, VISUAL_CHARACTER, VISUAL_LINE, VISUAL_BLOCK
  }

  companion object {
    @JvmStatic
    fun getInstance(editor: Editor): CommandState {
      return CommandState(editor.fim.fimStateMachine)
    }
  }
}

val CommandState.SubMode.engine: FimStateMachine.SubMode
  get() = when (this) {
    CommandState.SubMode.NONE -> FimStateMachine.SubMode.NONE
    CommandState.SubMode.VISUAL_CHARACTER -> FimStateMachine.SubMode.VISUAL_CHARACTER
    CommandState.SubMode.VISUAL_LINE -> FimStateMachine.SubMode.VISUAL_LINE
    CommandState.SubMode.VISUAL_BLOCK -> FimStateMachine.SubMode.VISUAL_BLOCK
  }

val CommandState.Mode.engine: FimStateMachine.Mode
  get() = when (this) {
    CommandState.Mode.COMMAND -> FimStateMachine.Mode.COMMAND
    CommandState.Mode.VISUAL -> FimStateMachine.Mode.VISUAL
    CommandState.Mode.SELECT -> FimStateMachine.Mode.SELECT
    CommandState.Mode.INSERT -> FimStateMachine.Mode.INSERT
    CommandState.Mode.CMD_LINE -> FimStateMachine.Mode.CMD_LINE
    CommandState.Mode.OP_PENDING -> FimStateMachine.Mode.OP_PENDING
    CommandState.Mode.REPLACE -> FimStateMachine.Mode.REPLACE
    CommandState.Mode.INSERT_NORMAL -> FimStateMachine.Mode.INSERT_NORMAL
    CommandState.Mode.INSERT_VISUAL -> FimStateMachine.Mode.INSERT_VISUAL
    CommandState.Mode.INSERT_SELECT -> FimStateMachine.Mode.INSERT_SELECT
  }

val FimStateMachine.Mode.ij: CommandState.Mode
  get() = when (this) {
    FimStateMachine.Mode.COMMAND -> CommandState.Mode.COMMAND
    FimStateMachine.Mode.VISUAL -> CommandState.Mode.VISUAL
    FimStateMachine.Mode.SELECT -> CommandState.Mode.SELECT
    FimStateMachine.Mode.INSERT -> CommandState.Mode.INSERT
    FimStateMachine.Mode.CMD_LINE -> CommandState.Mode.CMD_LINE
    FimStateMachine.Mode.OP_PENDING -> CommandState.Mode.OP_PENDING
    FimStateMachine.Mode.REPLACE -> CommandState.Mode.REPLACE
    FimStateMachine.Mode.INSERT_NORMAL -> CommandState.Mode.INSERT_NORMAL
    FimStateMachine.Mode.INSERT_VISUAL -> CommandState.Mode.INSERT_VISUAL
    FimStateMachine.Mode.INSERT_SELECT -> CommandState.Mode.INSERT_SELECT
  }
