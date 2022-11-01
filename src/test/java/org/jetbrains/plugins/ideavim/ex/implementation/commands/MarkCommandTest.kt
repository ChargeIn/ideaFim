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

package org.jetbrains.plugins.ideafim.ex.implementation.commands

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.newapi.fim
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */
class MarkCommandTest : FimTestCase() {
  fun `test simple mark`() {
    configureByText(
      """I found it in a legendary land
                         |all rocks and lavender and tufted grass,
                         |where it$c was settled on some sodden sand
                         |hard by the torrent of a mountain pass.
                       """.trimMargin()
    )
    typeText(commandToKeys("mark a"))
    com.flop.idea.fim.FimPlugin.getMark().getMark(myFixture.editor.fim, 'a')?.let {
      assertEquals(2, it.logicalLine)
      assertEquals(0, it.col)
    } ?: TestCase.fail("Mark is null")
  }

  fun `test global mark`() {
    configureByText(
      """I found it in a legendary land
                         |all rocks and lavender and tufted grass,
                         |where it$c was settled on some sodden sand
                         |hard by the torrent of a mountain pass.
                       """.trimMargin()
    )
    typeText(commandToKeys("mark G"))
    com.flop.idea.fim.FimPlugin.getMark().getMark(myFixture.editor.fim, 'G')?.let {
      assertEquals(2, it.logicalLine)
      assertEquals(0, it.col)
    } ?: TestCase.fail("Mark is null")
  }

  fun `test k mark`() {
    configureByText(
      """I found it in a legendary land
                         |all rocks and lavender and tufted grass,
                         |where it$c was settled on some sodden sand
                         |hard by the torrent of a mountain pass.
                       """.trimMargin()
    )
    typeText(commandToKeys("k a"))
    com.flop.idea.fim.FimPlugin.getMark().getMark(myFixture.editor.fim, 'a')?.let {
      assertEquals(2, it.logicalLine)
      assertEquals(0, it.col)
    } ?: TestCase.fail("Mark is null")
  }

  fun `test mark in range`() {
    configureByText(
      """I found it in a legendary land
                         |all rocks and lavender and tufted grass,
                         |where it$c was settled on some sodden sand
                         |hard by the torrent of a mountain pass.
                       """.trimMargin()
    )
    typeText(commandToKeys("1,2 mark a"))
    com.flop.idea.fim.FimPlugin.getMark().getMark(myFixture.editor.fim, 'a')?.let {
      assertEquals(1, it.logicalLine)
      assertEquals(0, it.col)
    } ?: TestCase.fail("Mark is null")
  }
}
