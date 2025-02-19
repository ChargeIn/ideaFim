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
import org.jetbrains.plugins.ideafim.FimTestCase

/*
                                                       *zb*
zb                      Like "z-", but leave the cursor in the same column.
 */
class ScrollLastScreenLineActionTest : FimTestCase() {
  fun `test scroll current line to bottom of screen`() {
    configureByPages(5)
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(60, 0)
    assertVisibleArea(26, 60)
  }

  fun `test scroll current line to bottom of screen and leave cursor in current column`() {
    configureByLines(100, "    I found it in a legendary land")
    setPositionAndScroll(40, 60, 14)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(60, 14)
    assertVisibleArea(26, 60)
  }

  fun `test scroll current line to bottom of screen minus scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(60, 0)
    assertVisibleArea(36, 70)
  }

  fun `test scrolls count line to bottom of screen`() {
    configureByPages(5)
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("100zb"))
    assertPosition(99, 0)
    assertVisibleArea(65, 99)
  }

  fun `test scrolls count line to bottom of screen minus scrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("100zb"))
    assertPosition(99, 0)
    assertVisibleArea(75, 109)
  }

  fun `test scrolls current line to bottom of screen ignoring scrolljump`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolljumpName, FimInt(10))
    configureByPages(5)
    setPositionAndScroll(40, 60)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(60, 0)
    assertVisibleArea(26, 60)
  }

  fun `test scrolls correctly when less than a page to scroll`() {
    configureByPages(5)
    setPositionAndScroll(5, 15)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(15, 0)
    assertVisibleArea(0, 34)
  }

  fun `test scrolls last line to bottom of screen with virtual space`() {
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    setPositionAndScroll(80, 99, 4)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(99, 4)
    assertVisibleArea(65, 99)
  }

  fun `test scrolls last line to bottom of screen with virtual space when caret less than scrolloff from bottom`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(100, "    I found it in a legendary land")
    setEditorVirtualSpace()
    setPositionAndScroll(80, 97, 4)
    typeText(injector.parser.parseKeys("zb"))
    assertPosition(97, 4)
    assertVisibleArea(65, 99)
  }
}
