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
import com.flop.idea.fim.fimscript.model.expressions.Variable

/**
 * see :h lockvar
 */
class LockVarCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  // todo doesn't throw proper fim exceptions in case of wrong arguments
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val variableAndDepth = parseVariableAndDepth(argument)
    injector.variableService.lockVariable(variableAndDepth.first, variableAndDepth.second, editor, context, fimContext)
    return ExecutionResult.Success
  }
}

/**
 * see :h unlockvar
 */
class UnlockVarCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  // todo doesn't throw proper fim exceptions in case of wrong arguments
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val variableAndDepth = parseVariableAndDepth(argument)
    injector.variableService.unlockVariable(variableAndDepth.first, variableAndDepth.second, editor, context, fimContext)
    return ExecutionResult.Success
  }
}

private fun parseVariableAndDepth(argument: String): Pair<Variable, Int> {
  val variable: String
  var arg = argument
  var depth = 2
  if (arg.startsWith("!")) {
    depth = 100
    arg = arg.substring(1)
  }
  val splittedArg = arg.trim().split(" ")
  when (splittedArg.size) {
    1 -> variable = splittedArg[0]
    2 -> {
      depth = splittedArg[0].toIntOrNull() ?: 2
      variable = splittedArg[1]
    }
    else -> throw ExException("Unknown error during lockvar command execution")
  }
  return Pair(parseVariable(variable), depth)
}

private fun parseVariable(variable: String): Variable {
  val splittedString = variable.split(":")
  return when (splittedString.size) {
    1 -> Variable(null, splittedString[0])
    2 -> Variable(Scope.getByValue(splittedString[0]), splittedString[1])
    else -> throw ExException("Unknown error during lockvar command execution")
  }
}
