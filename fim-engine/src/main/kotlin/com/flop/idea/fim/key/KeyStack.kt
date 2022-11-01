package com.flop.idea.fim.key

import com.flop.idea.fim.api.injector
import javax.swing.KeyStroke

/**
 * This thing is used for keeping keys from ToKeys mappings and macros.
 * Previously, the mapping was directly sent to the [KeyHandler]. However, in this case it was impossible to
 *   pass the key to modal entry (for getChar function).
 * Original fim uses a typeahead buffer for that, but it's not implemented for IdeaFim and this mappingStack
 *   solves an issus of passing a keystroke to modal entry.
 *   However, some more advanced solution may be necessary in the future.
 */
class KeyStack {
  private val stack = ArrayDeque<Frame>()

  fun hasStroke(): Boolean {
    return stack.isNotEmpty() && stack.first().hasStroke()
  }

  fun feedSomeStroke(): KeyStroke? {
    stack.forEach {
      if (it.hasStroke()) {
        return it.feed()
      }
    }
    return null
  }

  fun feedStroke(): KeyStroke {
    val frame = stack.first()
    val key = frame.feed()
    return key
  }

  fun addKeys(keyStrokes: List<KeyStroke>) {
    stack.addFirst(Frame(keyStrokes))
  }

  fun removeFirst() {
    if (stack.isNotEmpty()) {
      stack.removeFirst()
    }
  }

  fun resetFirst() {
    if (stack.isNotEmpty()) {
      stack.first().resetPointer()
    }
  }
}

private class Frame(
  val keys: List<KeyStroke>,
  var pointer: Int = 0,
) {
  fun hasStroke(): Boolean {
    return pointer < keys.size
  }

  fun feed(): KeyStroke {
    val key = keys[pointer]
    pointer += 1
    return key
  }

  fun resetPointer() {
    pointer = 0
  }

  override fun toString(): String {
    return "" + pointer + " | " + injector.parser.toKeyNotation(keys)
  }
}
