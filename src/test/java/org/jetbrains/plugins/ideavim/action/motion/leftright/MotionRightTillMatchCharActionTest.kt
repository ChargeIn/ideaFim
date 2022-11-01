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

package org.jetbrains.plugins.ideafim.action.motion.leftright

import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.FimTestCase

class MotionRightTillMatchCharActionTest : FimTestCase() {
  fun `test move and repeat`() {
    doTest(
      "tx;",
      "${c}hello x hello x hello",
      "hello x hello$c x hello",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test move and repeat twice`() {
    doTest(
      "tx;;",
      "${c}hello x hello x hello x hello",
      "hello x hello x hello$c x hello",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test move and repeat two`() {
    doTest(
      "tx2;",
      "${c}hello x hello x hello x hello",
      "hello x hello$c x hello x hello",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test move and repeat three`() {
    doTest(
      "tx3;",
      "${c}hello x hello x hello x hello x hello",
      "hello x hello x hello$c x hello x hello",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test move and repeat backwards`() {
    doTest(
      "tx,",
      "hello x hello x ${c}hello x hello x hello",
      "hello x hello x$c hello x hello x hello",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }
}
