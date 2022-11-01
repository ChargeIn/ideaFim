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

package com.flop.idea.fim.fimscript.model.commands

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.NativeAction
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * @author smartbomb
 */
data class ActionCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges) {

  override val argFlags = flags(
    RangeFlag.RANGE_OPTIONAL,
    ArgumentFlag.ARGUMENT_OPTIONAL,
    Access.READ_ONLY,
    Flag.SAVE_VISUAL
  )

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val actionName = argument.trim()
    val action = injector.actionExecutor.getAction(actionName) ?: throw ExException(injector.messages.message("action.not.found.0", actionName))
    if (injector.application.isUnitTest()) {
      executeAction(action, context)
    } else {
      injector.application.runAfterGotFocus { executeAction(action, context) }
    }
    return ExecutionResult.Success
  }

  private fun executeAction(action: NativeAction, context: ExecutionContext) {
    injector.actionExecutor.executeAction(action, context)
  }
}
