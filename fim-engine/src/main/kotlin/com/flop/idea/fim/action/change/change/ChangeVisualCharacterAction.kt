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
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.handler.VisualOperatorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

/**
 * @author vlan
 */
class ChangeVisualCharacterAction : VisualOperatorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.CHANGE

  override val argumentType: Argument.Type = Argument.Type.DIGRAPH

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_ALLOW_DIGRAPH, CommandFlags.FLAG_EXIT_VISUAL)

  override fun executeAction(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    range: FimSelection,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val argument = cmd.argument
    return argument != null &&
      changeCharacterRange(editor, range.toFimTextRange(false), argument.character)
  }
}

private val logger = fimLogger<ChangeVisualCharacterAction>()

/**
 * Each character in the supplied range gets replaced with the character ch
 *
 * @param editor The editor to change
 * @param range  The range to change
 * @param ch     The replacing character
 * @return true if able to change the range, false if not
 */
private fun changeCharacterRange(editor: FimEditor, range: TextRange, ch: Char): Boolean {
  logger.debug { "change range: $range to $ch" }
  val chars = editor.text()
  val starts = range.startOffsets
  val ends = range.endOffsets
  for (j in ends.indices.reversed()) {
    for (i in starts[j] until ends[j]) {
      if (i < chars.length && '\n' != chars[i]) {
        injector.changeGroup.replaceText(editor, i, i + 1, Character.toString(ch))
      }
    }
  }
  return true
}
