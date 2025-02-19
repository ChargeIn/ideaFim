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

import com.flop.idea.fim.command.MappingMode
import org.jetbrains.plugins.ideafim.FimTestCase

class UnMapCommandTest : FimTestCase() {
  override fun setUp() {
    super.setUp()
    configureByText(
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent()
    )
  }

  fun testMapKtoJ() {
    putMapping(MappingMode.N, "k", "j", false)

    typeText(commandToKeys("unmap k"))

    assertNoMapping("k")
  }

  fun `test mappings in insert mode`() {
    putMapping(MappingMode.I, "jk", "<Esc>", false)

    typeText(commandToKeys("iunmap jk"))

    assertNoMapping("jk")
  }

  fun `test removing only part of keys`() {
    putMapping(MappingMode.I, "jk", "<Esc>", false)

    typeText(commandToKeys("unmap j"))

    assertNoMapping("j")
    assertMappingExists("jk", "<Esc>", MappingMode.I)
  }

  fun `test removing keys from a different mode`() {
    putMapping(MappingMode.I, "jk", "<Esc>", false)

    typeText(commandToKeys("unmap jk"))

    assertMappingExists("jk", "<Esc>", MappingMode.I)
  }
}
