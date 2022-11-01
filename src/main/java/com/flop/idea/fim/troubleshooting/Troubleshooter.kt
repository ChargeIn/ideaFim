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

package com.flop.idea.fim.troubleshooting

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.containers.MultiMap
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.key.MappingOwner
import com.flop.idea.fim.key.ToKeysMappingInfo

// [WIP] https://youtrack.jetbrains.com/issue/VIM-2658/Troubleshooter
@Service
class Troubleshooter {
  private val problems: MultiMap<String, Problem> = MultiMap()

  fun removeByType(type: String) {
    problems.remove(type)
  }

  fun addProblem(type: String, problem: Problem) {
    problems.putValue(type, problem)
  }

  fun findIncorrectMappings(): List<Problem> {
    val problems = ArrayList<Problem>()
    MappingMode.values().forEach { mode ->
      injector.keyGroup.getKeyMapping(mode).getByOwner(MappingOwner.IdeaFim.InitScript).forEach { (_, to) ->
        if (to is ToKeysMappingInfo) {
          if (":action" in to.toKeys.joinToString { it.keyChar.toString() }) {
            problems += Problem("Mappings contain `:action` call")
          }
        }
      }
    }
    return problems
  }

  companion object {
    val instance = service<Troubleshooter>()
  }
}

class Problem(description: String)
