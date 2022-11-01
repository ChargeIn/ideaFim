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

import com.flop.idea.fim.action.change.FimRepeater
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments

/**
 * Base handler for commands that performs change actions.
 * This handler stores the commands and they can be repeated later with dot command.
 *
 * Use subclasses of this handler:
 *   - [ChangeEditorActionHandler.SingleExecution]
 *   - [ChangeEditorActionHandler.ForEachCaret]
 */
sealed class ChangeEditorActionHandler : EditorActionHandlerBase(false) {

  /**
   * This handler executes an action for each caret. That means that if you have 5 carets, [execute] will be
   *   called 5 times.
   * @see [ChangeEditorActionHandler.SingleExecution] for only one execution.
   */
  abstract class ForEachCaret : ChangeEditorActionHandler() {
    abstract fun execute(
      editor: FimEditor,
      caret: FimCaret,
      context: ExecutionContext,
      argument: Argument?,
      operatorArguments: OperatorArguments,
    ): Boolean
  }

  /**
   * This handler executes an action only once for all carets. That means that if you have 5 carets,
   *   [execute] will be called 1 time.
   * @see [ChangeEditorActionHandler.ForEachCaret] for per-caret execution
   */
  abstract class SingleExecution : ChangeEditorActionHandler() {
    abstract fun execute(
      editor: FimEditor,
      context: ExecutionContext,
      argument: Argument?,
      operatorArguments: OperatorArguments,
    ): Boolean
  }

  final override fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    // Here we have to save the last changed command. This should be done separately for each
    // call of the task, not for each caret. Currently there is no way to schedule any action
    // to be worked after each task. So here we override the deprecated execute function which
    // is called for each task and call the handlers for each caret, if implemented.

    // Shouldn't we just use [EditorWriteActionHandler]?
    editor.fimChangeActionSwitchMode = null

    editor.startGuardedBlockChecking()

    val worked = arrayOf(true)
    try {
      when (this) {
        is ForEachCaret -> {
          editor.forEachNativeCaret(
            { current ->
              if (!current.isValid) return@forEachNativeCaret
              if (!execute(editor, current, context, cmd.argument, operatorArguments)) {
                worked[0] = false
              }
            },
            true
          )
        }
        is SingleExecution -> {
          worked[0] = execute(editor, context, cmd.argument, operatorArguments)
        }
      }
    } catch (e: java.lang.Exception) {
      if (injector.application.isUnitTest() || e.javaClass.name != "ReadOnlyFragmentModificationException") {
        throw e
      } else {
        injector.engineEditorHelper.handleWithReadonlyFragmentModificationHandler(editor, e)
      }
    } finally {
      editor.stopGuardedBlockChecking()
    }

    if (worked[0]) {
      FimRepeater.saveLastChange(cmd)
      FimRepeater.repeatHandler = false
      editor.forEachNativeCaret({ it.fimLastColumn = it.getVisualPosition().column })
    }

    val toSwitch = editor.fimChangeActionSwitchMode
    if (toSwitch != null) {
      injector.changeGroup.processPostChangeModeSwitch(editor, context, toSwitch)
    }

    return worked[0]
  }
}
