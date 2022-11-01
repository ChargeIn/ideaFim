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

package org.jetbrains.plugins.ideafim.action.change.insert

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.FimTestCase

class InsertDeleteInsertedTextActionTest : FimTestCase() {
  // VIM-1655
  fun `test deleted text is not yanked`() {
    doTest(
      listOf("yiw", "ea", "Hello", "<C-U>", "<ESC>p"),
      """
            A Discovery

            I found ${c}it in a legendary land
      """.trimIndent(),
      """
            A Discovery

            I found iti${c}t in a legendary land
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-1655
  @FimBehaviorDiffers(description = "Inserted text is not deleted after <C-U>")
  fun `test deleted text is not yanked after replace`() {
    doTest(
      listOf("yiw", "eR", "Hello", "<C-U>", "<ESC>p"),
      """
            A Discovery

            I found ${c}it in a legendary land
      """.trimIndent(),
      """
            A Discovery

            I found ii${c}ta legendary land
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
