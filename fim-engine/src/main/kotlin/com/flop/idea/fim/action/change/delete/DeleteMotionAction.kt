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
import com.flop.idea.fim.command.DuplicableOperatorAction
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.handler.ChangeEditorActionHandler
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope

class DeleteMotionAction : ChangeEditorActionHandler.ForEachCaret(), DuplicableOperatorAction {
  override val type: Command.Type = Command.Type.DELETE

  override val argumentType: Argument.Type = Argument.Type.MOTION

  override val duplicateWith: Char = 'd'

  override fun execute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (argument == null) return false
    if (injector.optionService.isSet(OptionScope.GLOBAL, OptionConstants.experimentalapiName)) {
      val (first, second) = injector.changeGroup
        .getDeleteRangeAndType2(editor, caret, context, argument, false, operatorArguments)
        ?: return false
      return injector.changeGroup.deleteRange2(editor, caret, first, second)
    } else {
      val (first, second) = injector.changeGroup
        .getDeleteRangeAndType(editor, caret, context, argument, false, operatorArguments)
        ?: return false
      return injector.changeGroup.deleteRange(editor, caret, first, second, false, operatorArguments)
    }
  }
}
