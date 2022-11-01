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

package org.jetbrains.plugins.ideafim.action.copy

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

class YankAndPutTest : FimOptionTestCase(OptionConstants.clipboardName) {
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.clipboardName, OptionValueType.STRING, OptionConstants.clipboard_unnamed)
  )
  fun `test yank to number register with unnamed`() {
    val before = """
            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
    """.trimIndent()
    configureByText(before)
    // Select and yank first word
    typeText(injector.parser.parseKeys("vey"))
    // Replace second word
    typeText(injector.parser.parseKeys("wvep"))
    // Replace previous word
    typeText(injector.parser.parseKeys("bbvep"))

    assertState(
      """
            I it found in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )
  }

  @FimOptionTestConfiguration(
    FimTestOption(
      OptionConstants.clipboardName,
      OptionValueType.STRING,
      OptionConstants.clipboard_unnamed + "," + OptionConstants.clipboard_ideaput
    )
  )
  fun `test yank to number register with unnamed and ideaput`() {
    val before = """
            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
    """.trimIndent()
    configureByText(before)
    // Select and yank first word
    typeText(injector.parser.parseKeys("vey"))
    // Replace second word
    typeText(injector.parser.parseKeys("wvep"))
    // Replace previous word
    typeText(injector.parser.parseKeys("bbvep"))

    assertState(
      """
            I it found in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )
  }

  @FimOptionTestConfiguration(FimTestOption(OptionConstants.clipboardName, OptionValueType.STRING, ""))
  fun `test yank to number register`() {
    val before = """
            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
    """.trimIndent()
    configureByText(before)
    // Select and yank first word
    typeText(injector.parser.parseKeys("vey"))
    // Replace second word
    typeText(injector.parser.parseKeys("wvep"))
    // Replace previous word
    typeText(injector.parser.parseKeys("bbvep"))

    assertState(
      """
            I it found in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )
  }
}
