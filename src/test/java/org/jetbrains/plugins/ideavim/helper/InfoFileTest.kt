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

package org.jetbrains.plugins.ideafim.helper

import com.flop.idea.fim.RegisterActions.FIM_ACTIONS_EP
import com.flop.idea.fim.handler.EditorActionHandlerBase
import org.jetbrains.plugins.ideafim.FimTestCase
import java.io.File

/**
 * @author Alex Plate
 */
class InfoFileTest : FimTestCase() {
  fun `test info`() {
    val text = File("src/main/java/com/flop/idea/fim/package-info.java").readText()

    val notPresentedActions = mutableListOf<String>()
    forEachAction { action ->
      val actionName = action.javaClass.name
      if ("{@link $actionName}" !in text) {
        notPresentedActions += actionName
      }
    }
    assertTrue(
      notPresentedActions.joinToString(prefix = "Not presented actions in info file: \n", separator = "\n"),
      notPresentedActions.isEmpty()
    )
  }

  private inline fun forEachAction(supply: (action: EditorActionHandlerBase) -> Unit) {
    FIM_ACTIONS_EP.extensions.map { it.instance }.forEach { supply(it) }
  }
}
