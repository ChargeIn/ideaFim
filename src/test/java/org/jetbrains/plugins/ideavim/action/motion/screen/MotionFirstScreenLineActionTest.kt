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

package org.jetbrains.plugins.ideafim.action.motion.screen

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class MotionFirstScreenLineActionTest : FimTestCase() {
  fun `test move caret to first line of screen`() {
    configureByLines(50, "    I found it in a legendary land")
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(0, 4)
  }

  fun `test move caret to first line of screen further down file`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(40, 4)
  }

  fun `test move caret to count line from top of screen`() {
    configureByLines(50, "    I found it in a legendary land")
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("10H"))
    assertPosition(9, 4)
  }

  fun `test move caret to count line from top of screen further down file`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("10H"))
    assertPosition(49, 4)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test move caret to too large count line from top of screen`() {
    assertEquals(35, screenHeight)
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("100H"))
    assertPosition(74, 4)
  }

  fun `test move caret ignores scrolloff when top of screen is top of file`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(50, "    I found it in a legendary land")
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(0, 4)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test move caret applies scrolloff when top of screen is not top of file`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(50, "    I found it in a legendary land")
    setPositionAndScroll(1, 20)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(11, 4)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test move caret applies scrolloff when top of screen is not top of file 2`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(30, 4)
  }

  fun `test move caret to first screen line with count and scrolloff at top of file`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(50, "    I found it in a legendary land")
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("5H"))
    assertPosition(4, 4)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test move caret to first screen line with count and scrolloff not at top of file`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40)
    typeText(injector.parser.parseKeys("5H"))
    assertPosition(30, 4)
  }

  fun `test operator pending acts to first screen line`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40, 10)
    typeText(injector.parser.parseKeys("dH"))
    assertPosition(20, 4)
    assertLineCount(79)
  }

  fun `test operator pending acts on count line from top of screen`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40, 10)
    typeText(injector.parser.parseKeys("d5H"))
    assertPosition(24, 4)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test operator pending acts to first screen line with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40, 10)
    typeText(injector.parser.parseKeys("dH"))
    assertPosition(20, 10)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test operator pending acts on count line from top of screen with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40, 10)
    typeText(injector.parser.parseKeys("d5H"))
    assertPosition(24, 10)
  }

  fun `test operator pending acts to first screen line and then scrolls scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 40)
    typeText(injector.parser.parseKeys("dH"))
    assertPosition(20, 4)
    assertVisibleArea(10, 44)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test move caret to same column with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    configureByLines(50, "    I found it in a legendary land")
    setPositionAndScroll(0, 20, 10)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(0, 10)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test move caret to end of shorter line with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    configureByLines(70, "    I found it in a legendary land")
    setPositionAndScroll(10, 30, 10)
    typeText(injector.parser.parseKeys("A" + " extra text" + "<Esc>"))
    typeText(injector.parser.parseKeys("H"))
    assertPosition(10, 33)
  }

  fun `test move caret to first line of screen with inlays`() {
    // We're not scrolling, so inlays don't affect anything. Just place the caret on the first visible line
    configureByLines(50, "    I found it in a legendary land")
    addBlockInlay(com.flop.idea.fim.helper.EditorHelper.getOffset(myFixture.editor, 5, 5), true, 10)
    setPositionAndScroll(0, 20, 10)
    typeText(injector.parser.parseKeys("H"))
    assertPosition(0, 4)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test keep caret on screen when count is greater than visible lines plus inlays`() {
    assertEquals(35, screenHeight)
    configureByLines(50, "    I found it in a legendary land")
    addBlockInlay(com.flop.idea.fim.helper.EditorHelper.getOffset(myFixture.editor, 5, 5), true, 10)
    setPositionAndScroll(0, 20, 10)
    // Should move to the 34th visible line. We have space for 35 lines, but we're using some of that for inlays
    typeText(injector.parser.parseKeys("34H"))
    assertPosition(24, 4)
  }
}
