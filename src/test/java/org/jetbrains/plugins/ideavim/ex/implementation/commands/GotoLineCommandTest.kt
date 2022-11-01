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

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class GotoLineCommandTest : FimTestCase() {
  fun `test goto explicit line`() {
    val before = """
      A Discovery

      I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("3")
    val after = """
      A Discovery

      ${c}I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto explicit line check history`() {
    val before = """
      A Discovery

      I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("3")
    val after = """
      A Discovery

      ${c}I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)

    val register = com.flop.idea.fim.FimPlugin.getRegister().getRegister(':')
    kotlin.test.assertNotNull(register)
    kotlin.test.assertEquals("3", register.text)
  }

  fun `test goto positive relative line`() {
    val before = """
      A Discovery

      I found it ${c}in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("+2")
    val after = """
      A Discovery

      I found it in a legendary land
      all rocks and lavender and tufted grass,
      ${c}where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto using forward search range`() {
    val before = """
      A Discovery

      I found it ${c}in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("/settled")
    val after = """
      A Discovery

      I found it in a legendary land
      all rocks and lavender and tufted grass,
      ${c}where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto using backward search range`() {
    val before = """
      A Discovery

      I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("/lavender")
    val after = """
      A Discovery

      I found it in a legendary land
      ${c}all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto negative relative line`() {
    val before = """
      A Discovery

      I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it ${c}was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("-2")
    val after = """
      A Discovery

      ${c}I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto line moves to first non-blank char`() {
    val before = """
      A Discovery

          I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("3")
    val after = """
      A Discovery

          ${c}I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto zero relative line moves to first non-blank char on current line`() {
    val before = """
      A Discovery

          I found it ${c}in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("+0")
    val after = """
      A Discovery

          ${c}I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test goto line moves to same column with nostartofline option`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    val before = """
      A Discovery

          I found it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("3")
    val after = """
      A Discovery

          I found ${c}it in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test goto zero relative line with nostartofline option does not move caret`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    val before = """
      A Discovery

          I found it ${c}in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    enterCommand("+0")
    val after = """
      A Discovery

          I found it ${c}in a legendary land
      all rocks and lavender and tufted grass,
      where it was settled on some sodden sand
      hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }

  fun `test goto line with scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    enterCommand("30")
    assertPosition(29, 4)
    assertTopLogicalLine(5)
  }

  fun `test goto relative line with scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    enterCommand("+30")
    assertPosition(30, 4)
    assertTopLogicalLine(6)
  }
}
