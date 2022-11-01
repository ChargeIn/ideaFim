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

package com.flop.idea.fim.action.motion.visual

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.inBlockSubMode

/**
 * @author vlan
 */
class VisualSwapEndsAction : FimActionHandler.ForEachCaret() {

  override val type: Command.Type = Command.Type.OTHER_READONLY

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments
  ): Boolean = swapVisualEnds(caret)
}

/**
 * @author vlan
 */
class VisualSwapEndsBlockAction : FimActionHandler.SingleExecution() {

  override val type: Command.Type = Command.Type.OTHER_READONLY

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (editor.inBlockSubMode) {
      return swapVisualEndsBigO(editor)
    }

    var ret = true
    for (caret in editor.carets()) {
      ret = ret && swapVisualEnds(caret)
    }
    return ret
  }
}

private fun swapVisualEnds(caret: FimCaret): Boolean {
  val fimSelectionStart = caret.fimSelectionStart
  caret.fimSelectionStart = caret.offset.point

  caret.moveToOffset(fimSelectionStart)

  return true
}

private fun swapVisualEndsBigO(editor: FimEditor): Boolean {
  val caret = editor.primaryCaret()
  val anotherSideCaret = editor.nativeCarets().let { if (it.first() == caret) it.last() else it.first() }

  val adj = injector.visualMotionGroup.selectionAdj

  if (caret.offset.point == caret.selectionStart) {
    caret.fimSelectionStart = anotherSideCaret.selectionStart
    caret.moveToOffset(caret.selectionEnd - adj)
  } else {
    caret.fimSelectionStart = anotherSideCaret.selectionEnd - adj
    caret.moveToOffset(caret.selectionStart)
  }

  return true
}
