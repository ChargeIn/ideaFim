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

package org.jetbrains.plugins.ideafim.ex.implementation.commands

import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.FimTestCase

class JumpsCommandTest : FimTestCase() {
  fun `test shows empty list`() {
    configureByText("")
    enterCommand("jumps")
    assertExOutput(" jump line  col file/text\n>\n")
  }

  fun `test show jump list`() {
    configureByText(
      """I found ${c}it in a legendary land
                      |all rocks and lavender and tufted grass,
                      |where it was settled on some sodden sand
                      |hard by the torrent of a mountain pass.
                      |
                      |The features it combines mark it as new
                      |to science: shape and shade -- the special tinge,
                      |akin to moonlight, tempering its blue,
                      |the dingy underside, the checquered fringe.
                      """.trimMargin()
    )

    enterSearch("sodden")
    enterSearch("shape")
    enterSearch("rocks", false)
    enterSearch("underside")

    enterCommand("jumps")
    assertExOutput(
      """ jump line  col file/text
                     |   4     1    8 I found it in a legendary land
                     |   3     3   29 where it was settled on some sodden sand
                     |   2     7   12 to science: shape and shade -- the special tinge,
                     |   1     2    4 all rocks and lavender and tufted grass,
                     |>
                     |""".trimMargin()
    )
  }

  fun `test highlights current jump spot`() {
    configureByText(
      """I found ${c}it in a legendary land
                      |all rocks and lavender and tufted grass,
                      |where it was settled on some sodden sand
                      |hard by the torrent of a mountain pass.
                      |
                      |The features it combines mark it as new
                      |to science: shape and shade -- the special tinge,
                      |akin to moonlight, tempering its blue,
                      |the dingy underside, the checquered fringe.
                      """.trimMargin()
    )

    enterSearch("sodden")
    enterSearch("shape")
    enterSearch("rocks", false)
    enterSearch("underside")

    typeText(injector.parser.parseKeys("<C-O>" + "<C-O>"))

    enterCommand("jumps")
    assertExOutput(
      """ jump line  col file/text
                     |   2     1    8 I found it in a legendary land
                     |   1     3   29 where it was settled on some sodden sand
                     |>  0     7   12 to science: shape and shade -- the special tinge,
                     |   1     2    4 all rocks and lavender and tufted grass,
                     |   2     9   10 the dingy underside, the checquered fringe.
                     |""".trimMargin()
    )
  }

  fun `test list trims and truncates`() {
    val indent = " ".repeat(100)
    val text = "Really long line ".repeat(1000)
    configureByText(indent + text)

    enterSearch("long")

    enterCommand("jumps")
    assertExOutput(
      """ jump line  col file/text
                     |   1     1    0 ${text.substring(0, 200)}
                     |>
                     |""".trimMargin()
    )
  }

  fun `test correctly encodes non-printable characters`() {
    configureByText("\u0009Hello\u0006World\u007f")

    typeText(injector.parser.parseKeys("G"))

    enterCommand("jumps")
    assertExOutput(
      """ jump line  col file/text
                     |   1     1    0 Hello^FWorld^?
                     |>
                     |""".trimMargin()
    )
  }
}
