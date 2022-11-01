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
import org.jetbrains.plugins.ideafim.FimTestCase

class InsertExitModeActionTest : FimTestCase() {
  fun `test exit visual mode`() {
    doTest("i<Esc>", "12${c}3", "1${c}23", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test exit visual mode on line start`() {
    doTest("i<Esc>", "${c}123", "${c}123", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }
}
