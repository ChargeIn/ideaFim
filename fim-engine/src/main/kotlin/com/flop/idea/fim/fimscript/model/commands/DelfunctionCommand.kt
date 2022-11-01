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
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.expressions.Scope

/**
 * see "h :delfunction"
 */
data class DelfunctionCommand(
  val ranges: Ranges,
  val scope: Scope?,
  val name: String,
  val ignoreIfMissing: Boolean,
) : Command.SingleExecution(ranges) {

  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    if (ignoreIfMissing) {
      try {
        injector.functionService.deleteFunction(name, scope, this)
      } catch (e: ExException) {
        if (e.message != null && e.message!!.startsWith("E130")) {
          // "ignoreIfMissing" flag handles the "E130: Unknown function" exception
        } else {
          throw e
        }
      }
    } else {
      injector.functionService.deleteFunction(name, scope, this)
    }
    return ExecutionResult.Success
  }
}
