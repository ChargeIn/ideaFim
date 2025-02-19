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

package org.jetbrains.plugins.ideafim.action.change.change.number

import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.FimTestCase

class ChangeNumberDecActionTest : FimTestCase() {
  fun `test decrement hex to negative value`() {
    doTest("<C-X>", "0x0000", "0xffffffffffffffff", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test decrement hex to negative value by 10`() {
    doTest("10<C-X>", "0x0005", "0xfffffffffffffffb", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test decrement oct to negative value`() {
    doTest(
      ":set nrformats+=octal<CR><C-X>",
      "00000",
      "01777777777777777777777",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test decrement incorrect octal`() {
    doTest(":set nrformats+=octal<CR><C-X>", "008", "7", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test decrement oct to negative value by 10`() {
    doTest(
      ":set nrformats+=octal<CR>10<C-X>",
      "00005",
      "01777777777777777777773",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }
}
