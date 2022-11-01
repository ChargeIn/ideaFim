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

@file:JvmName("CommandStateHelper")

package com.flop.idea.fim.helper

import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.CommandState
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.command.engine
import com.flop.idea.fim.command.ij
import com.flop.idea.fim.newapi.fim

val Editor.isEndAllowed: Boolean
  get() = when (this.editorMode) {
    FimStateMachine.Mode.INSERT, FimStateMachine.Mode.VISUAL, FimStateMachine.Mode.SELECT, FimStateMachine.Mode.INSERT_VISUAL, FimStateMachine.Mode.INSERT_SELECT -> true
    FimStateMachine.Mode.COMMAND, FimStateMachine.Mode.CMD_LINE, FimStateMachine.Mode.REPLACE, FimStateMachine.Mode.OP_PENDING, FimStateMachine.Mode.INSERT_NORMAL -> {
      // One day we'll use a proper insert_normal mode
      if (this.editorMode.inSingleMode) true else usesVirtualSpace
    }
  }

val FimStateMachine.Mode.isEndAllowedIgnoringOnemore: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT, FimStateMachine.Mode.VISUAL, FimStateMachine.Mode.SELECT -> true
    FimStateMachine.Mode.COMMAND, FimStateMachine.Mode.CMD_LINE, FimStateMachine.Mode.REPLACE, FimStateMachine.Mode.OP_PENDING -> false
    FimStateMachine.Mode.INSERT_NORMAL -> false
    FimStateMachine.Mode.INSERT_VISUAL -> true
    FimStateMachine.Mode.INSERT_SELECT -> true
  }

val FimStateMachine.Mode.hasVisualSelection
  get() = when (this) {
    FimStateMachine.Mode.VISUAL, FimStateMachine.Mode.SELECT -> true
    FimStateMachine.Mode.REPLACE, FimStateMachine.Mode.CMD_LINE, FimStateMachine.Mode.COMMAND, FimStateMachine.Mode.INSERT, FimStateMachine.Mode.OP_PENDING -> false
    FimStateMachine.Mode.INSERT_NORMAL -> false
    FimStateMachine.Mode.INSERT_VISUAL -> true
    FimStateMachine.Mode.INSERT_SELECT -> true
  }

val Editor.editorMode
  get() = this.fim.fimStateMachine.mode

/**
 * COMPATIBILITY-LAYER: New method
 * Please see: https://jb.gg/zo8n0r
 */
val Editor.mode
  get() = this.fim.fimStateMachine.mode.ij

/**
 * COMPATIBILITY-LAYER: New method
 * Please see: https://jb.gg/zo8n0r
 */
val CommandState.Mode.isEndAllowed: Boolean
  get() = this.engine.isEndAllowed

var Editor.subMode
  get() = this.fim.fimStateMachine.subMode
  set(value) {
    this.fim.fimStateMachine.subMode = value
  }

@get:JvmName("inNormalMode")
val Editor.inNormalMode
  get() = this.editorMode.inNormalMode

@get:JvmName("inNormalMode")
val FimStateMachine.Mode.inNormalMode
  get() = this == FimStateMachine.Mode.COMMAND || this == FimStateMachine.Mode.INSERT_NORMAL

@get:JvmName("inInsertMode")
val Editor.inInsertMode
  get() = this.editorMode == FimStateMachine.Mode.INSERT || this.editorMode == FimStateMachine.Mode.REPLACE

@get:JvmName("inRepeatMode")
val Editor.inRepeatMode
  get() = this.fim.fimStateMachine.isDotRepeatInProgress

@get:JvmName("inVisualMode")
val Editor.inVisualMode
  get() = this.editorMode.inVisualMode

@get:JvmName("inSelectMode")
val Editor.inSelectMode
  get() = this.editorMode == FimStateMachine.Mode.SELECT || this.editorMode == FimStateMachine.Mode.INSERT_SELECT

val FimEditor.inSelectMode
  get() = this.mode == FimStateMachine.Mode.SELECT || this.mode == FimStateMachine.Mode.INSERT_SELECT

@get:JvmName("inBlockSubMode")
val Editor.inBlockSubMode
  get() = this.subMode == FimStateMachine.SubMode.VISUAL_BLOCK

@get:JvmName("inSingleCommandMode")
val Editor.inSingleCommandMode: Boolean
  get() = this.editorMode.inSingleMode

@get:JvmName("inSingleMode")
val FimStateMachine.Mode.inSingleMode: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT_NORMAL, FimStateMachine.Mode.INSERT_SELECT, FimStateMachine.Mode.INSERT_VISUAL -> true
    else -> false
  }

@get:JvmName("inSingleNormalMode")
val FimStateMachine.Mode.inSingleNormalMode: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT_NORMAL -> true
    else -> false
  }
