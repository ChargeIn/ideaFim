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

package org.jetbrains.plugins.ideafim.action.scroll

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/*
                                                       *CTRL-D*
CTRL-D                  Scroll window Downwards in the buffer.  The number of
                        lines comes from the 'scroll' option (default: half a
                        screen).  If [count] given, first set 'scroll' option
                        to [count].  The cursor is moved the same number of
                        lines down in the file (if possible; when lines wrap
                        and when hitting the end of the file there may be a
                        difference).  When the cursor is on the last line of
                        the buffer nothing happens and a beep is produced.
                        See also 'startofline' option.
 */
class ScrollHalfPageDownActionTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll half window downwards keeps cursor on same relative line`() {
    configureByPages(5)
    setPositionAndScroll(20, 25)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(42, 0)
    assertVisibleArea(37, 71)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards on last line causes beep`() {
    configureByPages(5)
    setPositionAndScroll(146, 175)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(175, 0)
    assertVisibleArea(146, 175)
    assertTrue(com.flop.idea.fim.FimPlugin.isError())
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards in bottom half of last page moves caret to the last line without scrolling`() {
    configureByPages(5)
    setPositionAndScroll(140, 165)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(175, 0)
    assertVisibleArea(141, 175)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards in bottom half of last page moves caret to the last line with scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(140, 164)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(175, 0)
    assertVisibleArea(141, 175)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards at end of file with existing virtual space moves caret without scrolling window`() {
    configureByPages(5)
    setPositionAndScroll(146, 165) // 146 at top line means bottom line is 181 (out of 175)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(175, 0)
    assertVisibleArea(146, 175)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards in top half of last page moves cursor down half a page`() {
    configureByPages(5)
    setPositionAndScroll(146, 150)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(167, 0)
    assertVisibleArea(146, 175)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll count lines downwards`() {
    configureByPages(5)
    setPositionAndScroll(100, 130)
    typeText(injector.parser.parseKeys("10<C-D>"))
    assertPosition(140, 0)
    assertVisibleArea(110, 144)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll count downwards modifies scroll option`() {
    configureByPages(5)
    setPositionAndScroll(100, 110)
    typeText(injector.parser.parseKeys("10<C-D>"))
    assertEquals((com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, OptionConstants.scrollName) as FimInt).value, 10)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards uses scroll option`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrollName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(100, 110)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(120, 0)
    assertVisibleArea(110, 144)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test count scroll downwards is limited to single page`() {
    configureByPages(5)
    setPositionAndScroll(100, 110)
    typeText(injector.parser.parseKeys("1000<C-D>"))
    assertPosition(145, 0)
    assertVisibleArea(135, 169)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards puts cursor on first non-blank column`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 25, 14)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(42, 4)
    assertVisibleArea(37, 71)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun`test scroll downwards keeps same column with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(20, 25, 14)
    typeText(injector.parser.parseKeys("<C-D>"))
    assertPosition(42, 14)
    assertVisibleArea(37, 71)
  }
}
