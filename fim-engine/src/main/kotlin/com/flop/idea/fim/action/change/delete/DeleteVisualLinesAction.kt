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
package com.flop.idea.fim.action.change.delete

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
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
class DeleteVisualLinesAction : VisualOperatorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.DELETE

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_MOT_LINEWISE, CommandFlags.FLAG_EXIT_VISUAL)

  override fun executeAction(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    range: FimSelection,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val textRange = range.toFimTextRange(false)
    val (usedCaret, usedRange, usedType) = when (range.type) {
      SelectionType.BLOCK_WISE -> Triple(editor.primaryCaret(), textRange, range.type)
      SelectionType.LINE_WISE -> Triple(caret, textRange, SelectionType.LINE_WISE)
      SelectionType.CHARACTER_WISE -> {
        val lineEndForOffset = injector.engineEditorHelper.getLineEndForOffset(editor, textRange.endOffset)
        val endsWithNewLine = if (lineEndForOffset.toLong() == editor.fileSize()) 0 else 1
        val lineRange = TextRange(
          injector.engineEditorHelper.getLineStartForOffset(editor, textRange.startOffset),
          lineEndForOffset + endsWithNewLine
        )
        Triple(caret, lineRange, SelectionType.LINE_WISE)
      }
    }
    return injector.changeGroup.deleteRange(editor, usedCaret, usedRange, usedType, false, operatorArguments)
  }
}
