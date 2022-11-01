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

package com.flop.idea.fim.action.motion.leftright

import com.flop.idea.fim.action.ComplicatedKeysAction
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

class MotionLeftAction : MotionActionHandler.ForEachCaret() {
  override val motionType: MotionType = MotionType.EXCLUSIVE

  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val offsetOfHorizontalMotion =
      injector.motion.getOffsetOfHorizontalMotion(editor, caret, -operatorArguments.count1, false)
    return if (offsetOfHorizontalMotion < 0) Motion.Error else Motion.AbsoluteOffset(offsetOfHorizontalMotion)
  }
}

class MotionLeftInsertModeAction : MotionActionHandler.ForEachCaret(), ComplicatedKeysAction {
  override val motionType: MotionType = MotionType.EXCLUSIVE

  override val keyStrokesSet: Set<List<KeyStroke>> = setOf(
    listOf(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)),
    listOf(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0))
  )

  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val offsetOfHorizontalMotion =
      injector.motion.getOffsetOfHorizontalMotion(editor, caret, -operatorArguments.count1, false)
    return if (offsetOfHorizontalMotion < 0) Motion.Error else Motion.AbsoluteOffset(offsetOfHorizontalMotion)
  }
}
