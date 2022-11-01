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
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/*
                                                       *z+*
z+                      Without [count]: Redraw with the line just below the
                        window at the top of the window.  Put the cursor in
                        that line, at the first non-blank in the line.
                        With [count]: just like "z<CR>".
 */
class ScrollFirstScreenLinePageStartActionTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scrolls first line on next page to top of screen`() {
    configureByPages(5)
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("z+"))
    assertPosition(35, 0)
    assertVisibleArea(35, 69)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scrolls to first non-blank in line`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("z+"))
    assertPosition(35, 4)
    assertVisibleArea(35, 69)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scrolls first line on next page to scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("z+"))
    assertPosition(35, 0)
    assertVisibleArea(25, 59)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scrolls first line on next page ignores scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("z+"))
    assertPosition(35, 0)
    assertVisibleArea(35, 69)
  }

  fun `test count z+ scrolls count line to top of screen`() {
    configureByPages(5)
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("100z+"))
    assertPosition(99, 0)
    assertVisibleArea(99, 133)
  }

  fun `test count z+ scrolls count line to top of screen plus scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 20)
    typeText(injector.parser.parseKeys("100z+"))
    assertPosition(99, 0)
    assertVisibleArea(89, 123)
  }

  @FimBehaviorDiffers(description = "Requires virtual space support")
  fun `test scroll on penultimate page`() {
    configureByPages(5)
    setPositionAndScroll(130, 145)
    typeText(injector.parser.parseKeys("z+"))
    assertPosition(165, 0)
    assertVisibleArea(146, 175)
  }
}
