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
package com.flop.idea.fim.action.motion.scroll

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

class MotionScrollLastScreenLinePageStartAction : FimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_READONLY

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_IGNORE_SCROLL_JUMP)

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val motion = injector.motion

    // Without [count]: Redraw with the line just above the window at the bottom of the window. Put the cursor in that
    // line, at the first non-blank in the line.
    if (cmd.rawCount == 0) {
      val prevVisualLine = injector.engineEditorHelper.normalizeVisualLine(
        editor,
        injector.engineEditorHelper.getVisualLineAtTopOfScreen(editor) - 1
      )
      val logicalLine = injector.engineEditorHelper.visualLineToLogicalLine(editor, prevVisualLine)
      return motion.scrollLineToLastScreenLine(editor, logicalLine + 1, true)
    }

    // [count]z^ first scrolls [count] to the bottom of the window, then moves the caret to the line that is now at
    // the top, and then move that line to the bottom of the window
    var logicalLine = injector.engineEditorHelper.normalizeLine(editor, cmd.rawCount - 1)
    if (motion.scrollLineToLastScreenLine(editor, logicalLine + 1, false)) {
      logicalLine = injector.engineEditorHelper.visualLineToLogicalLine(
        editor,
        injector.engineEditorHelper.getVisualLineAtTopOfScreen(editor)
      )
      return motion.scrollLineToLastScreenLine(editor, logicalLine + 1, true)
    }

    return false
  }
}
