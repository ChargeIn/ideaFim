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

package com.flop.idea.fim.handler

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments

/**
 * Handler for common usage.
 *
 * Use subclasses of this handler:
 *   - [FimActionHandler.SingleExecution]
 *   - [FimActionHandler.ForEachCaret]
 */
sealed class FimActionHandler(myRunForEachCaret: Boolean) : EditorActionHandlerBase(myRunForEachCaret) {
  /**
   * This handler executes an action for each caret. That means that if you have 5 carets,
   *   [execute] will be called 5 times.
   * @see [FimActionHandler.SingleExecution] for only one execution.
   */
  abstract class ForEachCaret : FimActionHandler(true) {
    abstract fun execute(
      editor: FimEditor,
      caret: FimCaret,
      context: ExecutionContext,
      cmd: Command,
      operatorArguments: OperatorArguments,
    ): Boolean
  }

  /**
   * This handler executes an action only once for all carets. That means that if you have 5 carets,
   *   [execute] will be called 1 time.
   * @see [FimActionHandler.ForEachCaret] for per-caret execution.
   */
  abstract class SingleExecution : FimActionHandler(false) {
    abstract fun execute(
      editor: FimEditor,
      context: ExecutionContext,
      cmd: Command,
      operatorArguments: OperatorArguments,
    ): Boolean
  }

  abstract class ConditionalMulticaret : FimActionHandler(false) {
    abstract fun runAsMulticaret(
      editor: FimEditor,
      context: ExecutionContext,
      cmd: Command,
      operatorArguments: OperatorArguments,
    ): Boolean

    abstract fun execute(
      editor: FimEditor,
      caret: FimCaret,
      context: ExecutionContext,
      cmd: Command,
      operatorArguments: OperatorArguments,
    ): Boolean

    abstract fun execute(
      editor: FimEditor,
      context: ExecutionContext,
      cmd: Command,
      operatorArguments: OperatorArguments,
    ): Boolean
  }

  final override fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments
  ): Boolean {
    return when (this) {
      is ForEachCaret -> execute(editor, caret, context, cmd, operatorArguments)
      is SingleExecution -> execute(editor, context, cmd, operatorArguments)
      is ConditionalMulticaret -> {
        val runAsMulticaret = runAsMulticaret(editor, context, cmd, operatorArguments)
        return if (runAsMulticaret) {
          var res = true
          editor.forEachCaret { res = execute(editor, it, context, cmd, operatorArguments) && res }
          res
        } else {
          execute(editor, context, cmd, operatorArguments)
        }
      }
    }
  }
}
