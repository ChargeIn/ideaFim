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

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.FimTestCase

class InsertAfterLineEndActionTest : FimTestCase() {
  fun `test insert after line end action`() {
    typeTextInFile(
      injector.parser.parseKeys("A" + " four" + "<ESC>"),
      """
                    one two ${c}three
                    sev${c}en si${c}x five

      """.trimIndent()
    )
    assertState(
      """
    one two three fou${c}r
    seven six five fou${c}r

      """.trimIndent()
    )
  }

  fun `test multiple carets`() {
    doTest(
      "AHello<esc>",
      """
                ${c}A Discovery

                ${c}I found it in a legendary land
                all rocks and ${c}lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A DiscoveryHell${c}o

                I found it in a legendary landHell${c}o
                all rocks and lavender and tufted grass,Hell${c}o
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
    assertMode(FimStateMachine.Mode.COMMAND)
  }
}
