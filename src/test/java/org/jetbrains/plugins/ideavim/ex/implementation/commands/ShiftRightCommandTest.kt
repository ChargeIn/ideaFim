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

import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */
class ShiftRightCommandTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT, "Replace term codes issues")
  fun `test simple right shift`() {
    val before = """        I found it in a legendary land
                      |        ${c}all rocks and lavender and tufted grass,
                      |        where it was settled on some sodden sand
                      |        hard by the torrent of a mountain pass.
                       """.trimMargin()
    configureByJavaText(before)

    typeText(commandToKeys(">"))

    val after = """        I found it in a legendary land
                      |            ${c}all rocks and lavender and tufted grass,
                      |        where it was settled on some sodden sand
                      |        hard by the torrent of a mountain pass.
                       """.trimMargin()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT, "Replace term codes issues")
  fun `test double right shift`() {
    val before = """        I found it in a legendary land
                      |        ${c}all rocks and lavender and tufted grass,
                      |        where it was settled on some sodden sand
                      |        hard by the torrent of a mountain pass.
                       """.trimMargin()
    configureByJavaText(before)

    typeText(commandToKeys(">>"))

    val after = """        I found it in a legendary land
                      |                ${c}all rocks and lavender and tufted grass,
                      |        where it was settled on some sodden sand
                      |        hard by the torrent of a mountain pass.
                       """.trimMargin()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT, "Replace term codes issues")
  fun `test range right shift`() {
    val before = """        I found it in a legendary land
                      |        ${c}all rocks and lavender and tufted grass,
                      |        where it was settled on some sodden sand
                      |        hard by the torrent of a mountain pass.
                       """.trimMargin()
    configureByJavaText(before)

    typeText(commandToKeys("3,4>"))

    val after = """        I found it in a legendary land
                      |        all rocks and lavender and tufted grass,
                      |            ${c}where it was settled on some sodden sand
                      |            hard by the torrent of a mountain pass.
                       """.trimMargin()
    assertState(after)
  }

  fun `test multiple carets`() {
    val before = """    I found it in a legendary land
                      |${c}all rocks and lavender and tufted grass,
                      |    ${c}where it was settled on some sodden sand
                      |    hard by the$c torrent of a mountain pass.
                       """.trimMargin()
    configureByJavaText(before)

    typeText(commandToKeys(">"))

    val after = """    I found it in a legendary land
                      |    ${c}all rocks and lavender and tufted grass,
                      |        ${c}where it was settled on some sodden sand
                      |        ${c}hard by the torrent of a mountain pass.
                       """.trimMargin()
    assertState(after)
  }
}
