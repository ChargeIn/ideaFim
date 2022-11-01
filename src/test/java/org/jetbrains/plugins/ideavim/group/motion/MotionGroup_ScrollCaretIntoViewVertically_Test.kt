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

package org.jetbrains.plugins.ideafim.group.motion

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

@Suppress("ClassName")
class MotionGroup_ScrollCaretIntoViewVertically_Test : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving up causes scrolling up`() {
    configureByPages(5)
    setPositionAndScroll(19, 24)

    typeText(injector.parser.parseKeys("12k"))
    assertPosition(12, 0)
    assertVisibleArea(12, 46)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up with scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(19, 24)

    typeText(injector.parser.parseKeys("12k"))
    assertPosition(12, 0)
    assertVisibleArea(3, 37)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up with scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(19, 29)

    typeText(injector.parser.parseKeys("12k"))
    assertPosition(17, 0)
    assertVisibleArea(12, 46)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up with scrolljump and scrolloff 1`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)

    setPositionAndScroll(19, 29)
    typeText(injector.parser.parseKeys("12k"))
    assertPosition(17, 0)
    assertVisibleArea(8, 42)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up with scrolljump and scrolloff 2`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(29, 39)

    typeText(injector.parser.parseKeys("20k"))
    assertPosition(19, 0)
    assertVisibleArea(10, 44)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up with collapsed folds`() {
    configureByPages(5)
    // TODO: Implement zf
    typeText(injector.parser.parseKeys("40G" + "Vjjjj" + ":'< +'>action CollapseSelection<CR>" + "V"))
    setPositionAndScroll(29, 49)

    typeText(injector.parser.parseKeys("30k"))
    assertPosition(15, 0)
    assertVisibleArea(15, 53)
  }

  // TODO: Handle soft wraps
//  fun `test scroll up with soft wraps`() {
//  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up more than half height moves caret to middle 1`() {
    configureByPages(5)
    setPositionAndScroll(115, 149)

    typeText(injector.parser.parseKeys("50k"))
    assertPosition(99, 0)
    assertVisualLineAtMiddleOfScreen(99)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up more than half height moves caret to middle with scrolloff`() {
    configureByPages(5)
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    setPositionAndScroll(99, 109)
    assertPosition(109, 0)

    typeText(injector.parser.parseKeys("21k"))
    assertPosition(88, 0)
    assertVisualLineAtMiddleOfScreen(88)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll up with less than half height moves caret to top of screen`() {
    configureByPages(5)
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    setPositionAndScroll(99, 109)

    typeText(injector.parser.parseKeys("20k"))
    assertPosition(89, 0)
    assertVisibleArea(80, 114)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving down causes scrolling down`() {
    configureByPages(5)
    setPositionAndScroll(0, 29)

    typeText(injector.parser.parseKeys("12j"))
    assertPosition(41, 0)
    assertVisibleArea(7, 41)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 29)

    typeText(injector.parser.parseKeys("12j"))
    assertPosition(41, 0)
    assertVisibleArea(11, 45)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 24)

    typeText(injector.parser.parseKeys("12j"))
    assertPosition(36, 0)
    assertVisibleArea(7, 41)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump and scrolloff 1`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 24)

    typeText(injector.parser.parseKeys("12j"))
    assertPosition(36, 0)
    assertVisibleArea(10, 44)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump and scrolloff 2`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(15))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 24)

    typeText(injector.parser.parseKeys("20j"))
    assertPosition(44, 0)
    assertVisibleArea(17, 51)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump and scrolloff 3`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(20))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 24)

    typeText(injector.parser.parseKeys("25j"))
    assertPosition(49, 0)
    assertVisibleArea(24, 58)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump and scrolloff 4`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(11))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 24)

    typeText(injector.parser.parseKeys("12j"))
    assertPosition(36, 0)
    assertVisibleArea(11, 45)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump and scrolloff 5`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 29)

    typeText(injector.parser.parseKeys("12j"))
    assertPosition(41, 0)
    assertVisibleArea(12, 46)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down with scrolljump and scrolloff 6`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(5))
    configureByPages(5)
    setPositionAndScroll(0, 24)

    typeText(injector.parser.parseKeys("20j"))
    assertPosition(44, 0)
    assertVisibleArea(15, 49)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test scroll down too large cursor is centred`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 19)

    typeText(injector.parser.parseKeys("35j"))
    assertPosition(54, 0)
    assertVisualLineAtMiddleOfScreen(54)
  }

  private fun assertVisualLineAtMiddleOfScreen(expected: Int) {
    assertEquals(expected, com.flop.idea.fim.helper.EditorHelper.getVisualLineAtMiddleOfScreen(myFixture.editor))
  }
}
