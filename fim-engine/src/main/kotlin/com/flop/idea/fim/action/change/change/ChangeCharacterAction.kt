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
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.handler.ChangeEditorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

class ChangeCharacterAction : ChangeEditorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.CHANGE

  override val argumentType: Argument.Type = Argument.Type.DIGRAPH

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_ALLOW_DIGRAPH)

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    return argument != null && changeCharacter(editor, caret, operatorArguments.count1, argument.character)
  }
}

private val logger = fimLogger<ChangeCharacterAction>()

/**
 * Replace each of the next count characters with the character ch
 *
 * @param editor The editor to change
 * @param caret  The caret to perform action on
 * @param count  The number of characters to change
 * @param ch     The character to change to
 * @return true if able to change count characters, false if not
 */
private fun changeCharacter(editor: FimEditor, caret: FimCaret, count: Int, ch: Char): Boolean {
  val col = caret.getLogicalPosition().column
  val len = injector.engineEditorHelper.getLineLength(editor)
  val offset = caret.offset.point
  if (len - col < count) {
    return false
  }

  // Special case - if char is newline, only add one despite count
  var num = count
  var space: String? = null
  if (ch == '\n') {
    num = 1
    space = injector.engineEditorHelper.getLeadingWhitespace(editor, editor.offsetToLogicalPosition(offset).line)
    logger.debug { "space='$space'" }
  }
  val repl = StringBuilder(count)
  for (i in 0 until num) {
    repl.append(ch)
  }
  injector.changeGroup.replaceText(editor, offset, offset + count, repl.toString())

  // Indent new line if we replaced with a newline
  if (ch == '\n') {
    injector.changeGroup.insertText(editor, caret, offset + 1, space!!)
    var slen = space.length
    if (slen == 0) {
      slen++
    }
    caret.moveToInlayAwareOffset(offset + slen)
  }
  return true
}
