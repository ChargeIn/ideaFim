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
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.MissingArgumentException
import com.flop.idea.fim.ex.MissingRangeException
import com.flop.idea.fim.ex.NoArgumentAllowedException
import com.flop.idea.fim.ex.NoRangeAllowedException
import com.flop.idea.fim.ex.ranges.LineRange
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.Msg
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.mode
import com.flop.idea.fim.helper.noneOfEnum
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.fimscript.model.Executable
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext
import java.util.*

sealed class Command(var commandRanges: Ranges, val commandArgument: String) : Executable {
  override lateinit var fimContext: FimLContext

  abstract val argFlags: CommandHandlerFlags
  protected open val optFlags: EnumSet<CommandFlags> = noneOfEnum()
  private val logger = fimLogger<Command>()

  abstract class ForEachCaret(ranges: Ranges, argument: String = "") : Command(ranges, argument) {
    abstract fun processCommand(
      editor: FimEditor,
      caret: FimCaret,
      context: ExecutionContext,
      operatorArguments: OperatorArguments
    ): ExecutionResult
  }

  abstract class SingleExecution(ranges: Ranges, argument: String = "") : Command(ranges, argument) {
    abstract fun processCommand(
      editor: FimEditor,
      context: ExecutionContext,
      operatorArguments: OperatorArguments,
    ): ExecutionResult
  }

  @Throws(ExException::class)
  override fun execute(editor: FimEditor, context: ExecutionContext): ExecutionResult {
    checkRanges()
    checkArgument()
    if (editor.inVisualMode && Flag.SAVE_VISUAL !in argFlags.flags) {
      editor.exitVisualModeNative()
    }
    if (argFlags.access == Access.WRITABLE && !editor.isDocumentWritable()) {
      logger.info("Trying to modify readonly document")
      return ExecutionResult.Error
    }

    val operatorArguments = OperatorArguments(
      editor.fimStateMachine.isOperatorPending,
      0,
      editor.mode,
      editor.subMode,
    )

    val runCommand = { runCommand(editor, context, operatorArguments) }
    return when (argFlags.access) {
      Access.WRITABLE -> injector.application.runWriteAction(runCommand)
      Access.READ_ONLY -> injector.application.runReadAction(runCommand)
      Access.SELF_SYNCHRONIZED -> runCommand.invoke()
    }
  }

  private fun runCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    var result: ExecutionResult = ExecutionResult.Success
    when (this) {
      is ForEachCaret -> {
        editor.forEachNativeCaret(
          { caret ->
            if (result is ExecutionResult.Success) {
              result = processCommand(editor, caret, context, operatorArguments)
            }
          },
          true
        )
      }
      is SingleExecution -> result = processCommand(editor, context, operatorArguments)
    }
    return result
  }

  private fun checkRanges() {
    if (RangeFlag.RANGE_FORBIDDEN == argFlags.rangeFlag && commandRanges.size() != 0) {
      injector.messages.showStatusBarMessage(injector.messages.message(Msg.e_norange))
      throw NoRangeAllowedException()
    }

    if (RangeFlag.RANGE_REQUIRED == argFlags.rangeFlag && commandRanges.size() == 0) {
      injector.messages.showStatusBarMessage(injector.messages.message(Msg.e_rangereq))
      throw MissingRangeException()
    }

    if (RangeFlag.RANGE_IS_COUNT == argFlags.rangeFlag) {
      commandRanges.setDefaultLine(1)
    }
  }

  private fun checkArgument() {
    if (ArgumentFlag.ARGUMENT_FORBIDDEN == argFlags.argumentFlag && commandArgument.isNotBlank()) {
      injector.messages.showStatusBarMessage(injector.messages.message(Msg.e_argforb))
      throw NoArgumentAllowedException()
    }

    if (ArgumentFlag.ARGUMENT_REQUIRED == argFlags.argumentFlag && commandArgument.isBlank()) {
      injector.messages.showStatusBarMessage(injector.messages.message(Msg.e_argreq))
      throw MissingArgumentException()
    }
  }

  enum class RangeFlag {
    /**
     * Indicates that a range must be specified with this command
     */
    RANGE_REQUIRED,

    /**
     * Indicates that a range is optional for this command
     */
    RANGE_OPTIONAL,

    /**
     * Indicates that a range can't be specified for this command
     */
    RANGE_FORBIDDEN,

    /**
     * Indicates that the command takes a count, not a range - effects default
     * Works like RANGE_OPTIONAL
     */
    RANGE_IS_COUNT
  }

  enum class ArgumentFlag {
    /**
     * Indicates that an argument must be specified with this command
     */
    ARGUMENT_REQUIRED,

    /**
     * Indicates that an argument is optional for this command
     */
    ARGUMENT_OPTIONAL,

    /**
     * Indicates that an argument can't be specified for this command
     */
    ARGUMENT_FORBIDDEN
  }

  enum class Access {
    /**
     * Indicates that this is a command that modifies the editor
     */
    WRITABLE,

    /**
     * Indicates that this command does not modify the editor
     */
    READ_ONLY,

    /**
     * Indicates that this command handles writability by itself
     */
    SELF_SYNCHRONIZED
  }

  enum class Flag {
    /**
     * This command should not exit visual mode.
     *
     * Fim exits visual mode before command execution, but in this case :action will work incorrect.
     *   With this flag visual mode will not be exited while command execution.
     */
    SAVE_VISUAL
  }

  data class CommandHandlerFlags(
    val rangeFlag: RangeFlag,
    val argumentFlag: ArgumentFlag,
    val access: Access,
    val flags: Set<Flag>,
  )

  fun flags(rangeFlag: RangeFlag, argumentFlag: ArgumentFlag, access: Access, vararg flags: Flag) =
    CommandHandlerFlags(rangeFlag, argumentFlag, access, flags.toSet())

  fun getLine(editor: FimEditor): Int = commandRanges.getLine(editor)

  fun getLine(editor: FimEditor, caret: FimCaret): Int = commandRanges.getLine(editor, caret)

  fun getCount(editor: FimEditor, defaultCount: Int, checkCount: Boolean): Int {
    val count = if (checkCount) countArgument else -1

    val res = commandRanges.getCount(editor, count)
    return if (res == -1) defaultCount else res
  }

  fun getCount(editor: FimEditor, caret: FimCaret, defaultCount: Int, checkCount: Boolean): Int {
    val count = commandRanges.getCount(editor, caret, if (checkCount) countArgument else -1)
    return if (count == -1) defaultCount else count
  }

  fun getLineRange(editor: FimEditor): LineRange = commandRanges.getLineRange(editor, -1)

  fun getLineRange(editor: FimEditor, caret: FimCaret, checkCount: Boolean = false): LineRange {
    return commandRanges.getLineRange(editor, caret, if (checkCount) countArgument else -1)
  }

  fun getTextRange(editor: FimEditor, checkCount: Boolean): TextRange {
    val count = if (checkCount) countArgument else -1
    return commandRanges.getTextRange(editor, count)
  }

  fun getTextRange(editor: FimEditor, caret: FimCaret, checkCount: Boolean): TextRange {
    return commandRanges.getTextRange(editor, caret, if (checkCount) countArgument else -1)
  }

  private val countArgument: Int
    get() = commandArgument.toIntOrNull() ?: -1
}
