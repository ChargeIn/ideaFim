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
                                                       *zt*
zt                      Like "z<CR>", but leave the cursor in the same
                        column.
 */
class ScrollFirstScreenLineActionTest : FimTestCase() {
  fun `test scroll current line to top of screen`() {
    configureByPages(5)
    setPositionAndScroll(0, 19)
    typeText(injector.parser.parseKeys("zt"))
    assertPosition(19, 0)
    assertVisibleArea(19, 53)
  }

  fun `test scroll current line to top of screen and leave cursor in current column`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(0, 19, 14)
    typeText(injector.parser.parseKeys("zt"))
    assertPosition(19, 14)
    assertVisibleArea(19, 53)
  }

  fun `test scroll current line to top of screen minus scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 19)
    typeText(injector.parser.parseKeys("zt"))
    assertPosition(19, 0)
    assertVisibleArea(9, 43)
  }

  fun `test scrolls count line to top of screen`() {
    configureByPages(5)
    setPositionAndScroll(0, 19)
    typeText(injector.parser.parseKeys("100zt"))
    assertPosition(99, 0)
    assertVisibleArea(99, 133)
  }

  fun `test scrolls count line to top of screen minus scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 19)
    typeText(injector.parser.parseKeys("zt"))
    assertPosition(19, 0)
    assertVisibleArea(19, 53)
  }

  @FimBehaviorDiffers(description = "Virtual space at end of file")
  fun `test invalid count scrolls last line to top of screen`() {
    configureByPages(5)
    setPositionAndScroll(0, 19)
    typeText(injector.parser.parseKeys("1000zt"))
    assertPosition(175, 0)
    assertVisibleArea(146, 175)
  }

  fun `test scroll current line to top of screen ignoring scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(0, 19)
    typeText(injector.parser.parseKeys("zt"))
    assertPosition(19, 0)
    assertVisibleArea(19, 53)
  }
}
