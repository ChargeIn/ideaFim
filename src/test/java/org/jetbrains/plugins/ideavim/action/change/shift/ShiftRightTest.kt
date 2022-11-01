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

package org.jetbrains.plugins.ideafim.action.change.shift

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class ShiftRightTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun `test shift till new line`() {
    val file = """
            A Discovery

              I found it in a legendary l${c}and
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys(">W"), file)
    assertState(
      """
            A Discovery

                  ${c}I found it in a legendary land
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
      """.trimIndent()
    )
  }

  // VIM-407
  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftShiftsOneCharacterSingleLine() {
    configureByText("<caret>w\n")
    typeText(injector.parser.parseKeys(">>"))
    assertState("    w\n")
  }

  // VIM-407
  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftShiftsOneCharacterMultiLine() {
    configureByText("Hello\n<caret>w\nWorld")
    typeText(injector.parser.parseKeys(">>"))
    assertState("Hello\n    w\nWorld")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftShiftsMultipleCharactersOneLine() {
    configureByText("<caret>Hello, world!\n")
    typeText(injector.parser.parseKeys(">>"))
    assertState("    Hello, world!\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftShiftsMultipleCharactersMultipleLines() {
    configureByText("<caret>Hello,\nworld!\n")
    typeText(injector.parser.parseKeys("j>>"))
    assertState("Hello,\n    world!\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsSingleLineSelection() {
    configureByText("<caret>Hello,\nworld!\n")
    typeText(injector.parser.parseKeys("jv$>>"))
    assertState("Hello,\n    world!\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsMultiLineSelection() {
    configureByText("<caret>Hello,\nworld!\n")
    typeText(injector.parser.parseKeys("vj$>>"))
    assertState("    Hello,\n    world!\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsMultiLineSelectionSkipsNewline() {
    configureByText("<caret>Hello,\nworld!\n\n")
    typeText(injector.parser.parseKeys("vG$>>"))
    assertState("    Hello,\n    world!\n\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsMultiLineSelectionSkipsNewlineWhenCursorNotInFirstColumn() {
    configureByText("<caret>Hello,\n\nworld!\n")
    typeText(injector.parser.parseKeys("lVG>"))
    assertState("    Hello,\n\n    world!\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsMultiLineSelectionAddsTrailingWhitespaceIfTherePreviouslyWas() {
    configureByText("<caret>Hello,\n    \nworld!\n")
    typeText(injector.parser.parseKeys("lVG>"))
    assertState("    Hello,\n        \n    world!\n")
  }

  // VIM-705 repeating a multiline indent would only affect last line
  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsMultiLineSelectionRepeat() {
    configureByText("<caret>a\nb\n")
    typeText(injector.parser.parseKeys("Vj>."))
    assertState("        a\n        b\n")
  }

  fun testShiftsDontCrashKeyHandler() {
    configureByText("\n")
    typeText(injector.parser.parseKeys("<I<>" + "<I<>"))
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun testShiftsVisualBlockMode() {
    configureByText("foo<caret>foo\nfoobar\nfoobaz\n")
    typeText(injector.parser.parseKeys("<C-V>jjl>"))
    assertState("foo    foo\nfoo    bar\nfoo    baz\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun `test shift right positions caret at first non-blank char`() {
    val file = """
      |A Discovery
      |
      |       I found it in a legendary l${c}and
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
    """.trimMargin()
    typeTextInFile(injector.parser.parseKeys(">>"), file)
    assertState(
      """
      |A Discovery

      |           ${c}I found it in a legendary land
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
      """.trimMargin()
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun `test shift right does not move caret with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    val file = """
      |A Discovery
      |
      |       I found it in a ${c}legendary land
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
    """.trimMargin()
    typeTextInFile(injector.parser.parseKeys(">>"), file)
    assertState(
      """
      |A Discovery

      |           I found it i${c}n a legendary land
      |       all rocks and lavender and tufted grass,
      |       where it was settled on some sodden sand
      |       hard by the torrent of a mountain pass.
      """.trimMargin()
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun `test shift ctrl-t`() {
    val file = """
            A Discovery

              I found it in a legendary l${c}and
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("i<C-T>"), file)
    assertState(
      """
            A Discovery

                  I found it in a legendary land
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
      """.trimIndent()
    )
  }
}
