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
import kotlin.math.roundToInt

@Suppress("ClassName")
class MotionGroup_ScrollCaretIntoViewHorizontally_Test : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right scrolls half screen to right by default`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("80|" + "l")) // 1 based
    assertPosition(0, 80) // 0 based
    assertVisibleLineBounds(0, 40, 119) // 0 based
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right scrolls half screen to right by default 2`() {
    configureByColumns(200)
    setEditorVisibleSize(100, screenHeight)
    typeText(injector.parser.parseKeys("100|" + "l"))
    assertVisibleLineBounds(0, 50, 149)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right scrolls half screen if moving too far 1`() {
    configureByColumns(400)
    typeText(injector.parser.parseKeys("70|" + "41l")) // Move more than half screen width, but scroll less
    assertVisibleLineBounds(0, 70, 149)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right scrolls half screen if moving too far 2`() {
    configureByColumns(400)
    typeText(injector.parser.parseKeys("50|" + "200l")) // Move and scroll more than half screen width
    assertVisibleLineBounds(0, 209, 288)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right with sidescroll 1`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(1))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("80|" + "l"))
    assertVisibleLineBounds(0, 1, 80)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right with sidescroll 2`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(2))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("80|" + "l"))
    assertVisibleLineBounds(0, 2, 81)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right with sidescrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("70|" + "l"))
    assertVisibleLineBounds(0, 30, 109)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right with sidescroll and sidescrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(1))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("70|" + "l"))
    assertVisibleLineBounds(0, 1, 80)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right with large sidescrolloff keeps cursor centred`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(999))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("50|" + "l"))
    assertVisibleLineBounds(0, 10, 89)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving right with inline inlay`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(1))
    configureByColumns(200)
    val inlay = addInlay(110, true, 5)
    typeText(injector.parser.parseKeys("100|" + "20l"))
    // These columns are hard to calculate, because the visible offset depends on the rendered width of the inlay
    // Also, because we're scrolling right (adding columns to the right) we make the right most column line up
    val textWidth = myFixture.editor.scrollingModel.visibleArea.width - inlay.widthInPixels
    val availableColumns = (textWidth / com.flop.idea.fim.helper.EditorHelper.getPlainSpaceWidthFloat(myFixture.editor)).roundToInt()
    assertVisibleLineBounds(0, 119 - availableColumns + 1, 119)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left scrolls half screen to left by default`() {
    configureByColumns(200)
    typeText(injector.parser.parseKeys("80|zs" + "h"))
    assertPosition(0, 78)
    assertVisibleLineBounds(0, 38, 117)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left scrolls half screen to left by default 2`() {
    configureByColumns(200)
    setEditorVisibleSize(100, screenHeight)
    typeText(injector.parser.parseKeys("100|zs" + "h"))
    assertVisibleLineBounds(0, 48, 147)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left scrolls half screen if moving too far 1`() {
    configureByColumns(400)
    typeText(injector.parser.parseKeys("170|zs" + "41h")) // Move more than half screen width, but scroll less
    assertVisibleLineBounds(0, 88, 167)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left scrolls half screen if moving too far 2`() {
    configureByColumns(400)
    typeText(injector.parser.parseKeys("290|zs" + "200h")) // Move more than half screen width, but scroll less
    assertVisibleLineBounds(0, 49, 128)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left with sidescroll 1`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(1))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("100|zs" + "h"))
    assertVisibleLineBounds(0, 98, 177)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left with sidescroll 2`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(2))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("100|zs" + "h"))
    assertVisibleLineBounds(0, 97, 176)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left with sidescrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("120|zs" + "h"))
    assertVisibleLineBounds(0, 78, 157)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left with sidescroll and sidescrolloff`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(1))
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("120|zs" + "h"))
    assertVisibleLineBounds(0, 108, 187)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left with inline inlay`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrollName, FimInt(1))
    configureByColumns(200)
    val inlay = addInlay(110, true, 5)
    typeText(injector.parser.parseKeys("120|zs" + "20h"))
    // These columns are hard to calculate, because the visible offset depends on the rendered width of the inlay
    val textWidth = myFixture.editor.scrollingModel.visibleArea.width - inlay.widthInPixels
    val availableColumns = (textWidth / com.flop.idea.fim.helper.EditorHelper.getPlainSpaceWidthFloat(myFixture.editor)).roundToInt()
    assertVisibleLineBounds(0, 99, 99 + availableColumns - 1)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SCROLL)
  fun `test moving left with large sidescrolloff keeps cursor centred`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(999))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("50|" + "h"))
    assertVisibleLineBounds(0, 8, 87)
  }
}
