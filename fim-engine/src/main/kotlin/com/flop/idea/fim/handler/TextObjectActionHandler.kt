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
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.TextObjectVisualType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.endOffsetInclusive
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.subMode

/**
 * @author Alex Plate
 *
 * Handler for TextObjects.
 *
 * This handler gets executed for each caret.
 */
abstract class TextObjectActionHandler : EditorActionHandlerBase(true) {

  final override val type: Command.Type = Command.Type.MOTION

  /**
   * Visual mode that works for this text object.
   * E.g. In visual line-wise mode, `aw` will switch to character mode.
   *   In visual character mode, `ip` will switch to line-wise mode.
   */
  abstract val visualType: TextObjectVisualType

  abstract fun getRange(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    count: Int,
    rawCount: Int,
    argument: Argument?,
  ): TextRange?

  /**
   * This code is called when user executes text object in visual mode. E.g. `va(a(a(`
   */
  final override fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (!editor.inVisualMode) return true

    val range = getRange(editor, caret, context, cmd.count, cmd.rawCount, cmd.argument) ?: return false

    val block = CommandFlags.FLAG_TEXT_BLOCK in cmd.flags
    val newstart = if (block || caret.offset.point >= caret.fimSelectionStart) range.startOffset else range.endOffsetInclusive
    val newend = if (block || caret.offset.point >= caret.fimSelectionStart) range.endOffsetInclusive else range.startOffset

    if (caret.fimSelectionStart == caret.offset.point || block) {
      caret.fimSetSelection(newstart, newstart, false)
    }

    if (visualType == TextObjectVisualType.LINE_WISE && editor.subMode != FimStateMachine.SubMode.VISUAL_LINE) {
      injector.visualMotionGroup.toggleVisual(editor, 1, 0, FimStateMachine.SubMode.VISUAL_LINE)
    } else if (visualType != TextObjectVisualType.LINE_WISE && editor.subMode == FimStateMachine.SubMode.VISUAL_LINE) {
      injector.visualMotionGroup.toggleVisual(editor, 1, 0, FimStateMachine.SubMode.VISUAL_CHARACTER)
    }

    caret.moveToOffset(newend)

    return true
  }
}
