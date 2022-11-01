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

package com.flop.idea.fim.fimscript.model.statements.loops

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.Executable
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimBlob
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.Variable

// todo refactor us senpai :(
data class ForLoop(val variable: Variable, val iterable: Expression, val body: List<Executable>) : Executable {
  override lateinit var fimContext: FimLContext

  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    injector.statisticsService.setIfLoopUsed(true)
    var result: ExecutionResult = ExecutionResult.Success
    body.forEach { it.fimContext = this }

    var iterableValue = iterable.evaluate(editor, context, this)
    if (iterableValue is FimString) {
      for (i in iterableValue.value) {
        injector.variableService.storeVariable(variable, FimString(i.toString()), editor, context, this)
        for (statement in body) {
          if (result is ExecutionResult.Success) {
            result = statement.execute(editor, context)
          } else {
            break
          }
        }
        if (result is ExecutionResult.Break) {
          result = ExecutionResult.Success
          break
        } else if (result is ExecutionResult.Continue) {
          result = ExecutionResult.Success
          continue
        } else if (result is ExecutionResult.Error) {
          break
        }
      }
    } else if (iterableValue is FimList) {
      var index = 0
      while (index < (iterableValue as FimList).values.size) {
        injector.variableService.storeVariable(variable, iterableValue.values[index], editor, context, this)
        for (statement in body) {
          if (result is ExecutionResult.Success) {
            result = statement.execute(editor, context)
          } else {
            break
          }
        }
        if (result is ExecutionResult.Break) {
          result = ExecutionResult.Success
          break
        } else if (result is ExecutionResult.Continue) {
          result = ExecutionResult.Success
          continue
        } else if (result is ExecutionResult.Error) {
          break
        }
        index += 1
        iterableValue = iterable.evaluate(editor, context, this) as FimList
      }
    } else if (iterableValue is FimBlob) {
      TODO("Not yet implemented")
    } else {
      throw ExException("E1098: String, List or Blob required")
    }
    return result
  }
}

data class ForLoopWithList(val variables: List<String>, val iterable: Expression, val body: List<Executable>) :
  Executable {
  override lateinit var fimContext: FimLContext

  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    var result: ExecutionResult = ExecutionResult.Success
    body.forEach { it.fimContext = this }

    var iterableValue = iterable.evaluate(editor, context, this)
    if (iterableValue is FimList) {
      var index = 0
      while (index < (iterableValue as FimList).values.size) {
        storeListVariables(iterableValue.values[index], editor, context)
        for (statement in body) {
          if (result is ExecutionResult.Success) {
            result = statement.execute(editor, context)
          } else {
            break
          }
        }
        if (result is ExecutionResult.Break) {
          result = ExecutionResult.Success
          break
        } else if (result is ExecutionResult.Continue) {
          result = ExecutionResult.Success
          continue
        } else if (result is ExecutionResult.Error) {
          break
        }
        index += 1
        iterableValue = iterable.evaluate(editor, context, this) as FimList
      }
    } else {
      throw ExException("E714: List required")
    }
    return result
  }

  private fun storeListVariables(list: FimDataType, editor: FimEditor, context: ExecutionContext) {
    if (list !is FimList) {
      throw ExException("E714: List required")
    }

    if (list.values.size < variables.size) {
      throw ExException("E688: More targets than List items")
    }
    if (list.values.size > variables.size) {
      throw ExException("E684: Less targets than List items")
    }

    for (item in list.values.withIndex()) {
      injector.variableService.storeVariable(Variable(null, variables[item.index]), item.value, editor, context, this)
    }
  }
}
