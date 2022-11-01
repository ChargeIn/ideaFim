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
package com.flop.idea.fim.command

import com.flop.idea.fim.api.FimActionsInitiator
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.common.DigraphResult
import com.flop.idea.fim.common.DigraphSequence
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.helper.noneOfEnum
import com.flop.idea.fim.key.CommandPartNode
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.annotations.Contract
import java.util.*
import javax.swing.KeyStroke

/**
 * Used to maintain state before and while entering a Fim command (operator, motion, text object, etc.)
 *
 * // TODO: 21.02.2022 This constructor should be empty
 */
class FimStateMachine(private val editor: FimEditor?) {
  val commandBuilder = CommandBuilder(getKeyRootNode(MappingMode.NORMAL))
  private val modeStates = Stack<ModeState>()
  val mappingState = MappingState()
  val digraphSequence = DigraphSequence()
  var isRecording = false
    set(value) {
      field = value
      doShowMode()
    }
  var isDotRepeatInProgress = false
  var isRegisterPending = false
  var isReplaceCharacter = false
    set(value) {
      field = value
      onModeChanged()
    }

  /**
   * The currently executing command
   *
   * This is a complete command, e.g. operator + motion. Some actions/helpers require additional context from flags in
   * the command/argument. Ideally, we would pass the command through KeyHandler#executeFimAction and
   * EditorActionHandlerBase#execute, but we also need to know the command type in MarkGroup#updateMarkFromDelete,
   * which is called via a document change event.
   *
   * This field is reset after the command has been executed.
   */
  var executingCommand: Command? = null
    private set

  val isOperatorPending: Boolean
    get() = mappingState.mappingMode == MappingMode.OP_PENDING && !commandBuilder.isEmpty

  init {
    pushModes(defaultModeState.mode, defaultModeState.subMode)
  }

  fun isDuplicateOperatorKeyStroke(key: KeyStroke?): Boolean {
    return isOperatorPending && commandBuilder.isDuplicateOperatorKeyStroke(key!!)
  }

  fun setExecutingCommand(cmd: Command) {
    executingCommand = cmd
  }

  val executingCommandFlags: EnumSet<CommandFlags>
    get() = executingCommand?.flags ?: noneOfEnum()

  fun pushModes(mode: Mode, submode: SubMode) {
    val newModeState = ModeState(mode, submode)

    logger.debug("Push new mode state: ${newModeState.toSimpleString()}")
    logger.debug { "Stack of mode states before push: ${toSimpleString()}" }

    val previousMode = currentModeState()
    modeStates.push(newModeState)
    setMappingMode()

    if (previousMode != newModeState) {
      onModeChanged()
    }
  }

  fun popModes() {
    val popped = modeStates.pop()
    setMappingMode()
    if (popped != currentModeState()) {
      onModeChanged()
    }

    logger.debug("Popped mode state: ${popped.toSimpleString()}")
    logger.debug { "Stack of mode states after pop: ${toSimpleString()}" }
  }

  fun resetOpPending() {
    if (mode == Mode.OP_PENDING) {
      popModes()
    }
  }

  fun resetReplaceCharacter() {
    if (isReplaceCharacter) {
      isReplaceCharacter = false
    }
  }

  fun resetRegisterPending() {
    if (isRegisterPending) {
      isRegisterPending = false
    }
  }

  private fun resetModes() {
    modeStates.clear()
    pushModes(defaultModeState.mode, defaultModeState.subMode)
    onModeChanged()
    setMappingMode()
  }

  private fun onModeChanged() {
    if (editor != null) {
      editor.updateCaretsVisualAttributes()
      editor.updateCaretsVisualPosition()
    } else {
      injector.application.localEditors().forEach { editor ->
        editor.updateCaretsVisualAttributes()
        editor.updateCaretsVisualPosition()
      }
    }
    doShowMode()
  }

  private fun setMappingMode() {
    mappingState.mappingMode = modeToMappingMode(mode)
  }

