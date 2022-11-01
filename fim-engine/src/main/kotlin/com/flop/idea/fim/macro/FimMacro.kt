package com.flop.idea.fim.macro

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor

interface FimMacro {
  val isExecutingMacro: Boolean
  var lastRegister: Char

  /**
   * Keys are taken from KeyHandler.mappingStack
   */
  fun playbackKeys(
    editor: FimEditor,
    context: ExecutionContext,
    cnt: Int,
    total: Int,
  )

  /**
   * This method is used to play the macro of keystrokes stored in the specified registers.
   *
   * @param editor  The editor to play the macro in
   * @param context The data context
   * @param reg     The register to get the macro from
   * @param count   The number of times to execute the macro
   * @return true if able to play the macro, false if invalid or empty register
   */
  fun playbackRegister(editor: FimEditor, context: ExecutionContext, reg: Char, count: Int): Boolean

  /**
   * This plays back the last register that was executed, if any.
   *
   * @param editor  The editr to play the macro in
   * @param context The data context
   * @param count   The number of times to execute the macro
   * @return true if able to play the macro, false in no previous playback
   */
  fun playbackLastRegister(
    editor: FimEditor,
    context: ExecutionContext,
    count: Int
  ): Boolean
}
