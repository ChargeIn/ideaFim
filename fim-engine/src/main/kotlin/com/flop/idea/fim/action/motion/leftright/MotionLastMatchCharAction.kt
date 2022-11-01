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

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler

class MotionLastMatchCharAction : MotionActionHandler.ForEachCaret() {
  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val repeatLastMatchChar = injector.motion.repeatLastMatchChar(editor, caret, operatorArguments.count1)
    return if (repeatLastMatchChar < 0) Motion.Error else Motion.AbsoluteOffset(repeatLastMatchChar)
  }

  override val motionType: MotionType = MotionType.EXCLUSIVE
}

class MotionLastMatchCharReverseAction : MotionActionHandler.ForEachCaret() {
  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    val repeatLastMatchChar = injector.motion.repeatLastMatchChar(editor, caret, -operatorArguments.count1)
    return if (repeatLastMatchChar < 0) Motion.Error else Motion.AbsoluteOffset(repeatLastMatchChar)
  }

  override val motionType: MotionType = MotionType.EXCLUSIVE
}
