package com.flop.idea.fim.macro

import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import javax.swing.KeyStroke

abstract class FimMacroBase : FimMacro {
  override var lastRegister: Char = 0.toChar()
  private var macroDepth = 0

  // Macro depth. 0 - if macro is not executing. 1 - macro in progress. 2+ - nested macro
  override val isExecutingMacro: Boolean
    get() = macroDepth > 0

  /**
   * This method is used to play the macro of keystrokes stored in the specified registers.
   *
   * @param editor  The editor to play the macro in
   * @param context The data context
   * @param reg     The register to get the macro from
   * @param count   The number of times to execute the macro
   * @return true if able to play the macro, false if invalid or empty register
   */
  override fun playbackRegister(editor: FimEditor, context: ExecutionContext, reg: Char, count: Int): Boolean {
    logger.debug { "play back register $reg $count times" }
    val register = injector.registerGroup.getPlaybackRegister(reg) ?: return false
    ++macroDepth
    try {
      val keys: List<KeyStroke> = if (register.rawText == null) {
        register.keys
      } else {
        injector.parser.parseKeys(register.rawText)
      }
      KeyHandler.getInstance().keyStack.addKeys(keys)
      playbackKeys(editor, context, 0, count)
    } finally {
      --macroDepth
    }

    lastRegister = reg
    return true
  }

  /**
   * This plays back the last register that was executed, if any.
   *
   * @param editor  The editr to play the macro in
   * @param context The data context
   * @param count   The number of times to execute the macro
   * @return true if able to play the macro, false in no previous playback
   */
  override fun playbackLastRegister(editor: FimEditor, context: ExecutionContext, count: Int): Boolean {
    return lastRegister.code != 0 && playbackRegister(editor, context, lastRegister, count)
  }

  companion object {
    val logger = fimLogger<FimMacroBase>()
  }
}
