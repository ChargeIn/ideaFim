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

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */
class ChangeVisualNumberDecActionTest : FimTestCase() {
  fun `test dec visual full number`() {
    doTest(
      "V<C-X>",
      "${c}12345",
      "${c}12344",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test dec visual multiple numbers`() {
    doTest(
      "v10w<C-X>",
      "11 <- should not be decremented |${c}11| should not be decremented -> 12",
      "11 <- should not be decremented |${c}10| should not be decremented -> 12",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test dec visual part of number`() {
    doTest(
      "v4l<C-X>",
      "11111${c}33333111111",
      "11111${c}33332111111",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test dec visual multiple lines`() {
    doTest(
      "V2j<C-X>",
      """
                    no dec 1
                    no dec 1
                    ${c}dec    5
                    dec   5
                    dec   5
                    no dec 1
                    no dec 1

      """.trimIndent(),
      """
                    no dec 1
                    no dec 1
                    ${c}dec    4
                    dec   4
                    dec   4
                    no dec 1
                    no dec 1

      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test dec visual 1000 multiple lines`() {
    doTest(
      "V2j<C-X>",
      """
                    ${c}1000
                    1000
                    1000
      """.trimIndent(),
      """
                    ${c}999
                    999
                    999
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test dec visual multiple numbers on line`() {
    doTest(
      "V<C-X>",
      "1 should$c not be decremented -> 2",
      "${c}0 should not be decremented -> 2",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test change number dec visual action`() {
    typeTextInFile(
      injector.parser.parseKeys("Vj<C-X>"),
      """
                    ${c}1
                    2
                    3
                    ${c}4
                    5
      """.trimIndent()
    )
    assertState(
      """
                ${c}0
                1
                3
                ${c}3
                4
      """.trimIndent()
    )
  }
}
