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
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.ChangeEditorActionHandler
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.services.IjFimOptionService

class DeleteJoinLinesAction : ChangeEditorActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.DELETE

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (editor.isOneLineMode()) return false
    if (injector.optionService.isSet(OptionScope.LOCAL(editor), IjFimOptionService.ideajoinName)) {
      return injector.changeGroup.joinViaIdeaByCount(editor, context, operatorArguments.count1)
    }
    injector.editorGroup.notifyIdeaJoin(editor)
    val res = arrayOf(true)
    editor.forEachNativeCaret(
      { caret: FimCaret ->
        if (!injector.changeGroup.deleteJoinLines(editor, caret, operatorArguments.count1, false, operatorArguments)) res[0] = false
      },
      true
    )
    return res[0]
  }
}
