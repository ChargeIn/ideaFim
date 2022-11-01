package com.flop.idea.fim.command

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.handler.EditorActionHandlerBase
import com.flop.idea.fim.handler.MotionActionHandler
import com.flop.idea.fim.handler.TextObjectActionHandler
import java.util.*

/**
 * This represents a single Fim command to be executed (operator, motion, text object, etc.). It may optionally include
 * an argument if appropriate for the command. The command has a count and a type.
 */
data class Command(
  var rawCount: Int,
  var action: EditorActionHandlerBase,
  val type: Type,
  var flags: EnumSet<CommandFlags>,
) {

  constructor(rawCount: Int, register: Char) : this(
    rawCount,
    NonExecutableActionHandler,
    Type.SELECT_REGISTER,
    EnumSet.of(CommandFlags.FLAG_EXPECT_MORE)
  ) {
    this.register = register
  }

  init {
    action.process(this)
  }

  var count: Int
    get() = rawCount.coerceAtLeast(1)
    set(value) {
      rawCount = value
    }

  var argument: Argument? = null
  var register: Char? = null

  fun isLinewiseMotion(): Boolean {
    return when (action) {
      is TextObjectActionHandler -> (action as TextObjectActionHandler).visualType == TextObjectVisualType.LINE_WISE
      is MotionActionHandler -> (action as MotionActionHandler).motionType == MotionType.LINE_WISE
      else -> error("Command is not a motion: $action")
    }
  }

  enum class Type {
    /**
     * Represents commands that actually move the cursor and can be arguments to operators.
     */
    MOTION,

    /**
     * Represents commands that insert new text into the editor.
     */
    INSERT,

    /**
     * Represents commands that remove text from the editor.
     */
    DELETE,

    /**
     * Represents commands that change text in the editor.
     */
    CHANGE,

    /**
     * Represents commands that copy text in the editor.
     */
    COPY,
    PASTE,

    /**
     * Represents commands that select the register.
     */
    SELECT_REGISTER,
    OTHER_READONLY,
    OTHER_WRITABLE,

    /**
     * Represent commands that don't require an outer read or write action for synchronization.
     */
    OTHER_SELF_SYNCHRONIZED;

    val isRead: Boolean
      get() = when (this) {
        MOTION, COPY, OTHER_READONLY -> true
        else -> false
      }

    val isWrite: Boolean
      get() = when (this) {
        INSERT, DELETE, CHANGE, PASTE, OTHER_WRITABLE -> true
        else -> false
      }
  }
}

private object NonExecutableActionHandler : EditorActionHandlerBase(false) {
  override val type: Command.Type
    get() = error("This action should not be executed")

  override fun baseExecute(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments
  ): Boolean {
    error("This action should not be executed")
  }
}