  @Contract(pure = true)
  private fun modeToMappingMode(mode: Mode): MappingMode {
    return when (mode) {
      Mode.COMMAND -> MappingMode.NORMAL
      Mode.INSERT, Mode.REPLACE -> MappingMode.INSERT
      Mode.VISUAL -> MappingMode.VISUAL
      Mode.SELECT -> MappingMode.SELECT
      Mode.CMD_LINE -> MappingMode.CMD_LINE
      Mode.OP_PENDING -> MappingMode.OP_PENDING
      Mode.INSERT_NORMAL -> MappingMode.NORMAL
      Mode.INSERT_VISUAL -> MappingMode.VISUAL
      Mode.INSERT_SELECT -> MappingMode.SELECT
    }
  }

  val mode: Mode
    get() = currentModeState().mode

  var subMode: SubMode
    get() = currentModeState().subMode
    set(submode) {
      val modeState = currentModeState()
      popModes()
      pushModes(modeState.mode, submode)
    }

  fun startDigraphSequence() {
    digraphSequence.startDigraphSequence()
  }

  fun startLiteralSequence() {
    digraphSequence.startLiteralSequence()
  }

  fun processDigraphKey(key: KeyStroke, editor: FimEditor): DigraphResult {
    return digraphSequence.processKey(key, editor)
  }

  fun resetDigraph() {
    digraphSequence.reset()
  }

  /**
   * Toggles the insert/overwrite state. If currently insert, goto replace mode. If currently replace, goto insert
   * mode.
   */
  fun toggleInsertOverwrite() {
    val oldMode = mode
    var newMode = oldMode
    if (oldMode == Mode.INSERT) {
      newMode = Mode.REPLACE
    } else if (oldMode == Mode.REPLACE) {
      newMode = Mode.INSERT
    }
    if (oldMode != newMode) {
      val modeState = currentModeState()
      popModes()
      pushModes(newMode, modeState.subMode)
    }
  }

  /**
   * Resets the command, mode, visual mode, and mapping mode to initial values.
   */
  fun reset() {
    executingCommand = null
    resetModes()
    commandBuilder.resetInProgressCommandPart(getKeyRootNode(mappingState.mappingMode))
    digraphSequence.reset()
  }

  fun toSimpleString(): String = modeStates.joinToString { it.toSimpleString() }

  /**
   * It's a bit more complicated
   *
   *  Neofim
   * :h mode()
   *
   * - mode(expr)          Return a string that indicates the current mode.
   *
   *   If "expr" is supplied and it evaluates to a non-zero Number or
   *   a non-empty String (|non-zero-arg|), then the full mode is
   *   returned, otherwise only the first letter is returned.
   *
   *   n          Normal
   *   no         Operator-pending
   *   nov        Operator-pending (forced characterwise |o_v|)
   *   noV        Operator-pending (forced linewise |o_V|)
   *   noCTRL-V   Operator-pending (forced blockwise |o_CTRL-V|)
   *   niI        Normal using |i_CTRL-O| in |Insert-mode|
   *   niR        Normal using |i_CTRL-O| in |Replace-mode|
   *   niV        Normal using |i_CTRL-O| in |Virtual-Replace-mode|
   *   v          Visual by character
   *   V          Visual by line
   *   CTRL-V     Visual blockwise
   *   s          Select by character
   *   S          Select by line
   *   CTRL-S     Select blockwise
   *   i          Insert
   *   ic         Insert mode completion |compl-generic|
   *   ix         Insert mode |i_CTRL-X| completion
   *   R          Replace |R|
   *   Rc         Replace mode completion |compl-generic|
   *   Rv         Virtual Replace |gR|
   *   Rx         Replace mode |i_CTRL-X| completion
   *   c          Command-line editing
   *   cv         Fim Ex mode |gQ|
   *   ce         Normal Ex mode |Q|
   *   r          Hit-enter prompt
   *   rm         The -- more -- prompt
   *   r?         |:confirm| query of some sort
   *   !          Shell or external command is executing
   *   t          Terminal mode: keys go to the job
   *   This is useful in the 'statusline' option or when used
   *   with |remote_expr()| In most other places it always returns
   *   "c" or "n".
   *   Note that in the future more modes and more specific modes may
   *   be added. It's better not to compare the whole string but only
   *   the leading character(s).
   */
  fun toFimNotation(): String {
    return when (mode) {
      Mode.COMMAND -> "n"
      Mode.VISUAL -> when (subMode) {
        SubMode.VISUAL_CHARACTER -> "v"
        SubMode.VISUAL_LINE -> "V"
        SubMode.VISUAL_BLOCK -> "\u0016"
        else -> error("Unexpected state")
      }
      Mode.INSERT -> "i"
      Mode.SELECT -> when (subMode) {
        SubMode.VISUAL_CHARACTER -> "s"
        SubMode.VISUAL_LINE -> "S"
        SubMode.VISUAL_BLOCK -> "\u0013"
        else -> error("Unexpected state")
      }

      Mode.REPLACE -> "R"
      Mode.INSERT_VISUAL -> when (subMode) {
        SubMode.VISUAL_CHARACTER -> "v"
        SubMode.VISUAL_LINE -> "V"
        SubMode.VISUAL_BLOCK -> "\u0016"
        else -> error("Unexpected state")
      }
      else -> error("Unexpected state")
    }
  }

