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
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.FimTestCase

/*
                                                       *z.*
z.                      Redraw, line [count] at center of window (default
                        cursor line).  Put cursor at first non-blank in the
                        line.
 */
class ScrollMiddleScreenLineStartActionTest : FimTestCase() {
  fun `test scrolls current line to middle of screen`() {
    configureByPages(5)
    setPositionAndScroll(40, 45)
    typeText(injector.parser.parseKeys("z."))
    assertPosition(45, 0)
    assertVisibleArea(28, 62)
  }

  fun `test scrolls current line to middle of screen and moves cursor to first non-blank`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(40, 45, 14)
    typeText(injector.parser.parseKeys("z."))
    assertPosition(45, 4)
    assertVisibleArea(28, 62)
  }

  fun `test scrolls count line to the middle of the screen`() {
    configureByPages(5)
    setPositionAndScroll(40, 45)
    typeText(injector.parser.parseKeys("100z."))
    assertPosition(99, 0)
    assertVisibleArea(82, 116)
  }

  fun `test scrolls count line ignoring scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(40, 45)
    typeText(injector.parser.parseKeys("100z."))
    assertPosition(99, 0)
    assertVisibleArea(82, 116)
  }

  fun `test scrolls correctly when count line is in first half of first page`() {
    configureByPages(5)
    setPositionAndScroll(40, 45)
    typeText(injector.parser.parseKeys("10z."))
    assertPosition(9, 0)
    assertVisibleArea(0, 34)
  }

  @FimBehaviorDiffers(description = "Virtual space at end of file")
  fun `test scrolls last line of file correctly`() {
    configureByPages(5)
    setPositionAndScroll(0, 0)
    typeText(injector.parser.parseKeys("175z."))
    assertPosition(174, 0)
    assertVisibleArea(146, 175)
  }
}
