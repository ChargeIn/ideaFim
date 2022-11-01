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
package com.flop.idea.fim.action.change.change

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.CommandFlags.FLAG_EXIT_VISUAL
import com.flop.idea.fim.command.CommandFlags.FLAG_MOT_LINEWISE
import com.flop.idea.fim.command.CommandFlags.FLAG_MULTIKEY_UNDO
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.handler.VisualOperatorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

/**
 * @author vlan
 */
class ChangeVisualLinesEndAction : VisualOperatorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.CHANGE

  override val flags: EnumSet<CommandFlags> = enumSetOf(FLAG_MOT_LINEWISE, FLAG_MULTIKEY_UNDO, FLAG_EXIT_VISUAL)

  override fun executeAction(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    range: FimSelection,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val fimTextRange = range.toFimTextRange(true)
    return if (range.type == SelectionType.BLOCK_WISE && fimTextRange.isMultiple) {
      val starts = fimTextRange.startOffsets
      val ends = fimTextRange.endOffsets
      for (i in starts.indices) {
        if (ends[i] > starts[i]) {
          ends[i] = injector.engineEditorHelper.getLineEndForOffset(editor, starts[i])
        }
      }
      val blockRange = TextRange(starts, ends)
      injector.changeGroup.changeRange(editor, caret, blockRange, SelectionType.BLOCK_WISE, context, operatorArguments)
    } else {
      val lineEndForOffset = injector.engineEditorHelper.getLineEndForOffset(editor, fimTextRange.endOffset)
      val endsWithNewLine = if (lineEndForOffset.toLong() == editor.fileSize()) 0 else 1
      val lineRange = TextRange(
        injector.engineEditorHelper.getLineStartForOffset(editor, fimTextRange.startOffset),
        lineEndForOffset + endsWithNewLine
      )
      injector.changeGroup.changeRange(editor, caret, lineRange, SelectionType.LINE_WISE, context, operatorArguments)
    }
  }
}
