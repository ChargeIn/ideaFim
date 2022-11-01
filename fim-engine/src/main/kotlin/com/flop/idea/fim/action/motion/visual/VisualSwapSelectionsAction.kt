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
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.subMode

/**
 * @author vlan
 */
class VisualSwapSelectionsAction : FimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_READONLY

  // FIXME: 2019-03-05 Make it multicaret
  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    return swapVisualSelections(editor)
  }
}

private fun swapVisualSelections(editor: FimEditor): Boolean {
  val lastSelectionType = editor.fimLastSelectionType ?: return false

  val lastVisualRange = injector.markGroup.getVisualSelectionMarks(editor) ?: return false
  val primaryCaret = editor.primaryCaret()
  editor.removeSecondaryCarets()
  val fimSelectionStart = primaryCaret.fimSelectionStart

  editor.fimLastSelectionType = SelectionType.fromSubMode(editor.subMode)
  injector.markGroup.setVisualSelectionMarks(editor, TextRange(fimSelectionStart, primaryCaret.offset.point))

  editor.subMode = lastSelectionType.toSubMode()
  primaryCaret.fimSetSelection(lastVisualRange.startOffset, lastVisualRange.endOffset, true)

  injector.motion.scrollCaretIntoView(editor)

  return true
}
