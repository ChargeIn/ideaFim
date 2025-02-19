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

package org.jetbrains.plugins.ideafim.helper

import com.flop.idea.fim.helper.EditorHelper
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.junit.Assert
import kotlin.math.roundToInt

class EditorHelperTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test scroll column to left of screen`() {
    configureByColumns(100)
    com.flop.idea.fim.helper.EditorHelper.scrollColumnToLeftOfScreen(myFixture.editor, 0, 2)
    val visibleArea = myFixture.editor.scrollingModel.visibleArea
    val columnWidth = com.flop.idea.fim.helper.EditorHelper.getPlainSpaceWidthFloat(myFixture.editor)
    Assert.assertEquals((2 * columnWidth).roundToInt(), visibleArea.x)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test scroll column to right of screen`() {
    configureByColumns(100)
    val column = screenWidth + 2
    com.flop.idea.fim.helper.EditorHelper.scrollColumnToRightOfScreen(myFixture.editor, 0, column)
    val visibleArea = myFixture.editor.scrollingModel.visibleArea
    val columnWidth = com.flop.idea.fim.helper.EditorHelper.getPlainSpaceWidthFloat(myFixture.editor)
    Assert.assertEquals(((column - screenWidth + 1) * columnWidth).roundToInt(), visibleArea.x)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test scroll column to middle of screen with even number of columns`() {
    configureByColumns(200)
    // For an 80 column screen, moving a column to the centre should position it in column 41 (1 based) - 40 columns on
    // the left, mid point, 39 columns on the right
    // Put column 100 into position 41 -> offset is 59 columns
    com.flop.idea.fim.helper.EditorHelper.scrollColumnToMiddleOfScreen(myFixture.editor, 0, 99)
    val visibleArea = myFixture.editor.scrollingModel.visibleArea
    val columnWidth = com.flop.idea.fim.helper.EditorHelper.getPlainSpaceWidthFloat(myFixture.editor)
    Assert.assertEquals((59 * columnWidth).roundToInt(), visibleArea.x)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test scroll column to middle of screen with odd number of columns`() {
    configureByColumns(200)
    setEditorVisibleSize(81, 25)
    // For an 81 column screen, moving a column to the centre should position it in column 41 (1 based) - 40 columns on
    // the left, mid point, 40 columns on the right
    // Put column 100 into position 41 -> offset is 59 columns
    com.flop.idea.fim.helper.EditorHelper.scrollColumnToMiddleOfScreen(myFixture.editor, 0, 99)
    val visibleArea = myFixture.editor.scrollingModel.visibleArea
    val columnWidth = com.flop.idea.fim.helper.EditorHelper.getPlainSpaceWidthFloat(myFixture.editor)
    Assert.assertEquals((59 * columnWidth).roundToInt(), visibleArea.x)
  }
}
