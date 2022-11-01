package com.flop.idea.fim.helper

import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import java.util.*

inline fun <reified T : Enum<T>> noneOfEnum(): EnumSet<T> = EnumSet.noneOf(T::class.java)

val TextRange.endOffsetInclusive
  get() = if (this.endOffset > 0 && this.endOffset > this.startOffset) this.endOffset - 1 else this.endOffset

val FimEditor.mode
  get() = this.fimStateMachine.mode

val FimEditor.inVisualMode
  get() = this.mode.inVisualMode

val FimEditor.inRepeatMode
  get() = this.fimStateMachine.isDotRepeatInProgress

var FimEditor.subMode
  get() = this.fimStateMachine.subMode
  set(value) {
    this.fimStateMachine.subMode = value
  }

val FimEditor.fimStateMachine
  get() = FimStateMachine.getInstance(this)

val FimStateMachine.Mode.inVisualMode
  get() = this == FimStateMachine.Mode.VISUAL || this == FimStateMachine.Mode.INSERT_VISUAL

val FimEditor.inBlockSubMode
  get() = this.subMode == FimStateMachine.SubMode.VISUAL_BLOCK

/**
 * Please use `isEndAllowed` based on `Editor` (another extension function)
 * It takes "single command" into account.
 */
val FimStateMachine.Mode.isEndAllowed: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT, FimStateMachine.Mode.VISUAL, FimStateMachine.Mode.SELECT -> true
    FimStateMachine.Mode.COMMAND, FimStateMachine.Mode.CMD_LINE, FimStateMachine.Mode.REPLACE, FimStateMachine.Mode.OP_PENDING -> usesVirtualSpace
    FimStateMachine.Mode.INSERT_NORMAL -> usesVirtualSpace
    FimStateMachine.Mode.INSERT_VISUAL -> usesVirtualSpace
    FimStateMachine.Mode.INSERT_SELECT -> usesVirtualSpace
  }

val usesVirtualSpace
  get() = (
    injector.optionService.getOptionValue(
      OptionScope.GLOBAL,
      OptionConstants.virtualeditName
    ) as FimString
    ).value == "onemore"

val FimEditor.isEndAllowed: Boolean
  get() = when (this.mode) {
    FimStateMachine.Mode.INSERT, FimStateMachine.Mode.VISUAL, FimStateMachine.Mode.SELECT, FimStateMachine.Mode.INSERT_VISUAL, FimStateMachine.Mode.INSERT_SELECT -> true
    FimStateMachine.Mode.COMMAND, FimStateMachine.Mode.CMD_LINE, FimStateMachine.Mode.REPLACE, FimStateMachine.Mode.OP_PENDING, FimStateMachine.Mode.INSERT_NORMAL -> {
      // One day we'll use a proper insert_normal mode
      if (this.mode.inSingleMode) true else usesVirtualSpace
    }
  }

val FimStateMachine.Mode.inSingleMode: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT_NORMAL, FimStateMachine.Mode.INSERT_SELECT, FimStateMachine.Mode.INSERT_VISUAL -> true
    else -> false
  }

val FimStateMachine.Mode.inInsertMode: Boolean
  get() = this == FimStateMachine.Mode.INSERT || this == FimStateMachine.Mode.REPLACE

val FimStateMachine.Mode.inSingleNormalMode: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT_NORMAL -> true
    else -> false
  }

val FimEditor.inNormalMode
  get() = this.mode.inNormalMode

val FimStateMachine.Mode.inNormalMode
  get() = this == FimStateMachine.Mode.COMMAND || this == FimStateMachine.Mode.INSERT_NORMAL

val FimStateMachine.Mode.isEndAllowedIgnoringOnemore: Boolean
  get() = when (this) {
    FimStateMachine.Mode.INSERT, FimStateMachine.Mode.VISUAL, FimStateMachine.Mode.SELECT -> true
    FimStateMachine.Mode.COMMAND, FimStateMachine.Mode.CMD_LINE, FimStateMachine.Mode.REPLACE, FimStateMachine.Mode.OP_PENDING -> false
    FimStateMachine.Mode.INSERT_NORMAL -> false
    FimStateMachine.Mode.INSERT_VISUAL -> true
    FimStateMachine.Mode.INSERT_SELECT -> true
  }

val FimEditor.inInsertMode
  get() = this.mode.inInsertMode

val FimEditor.inSelectMode
  get() = this.mode == FimStateMachine.Mode.SELECT || this.mode == FimStateMachine.Mode.INSERT_SELECT

val FimEditor.inSingleCommandMode
  get() = this.mode.inSingleMode

inline fun <reified T : Enum<T>> enumSetOf(vararg value: T): EnumSet<T> = when (value.size) {
  0 -> noneOfEnum()
  1 -> EnumSet.of(value[0])
  else -> EnumSet.of(value[0], *value.slice(1..value.lastIndex).toTypedArray())
}

fun FimStateMachine.pushSelectMode(subMode: FimStateMachine.SubMode, prevMode: FimStateMachine.Mode = this.mode) {
  if (prevMode.inSingleMode) {
    popModes()
    pushModes(FimStateMachine.Mode.INSERT_SELECT, subMode)
  } else {
    pushModes(FimStateMachine.Mode.SELECT, subMode)
  }
}

fun FimStateMachine.pushVisualMode(subMode: FimStateMachine.SubMode, prevMode: FimStateMachine.Mode = this.mode) {
  if (prevMode.inSingleMode) {
    popModes()
    pushModes(FimStateMachine.Mode.INSERT_VISUAL, subMode)
  } else {
    pushModes(FimStateMachine.Mode.VISUAL, subMode)
  }
}

fun <K, V> Map<K, V>.firstOrNull(): Map.Entry<K, V>? {
  return this.entries.firstOrNull()
}