  private fun currentModeState(): ModeState {
    return if (modeStates.size > 0) modeStates.peek() else defaultModeState
  }

  private fun doShowMode() {
    val msg = StringBuilder()
    if (injector.optionService.isSet(OptionScope.GLOBAL, OptionConstants.showmodeName)) {
      msg.append(getStatusString())
    }
    if (isRecording) {
      if (msg.isNotEmpty()) {
        msg.append(" - ")
      }
      msg.append(injector.messages.message("show.mode.recording"))
    }
    injector.messages.showMode(msg.toString())
  }

  fun getStatusString(): String {
    val pos = modeStates.size - 1
    val modeState = if (pos >= 0) {
      modeStates[pos]
    } else {
      defaultModeState
    }
    return buildString {
      when (modeState.mode) {
        Mode.INSERT_NORMAL -> append("-- (insert) --")
        Mode.INSERT -> append("INSERT")
        Mode.REPLACE -> append("REPLACE")
        Mode.VISUAL -> {
          append("-- VISUAL")
          when (modeState.subMode) {
            SubMode.VISUAL_LINE -> append(" LINE")
            SubMode.VISUAL_BLOCK -> append(" BLOCK")
            else -> Unit
          }
          append(" --")
        }
        Mode.SELECT -> {
          append("-- SELECT")
          when (modeState.subMode) {
            SubMode.VISUAL_LINE -> append(" LINE")
            SubMode.VISUAL_BLOCK -> append(" BLOCK")
            else -> Unit
          }
          append(" --")
        }
        Mode.INSERT_VISUAL -> {
          append("-- (insert) VISUAL")
          when (modeState.subMode) {
            SubMode.VISUAL_LINE -> append(" LINE")
            SubMode.VISUAL_BLOCK -> append(" BLOCK")
            else -> Unit
          }
          append(" --")
        }
        Mode.INSERT_SELECT -> {
          append("-- (insert) SELECT")
          when (modeState.subMode) {
            SubMode.VISUAL_LINE -> append(" LINE")
            SubMode.VISUAL_BLOCK -> append(" BLOCK")
            else -> Unit
          }
          append(" --")
        }
        else -> Unit
      }
    }
  }

  enum class Mode {
    // Basic modes
    COMMAND, VISUAL, SELECT, INSERT, CMD_LINE, /*EX*/

    // Additional modes
    OP_PENDING, REPLACE /*, VISUAL_REPLACE*/, INSERT_NORMAL, INSERT_VISUAL, INSERT_SELECT
  }

  enum class SubMode {
    NONE, VISUAL_CHARACTER, VISUAL_LINE, VISUAL_BLOCK
  }

  private data class ModeState(val mode: Mode, val subMode: SubMode) {
    fun toSimpleString(): String = "$mode:$subMode"
  }

  companion object {
    private val logger = fimLogger<FimStateMachine>()
    private val defaultModeState = ModeState(Mode.COMMAND, SubMode.NONE)
    private val globalState = FimStateMachine(null)

    /**
     * COMPATIBILITY-LAYER: Method switched to Any (was FimEditor)
     * Please see: https://jb.gg/zo8n0r
     */
    @JvmStatic
    fun getInstance(editor: Any?): FimStateMachine {
      return if (editor == null || injector.optionService.isSet(OptionScope.GLOBAL, OptionConstants.ideaglobalmodeName)) {
        globalState
      } else {
        injector.commandStateFor(editor)
      }
    }

    private fun getKeyRootNode(mappingMode: MappingMode): CommandPartNode<FimActionsInitiator> {
      return injector.keyGroup.getKeyRoot(mappingMode)
    }
  }
}
