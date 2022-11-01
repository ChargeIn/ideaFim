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
package com.flop.idea.fim.action.motion.gn

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler
import com.flop.idea.fim.handler.toMotionOrError
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.noneOfEnum
import java.util.*
import kotlin.math.max

class VisualSelectNextSearch : MotionActionHandler.SingleExecution() {
  override val flags: EnumSet<CommandFlags> = noneOfEnum()

  override fun getOffset(
    editor: FimEditor,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    return selectNextSearch(editor, operatorArguments.count1, true).toMotionOrError()
  }

  override val motionType: MotionType = MotionType.EXCLUSIVE
}

class VisualSelectPreviousSearch : MotionActionHandler.SingleExecution() {
  override val flags: EnumSet<CommandFlags> = noneOfEnum()

  override fun getOffset(
    editor: FimEditor,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    return selectNextSearch(editor, operatorArguments.count1, false).toMotionOrError()
  }

  override val motionType: MotionType = MotionType.EXCLUSIVE
}

private fun selectNextSearch(editor: FimEditor, count: Int, forwards: Boolean): Int {
  val caret = editor.primaryCaret()
  val range = injector.searchGroup.getNextSearchRange(editor, count, forwards) ?: return -1
  val adj = injector.visualMotionGroup.selectionAdj
  if (!editor.inVisualMode) {
    val startOffset = if (forwards) range.startOffset else max(range.endOffset - adj, 0)
    caret.moveToOffset(startOffset)
    injector.visualMotionGroup.enterVisualMode(editor, FimStateMachine.SubMode.VISUAL_CHARACTER)
  }
  return if (forwards) max(range.endOffset - adj, 0) else range.startOffset
}
