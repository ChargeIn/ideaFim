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

package com.flop.idea.fim.fimscript.model.statements

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.FinishException
import com.flop.idea.fim.fimscript.model.Executable
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext

data class TryStatement(val tryBlock: TryBlock, val catchBlocks: List<CatchBlock>, val finallyBlock: FinallyBlock?) :
  Executable {
  override lateinit var fimContext: FimLContext

  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    var uncaughtException: ExException? = null
    var result: ExecutionResult = ExecutionResult.Success
    try {
      tryBlock.fimContext = this
      result = tryBlock.execute(editor, context)
      if (result !is ExecutionResult.Success) {
        return result
      }
    } catch (e: ExException) {
      if (e is FinishException) {
        if (finallyBlock != null) {
          finallyBlock.fimContext = this
          finallyBlock.execute(editor, context)
        }
        throw e
      }

      var caught = false
      for (catchBlock in catchBlocks) {
        catchBlock.fimContext = this
        if (injector.regexpService.matches(catchBlock.pattern, e.message)) {
          caught = true
          result = catchBlock.execute(editor, context)
          if (result !is ExecutionResult.Success) {
            return result
          }
          break
        }
      }
      if (!caught) {
        uncaughtException = e
      }
    }
    if (finallyBlock != null) {
      finallyBlock.fimContext = this
      result = finallyBlock.execute(editor, context)
    }
    if (uncaughtException != null) {
      throw uncaughtException
    }
    return result
  }
}

data class TryBlock(val body: List<Executable>) : Executable {
  override lateinit var fimContext: FimLContext
  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    body.forEach { it.fimContext = this.fimContext }
    return executeBody(body, editor, context)
  }
}

data class CatchBlock(val pattern: String, val body: List<Executable>) : Executable {
  override lateinit var fimContext: FimLContext
  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    body.forEach { it.fimContext = this.fimContext }
    return executeBody(body, editor, context)
  }
}

data class FinallyBlock(val body: List<Executable>) : Executable {
  override lateinit var fimContext: FimLContext
  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    body.forEach { it.fimContext = this.fimContext }
    return executeBody(body, editor, context)
  }
}

fun executeBody(
  body: List<Executable>,
  editor: FimEditor,
  context: ExecutionContext,
): ExecutionResult {
  var result: ExecutionResult = ExecutionResult.Success
  for (statement in body) {
    if (result is ExecutionResult.Success) {
      result = statement.execute(editor, context)
    } else {
      break
    }
  }
  return result
}
