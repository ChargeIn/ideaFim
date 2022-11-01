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

package com.flop.idea.fim.listener

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.options.OptionScope
import java.io.Closeable

/**
 * Base class for listener suppressors.
 * Children of this class have an ability to suppress editor listeners
 *
 * E.g.
 * ```
 *  CaretFimListenerSuppressor.lock()
 *  caret.moveToOffset(10) // fim's caret listener will not be executed
 *  CaretFimListenerSuppressor.unlock()
 * ````
 *
 *  Locks can be nested:
 * ```
 * CaretFimListenerSuppressor.lock()
 * moveCaret(caret) // fim's caret listener will not be executed
 * CaretFimListenerSuppressor.unlock()
 *
 * fun moveCaret(caret: Caret) {
 *     CaretFimListenerSuppressor.lock()
 *     caret.moveToOffset(10)
 *     CaretFimListenerSuppressor.unlock()
 * }
 * ```
 *
 * [Locked] implements [Closeable], so you can use try-with-resources block
 *
 * java
 * ```
 * try (FimListenerSuppressor.Locked ignored = SelectionFimListenerSuppressor.INSTANCE.lock()) {
 *     ....
 * }
 * ```
 *
 * Kotlin
 * ```
 * SelectionFimListenerSuppressor.lock().use { ... }
 * ```
 */
sealed class FimListenerSuppressor {
  private var caretListenerSuppressor = 0

  fun lock(): Locked {
    LOG.trace("Suppressor lock")
    LOG.trace(injector.application.currentStackTrace())
    caretListenerSuppressor++
    return Locked()
  }

  // Please try not to use lock/unlock without scoping
  // Prefer try-with-resources
  fun unlock() {
    LOG.trace("Suppressor unlock")
    LOG.trace(injector.application.currentStackTrace())
    caretListenerSuppressor--
  }

  fun reset() {
    if (caretListenerSuppressor != 0 && injector.optionService.isSet(OptionScope.GLOBAL, "ideastrictmode")) {
      error("Listener is not zero")
    }
    caretListenerSuppressor = 0
  }

  val isNotLocked: Boolean
    get() = caretListenerSuppressor == 0

  inner class Locked : Closeable {
    override fun close() = unlock()
  }

  companion object {
    private val LOG = fimLogger<FimListenerSuppressor>()
  }
}

object SelectionFimListenerSuppressor : FimListenerSuppressor()
