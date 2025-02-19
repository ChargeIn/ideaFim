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
                                                       *CTRL-Y*
CTRL-Y                  Scroll window [count] lines upwards in the buffer.
                        The text moves downwards on the screen.
                        Note: When using the MS-Windows key bindings CTRL-Y is
                        remapped to redo.
 */
class ScrollLineUpActionTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll single line up`() {
    configureByPages(5)
    setPositionAndScroll(29, 29)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertPosition(29, 0)
    assertVisibleArea(28, 62)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll line up will keep cursor on screen`() {
    configureByPages(5)
    setPositionAndScroll(29, 63)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertPosition(62, 0)
    assertVisibleArea(28, 62)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll line up will maintain current column at start of line with sidescrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(29, 63, 5)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertPosition(62, 5)
    assertVisibleArea(28, 62)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll count lines up`() {
    configureByPages(5)
    setPositionAndScroll(29, 29)
    typeText(injector.parser.parseKeys("10<C-Y>"))
    assertPosition(29, 0)
    assertVisibleArea(19, 53)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll count lines up will keep cursor on screen`() {
    configureByPages(5)
    setPositionAndScroll(29, 63)
    typeText(injector.parser.parseKeys("10<C-Y>"))
    assertPosition(53, 0)
    assertVisibleArea(19, 53)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test too many lines up stops at zero`() {
    configureByPages(5)
    setPositionAndScroll(29, 29)
    typeText(injector.parser.parseKeys("100<C-Y>"))
    assertPosition(29, 0)
    assertVisibleArea(0, 34)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test too many lines up stops at zero and keeps cursor on screen`() {
    configureByPages(5)
    setPositionAndScroll(59, 59)
    typeText(injector.parser.parseKeys("100<C-Y>"))
    assertPosition(34, 0)
    assertVisibleArea(0, 34)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll up uses scrolloff and moves cursor`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(20, 44)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertPosition(43, 0)
    assertVisibleArea(19, 53)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll up is not affected by scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(29, 63)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertPosition(62, 0)
    assertVisibleArea(28, 62)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll line up in visual mode`() {
    configureByPages(5)
    setPositionAndScroll(29, 29)
    typeText(injector.parser.parseKeys("Vjjjj" + "<C-Y>"))
    assertVisibleArea(28, 62)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll line up with virtual space`() {
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    setPositionAndScroll(85, 90, 4)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertVisibleArea(84, 99)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll line up with virtual space and scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    // Last line is scrolloff from top. <C-Y> should just move last line down
    setPositionAndScroll(89, 99, 4)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertVisibleArea(88, 99)
    assertVisualPosition(99, 4)
  }

  // This actually works, but the set up puts us in the wrong position
  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll line up on last line with scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    setPositionAndScroll(65, 99, 4)
    typeText(injector.parser.parseKeys("<C-Y>"))
    assertVisibleArea(64, 98)
    assertVisualPosition(88, 4) // Moves caret up by scrolloff
  }
}
