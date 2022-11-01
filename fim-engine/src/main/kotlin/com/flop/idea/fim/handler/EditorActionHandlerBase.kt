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
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.helper.noneOfEnum
import org.jetbrains.annotations.NonNls
import java.util.*
import javax.swing.KeyStroke

/**
 * All the commands in IdeaFim should implement one of the following handlers and be registered in FimActions.xml
 * Check the KtDocs of handlers for the details.
 *
 * Structure of handlers:
 *
 * - [EditorActionHandlerBase]: Base handler for all handlers. Please don't use it directly.
 *  - [FimActionHandler]: .............. Common fim commands.. E.g.: u, <C-W>s, <C-D>.
 *  - [TextObjectActionHandler]: ....... Text objects. ....... E.g.: iw, a(, i>
 *  - [MotionActionHandler]: ........... Motion commands. .... E.g.: k, w, <Up>
 *  - [ChangeEditorActionHandler]: ..... Change commands. .... E.g.: s, r, gU
 *  - [VisualOperatorActionHandler]: ... Visual commands.
 *  - [IdeActionHandler]: .............. Commands handled by existing IDE actions.
 *
 *  SpecialKeyHandlers are not presented here because these handlers are created to a limited set of commands and they
 *    are already implemented.
 */
abstract class EditorActionHandlerBase(private val myRunForEachCaret: Boolean) {
  val id: String = getActionId(this::class.java.name)

  abstract val type: Command.Type

  open val argumentType: Argument.Type? = null

  /**
   * Returns various binary flags for the command.
   *
   * These legacy flags will be refactored in future releases.
   *
   * @see com.flop.idea.fim.command.Command
   */
  open val flags: EnumSet<CommandFlags> = noneOfEnum()

  abstract fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean

  fun execute(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments) {
    val action = { caret: FimCaret -> doExecute(editor, caret, context, operatorArguments) }
    if (myRunForEachCaret) {
      editor.forEachCaret(action)
    } else {
      action(editor.primaryCaret())
    }
  }

  private fun doExecute(editor: FimEditor, caret: FimCaret, context: ExecutionContext, operatorArguments: OperatorArguments) {
    if (!injector.enabler.isEnabled()) return

    logger.debug("Execute command with handler: " + this.javaClass.name)

    val cmd = FimStateMachine.getInstance(editor).executingCommand ?: run {
      injector.messages.indicateError()
      return
    }

    if (!baseExecute(
        editor,
        caret,
        injector.executionContextManager.onCaret(caret, context),
        cmd,
        operatorArguments
      )
    ) injector.messages.indicateError()
  }

  open fun process(cmd: Command) {
    // No-op
  }

  companion object {
    private val logger = fimLogger<EditorActionHandlerBase>()

    fun parseKeysSet(keyStrings: List<String>) = keyStrings.map { injector.parser.parseKeys(it) }.toSet()

    @JvmStatic
    fun parseKeysSet(@NonNls vararg keyStrings: String): Set<List<KeyStroke>> = List(keyStrings.size) {
      injector.parser.parseKeys(keyStrings[it])
    }.toSet()

    @NonNls
    private const val FimActionPrefix = "Fim"

    @NonNls
    fun getActionId(classFullName: String): String {
      return classFullName
        .takeLastWhile { it != '.' }
        .let { if (it.startsWith(FimActionPrefix, true)) it else "$FimActionPrefix$it" }
    }
  }
}
