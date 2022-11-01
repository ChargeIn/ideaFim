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
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.MotionType
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.Direction
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler
import com.flop.idea.fim.handler.toMotionOrError
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

enum class TillCharacterMotionType {
  LAST_F,
  LAST_SMALL_F,
  LAST_T,
  LAST_SMALL_T,
}

class MotionLeftMatchCharAction : TillCharacterMotion(Direction.BACKWARDS, TillCharacterMotionType.LAST_F, false)
class MotionLeftTillMatchCharAction : TillCharacterMotion(Direction.BACKWARDS, TillCharacterMotionType.LAST_T, true)
class MotionRightMatchCharAction : TillCharacterMotion(Direction.FORWARDS, TillCharacterMotionType.LAST_SMALL_F, false)
class MotionRightTillMatchCharAction :
  TillCharacterMotion(Direction.FORWARDS, TillCharacterMotionType.LAST_SMALL_T, true)

sealed class TillCharacterMotion(
  private val direction: Direction,
  private val tillCharacterMotionType: TillCharacterMotionType,
  private val finishBeforeCharacter: Boolean,
) : MotionActionHandler.ForEachCaret() {
  override val argumentType: Argument.Type = Argument.Type.DIGRAPH

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_ALLOW_DIGRAPH)

  override val motionType: MotionType =
    if (direction == Direction.BACKWARDS) MotionType.EXCLUSIVE else MotionType.INCLUSIVE

  override fun getOffset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Motion {
    if (argument == null) return Motion.Error
    val res = if (finishBeforeCharacter) {
      injector.motion
        .moveCaretToBeforeNextCharacterOnLine(
          editor,
          caret,
          direction.toInt() * operatorArguments.count1,
          argument.character
        )
    } else {
      injector.motion.moveCaretToNextCharacterOnLine(
        editor,
        caret,
        direction.toInt() * operatorArguments.count1,
        argument.character
      )
    }
    injector.motion.setLastFTCmd(tillCharacterMotionType, argument.character)
    return res.toMotionOrError()
  }
}
