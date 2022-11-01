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

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.handler.Motion
import com.flop.idea.fim.handler.MotionActionHandler
import java.util.*

/**
 * This represents a command argument.
 */
class Argument private constructor(
  val character: Char = 0.toChar(),
  val motion: Command = EMPTY_COMMAND,
  val offsets: Map<FimCaret, FimSelection> = emptyMap(),
  val string: String = "",
  val type: Type,
) {
  constructor(motionArg: Command) : this(motion = motionArg, type = Type.MOTION)
  constructor(charArg: Char) : this(character = charArg, type = Type.CHARACTER)
  constructor(strArg: String) : this(string = strArg, type = Type.EX_STRING)
  constructor(offsets: Map<FimCaret, FimSelection>) : this(offsets = offsets, type = Type.OFFSETS)

  enum class Type {
    MOTION, CHARACTER, DIGRAPH, EX_STRING, OFFSETS
  }

  companion object {
    @JvmField
    val EMPTY_COMMAND = Command(
      0,
      object : MotionActionHandler.SingleExecution() {
        override fun getOffset(
          editor: FimEditor,
          context: ExecutionContext,
          argument: Argument?,
          operatorArguments: OperatorArguments
        ) = Motion.NoMotion

        override val motionType: MotionType = MotionType.EXCLUSIVE
      },
      Command.Type.MOTION, EnumSet.noneOf(CommandFlags::class.java)
    )
  }
}
