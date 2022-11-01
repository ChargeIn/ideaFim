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
package com.flop.idea.fim.action.copy

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.handler.VisualOperatorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

/**
 * @author vlan
 */
class YankVisualAction : VisualOperatorActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.COPY

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_EXIT_VISUAL)

  override fun executeForAllCarets(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    caretsAndSelections: Map<FimCaret, FimSelection>,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val selections = caretsAndSelections.values
    val starts: MutableList<Int> = ArrayList()
    val ends: MutableList<Int> = ArrayList()
    selections.forEach { selection: FimSelection ->
      val textRange = selection.toFimTextRange(false)
      textRange.startOffsets.forEach { e: Int -> starts.add(e) }
      textRange.endOffsets.forEach { e: Int -> ends.add(e) }
    }
    val fimSelection = selections.firstOrNull() ?: return false
    val startsArray = starts.toIntArray()
    val endsArray = ends.toIntArray()
    return injector.yank.yankRange(editor, TextRange(startsArray, endsArray), fimSelection.type, true)
  }
}
