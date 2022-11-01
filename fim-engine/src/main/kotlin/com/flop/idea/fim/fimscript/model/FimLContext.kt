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

package com.flop.idea.fim.fimscript.model

import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration

sealed interface FimLContext {

  // todo rename
  fun getPreviousParentContext(): FimLContext

  fun isFirstParentContext(): Boolean {
    return this is Script || this is CommandLineFimLContext
  }

  // todo rename
  // todo documentation
  fun getFirstParentContext(): FimLContext {
    return if (isFirstParentContext()) {
      this
    } else {
      val previousContext = this.getPreviousParentContext()
      previousContext.getFirstParentContext()
    }
  }

  // todo better name
  fun getExecutableContext(executable: FimLContext): ExecutableContext {
    var currentNode: FimLContext = executable
    while (currentNode !is FunctionDeclaration && !currentNode.isFirstParentContext()) {
      currentNode = currentNode.getPreviousParentContext()
    }
    return when (currentNode) {
      is FunctionDeclaration -> ExecutableContext.FUNCTION
      is Script -> ExecutableContext.SCRIPT
      is CommandLineFimLContext -> ExecutableContext.COMMAND_LINE
      else -> throw RuntimeException("Reached unknown first parent context")
    }
  }

  fun getScript(): Script? {
    val firstParentContext = getFirstParentContext()
    return if (firstParentContext is Script) firstParentContext else null
  }
}

/*
 * FimL that was invoked from command line
 */
object CommandLineFimLContext : FimLContext {

  override fun getPreviousParentContext(): FimLContext {
    throw RuntimeException("Command line has no parent context")
  }
}

// todo rename
enum class ExecutableContext {
  COMMAND_LINE,
  SCRIPT,
  FUNCTION
}
