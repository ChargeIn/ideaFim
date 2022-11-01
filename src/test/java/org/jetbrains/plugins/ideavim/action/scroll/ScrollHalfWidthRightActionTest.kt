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
For the following four commands the cursor follows the screen.  If the
character that the cursor is on is moved off the screen, the cursor is moved
to the closest character that is on the screen.  The value of 'sidescroll' is
not used.

                                                       *zH*
zH                      Move the view on the text half a screenwidth to the
                        left, thus scroll the text half a screenwidth to the
                        right.  This only works when 'wrap' is off.

[count] is used but undocumented.
 */
class ScrollHalfWidthRightActionTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll half page width`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("200|" + "ze" + "zH"))
    assertPosition(0, 159)
    assertVisibleLineBounds(0, 80, 159)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll keeps cursor in place if already in scrolled area`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("100|" + "zs" + "zH"))
    assertPosition(0, 99)
    // Scroll right 40 characters 99 -> 59
    assertVisibleLineBounds(0, 59, 138)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll moves cursor if moves off screen`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("100|" + "ze" + "zH"))
    assertPosition(0, 79)
    assertVisibleLineBounds(0, 0, 79)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll count half page widths`() {
    configureByColumns(400)
    typeText(injector.parser.parseKeys("350|" + "ze" + "3zH"))
    assertPosition(0, 229)
    assertVisibleLineBounds(0, 150, 229)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll half page width with sidescrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("150|" + "ze" + "zH"))
    assertPosition(0, 109)
    assertVisibleLineBounds(0, 40, 119)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll half page width ignores sidescroll`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("200|" + "ze" + "zH"))
    assertPosition(0, 159)
    assertVisibleLineBounds(0, 80, 159)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll at start of line does nothing`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("zH"))
    assertPosition(0, 0)
    assertVisibleLineBounds(0, 0, 79)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll near start of line does nothing`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("10|" + "zH"))
    assertPosition(0, 9)
    assertVisibleLineBounds(0, 0, 79)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll includes inlay visual column in half page width`() {
    configureByColumns(200)
    addInlay(180, true, 5)
    typeText(injector.parser.parseKeys("190|" + "ze" + "zH"))
    // The inlay is included in the count of scrolled visual columns
    assertPosition(0, 150)
    assertVisibleLineBounds(0, 71, 150)
  }

  @TestWithoutNeofim(SkipNeofimReason.SCROLL)
  fun `test scroll with inlay and cursor in scrolled area`() {
    configureByColumns(200)
    addInlay(180, true, 5)
    typeText(injector.parser.parseKeys("170|" + "ze" + "zH"))
    // The inlay is after the cursor, and does not affect scrolling
    assertPosition(0, 129)
    assertVisibleLineBounds(0, 50, 129)
  }
}
