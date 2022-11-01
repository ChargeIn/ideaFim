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

package org.jetbrains.plugins.ideafim.action.motion.updown

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class MotionGotoLineLastActionTest : FimTestCase() {
  fun `test simple motion`() {
    doTest(
      "G",
      """
                A Discovery

                I found it in a legendary land
                all ${c}rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                ${c}hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test motion with count`() {
    doTest(
      "5G",
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                ${c}where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test motion with large count`() {
    doTest(
      "100G",
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                ${c}hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test motion with zero count`() {
    doTest(
      "0G",
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                ${c}hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test moves caret to first non-blank char`() {
    doTest(
      "G",
      """
        |       A Discovery
        |
        |       I found it in a legendary land
        |       all ${c}rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard by the torrent of a mountain pass.
      """.trimMargin(),
      """
        |       A Discovery
        |
        |       I found it in a legendary land
        |       all rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       ${c}hard by the torrent of a mountain pass.
      """.trimMargin(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test moves caret to same column with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    doTest(
      "G",
      """
        |       A Discovery
        |
        |       I found it in a legendary land
        |       all ${c}rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard by the torrent of a mountain pass.
      """.trimMargin(),
      """
        |       A Discovery
        |
        |       I found it in a legendary land
        |       all rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard$c by the torrent of a mountain pass.
      """.trimMargin(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test with last empty line`() {
    doTest(
      "G",
      """
                A Discovery

                I found it in a legendary land
                all ${c}rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
                
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
                $c
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test scrolling positions target line in middle of screen`() {
    configureByLines(100, "    I found it in a legendary land")
    typeText(injector.parser.parseKeys("70G"))
    assertPosition(69, 4)
    assertVisibleArea(52, 86)
  }

  fun `test go to last line of file puts target line at bottom of screen`() {
    configureByLines(100, "    I found it in a legendary land")
    typeText(injector.parser.parseKeys("G"))
    assertPosition(99, 4)
    assertVisibleArea(65, 99)
  }

  fun `test go to last line of file puts target line at bottom of screen with virtual space enabled`() {
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    typeText(injector.parser.parseKeys("G"))
    assertPosition(99, 4)
    assertVisibleArea(65, 99)
  }

  fun `test go to line in last half screen of file puts last line at bottom of screen`() {
    configureByLines(100, "    I found it in a legendary land")
    typeText(injector.parser.parseKeys("90G"))
    assertPosition(89, 4)
    assertVisibleArea(65, 99)
  }

  fun `test go to line in last half screen of file puts last line at bottom of screen ignoring scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    typeText(injector.parser.parseKeys("95G"))
    assertPosition(94, 4)
    assertVisibleArea(65, 99)
  }

  fun `test go to line does not scroll when default virtual space already at bottom of file`() {
    // Editor has 5 lines of virtual space by default
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(69, 85)
    typeText(injector.parser.parseKeys("G"))
    assertPosition(99, 4)
    assertVisibleArea(69, 99)
  }

  fun `test go to line does not scroll when full virtual space already at bottom of file`() {
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    setPositionAndScroll(85, 85)
    typeText(injector.parser.parseKeys("G"))
    assertPosition(99, 4)
    assertVisibleArea(85, 99)
  }

  fun `test go to line does not scroll when last line is less than scrolloff above bottom of file`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    setPositionAndScroll(67, 97)
    typeText(injector.parser.parseKeys("G"))
    assertPosition(99, 4)
    assertVisibleArea(67, 99)
  }

  fun `test go to line does not scroll when last line is less than scrolloff above bottom of file with folds`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    typeText(injector.parser.parseKeys("20G" + "V10j" + ":'< +'>action CollapseSelection<CR>" + "V"))
    setPositionAndScroll(67, 97)
    typeText(injector.parser.parseKeys("G"))
    assertPosition(99, 4)
    assertVisibleArea(67, 99)
  }
}
