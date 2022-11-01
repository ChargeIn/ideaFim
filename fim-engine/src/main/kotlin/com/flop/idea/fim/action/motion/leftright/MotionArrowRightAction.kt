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
import com.flop.idea.fim.handler.NonShiftedSpecialKeyHandler
import com.flop.idea.fim.helper.isEndAllowed
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

class MotionArrowRightAction : NonShiftedSpecialKeyHandler(), ComplicatedKeysAction {
  override val motionType: MotionType = MotionType.EXCLUSIVE

  override val keyStrokesSet: Set<List<KeyStroke>> = setOf(
    injector.parser.parseKeys("<Right>"),
    listOf(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0))
  )

  override fun offset(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    count: Int,
    rawCount: Int,
    argument: Argument?,
  ): Int {
    val allowPastEnd = editor.isEndAllowed
    return injector.motion.getOffsetOfHorizontalMotion(editor, caret, count, allowPastEnd)
  }
}
