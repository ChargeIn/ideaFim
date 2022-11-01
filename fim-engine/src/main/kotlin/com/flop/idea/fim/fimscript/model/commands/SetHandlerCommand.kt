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
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.key.ShortcutOwner
import com.flop.idea.fim.key.ShortcutOwnerInfo
import com.flop.idea.fim.fimscript.model.ExecutionResult

data class SetHandlerCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    return if (doCommand()) ExecutionResult.Success else ExecutionResult.Error
  }

  private fun doCommand(): Boolean {
    if (argument.isBlank()) return false

    val args = argument.trim().split(" ")
    if (args.isEmpty()) return false

    val key = try {
      val shortcut = args[0]
      if (shortcut.startsWith('<')) injector.parser.parseKeys(shortcut).first() else null
    } catch (e: IllegalArgumentException) {
      null
    }

    val owner = ShortcutOwnerInfo.allPerModeFim
    val skipShortcut = if (key == null) 0 else 1
    val resultingOwner = args.drop(skipShortcut).fold(owner) { currentOwner: ShortcutOwnerInfo.PerMode?, newData ->
      updateOwner(currentOwner, newData)
    } ?: return false

    if (key != null) {
      injector.keyGroup.savedShortcutConflicts[key] = resultingOwner
    } else {
      injector.keyGroup.shortcutConflicts.keys.forEach { conflictKey ->
        injector.keyGroup.savedShortcutConflicts[conflictKey] = resultingOwner
      }
    }
    return true
  }

  companion object {
    // todo you should make me internal
    fun updateOwner(owner: ShortcutOwnerInfo.PerMode?, newData: String): ShortcutOwnerInfo.PerMode? {
      if (owner == null) return null
      val split = newData.split(":", limit = 2)
      if (split.size != 2) return null

      val left = split[0]
      val right = ShortcutOwner.fromStringOrNull(split[1]) ?: return null

      var currentOwner: ShortcutOwnerInfo.PerMode = owner
      val modeSplit = left.split("-")
      modeSplit.forEach {
        currentOwner = when (it) {
          "n" -> currentOwner.copy(normal = right)
          "i" -> currentOwner.copy(insert = right)
          "v" -> currentOwner.copy(visual = right, select = right)
          "x" -> currentOwner.copy(visual = right)
          "s" -> currentOwner.copy(select = right)
          "a" -> currentOwner.copy(normal = right, insert = right, visual = right, select = right)
          else -> return null
        }
      }
      return currentOwner
    }
  }
}
