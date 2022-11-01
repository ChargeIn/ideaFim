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

import com.intellij.openapi.editor.LogicalPosition
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.mark.Mark
import com.flop.idea.fim.newapi.fim
import org.jetbrains.plugins.ideafim.FimTestCase
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * @author Jørgen Granseth
 */
class DeleteMarksCommandTest : FimTestCase() {
  private fun setUpMarks(marks: String) {
    configureByText(
      """I found it in a legendary land
         all rocks and lavender and tufted grass,
         where it was settled on some sodden sand
         hard by the torrent of a mountain pass.

         The features it combines mark it as new
         to science: shape and shade -- the special tinge,
         akin to moonlight, tempering its blue,
         the dingy underside, the checquered fringe.

         My needles have teased out its sculpted sex;
         corroded tissues could no longer hide
         that priceless mote now dimpling the convex
         and limpid teardrop on a lighted slide.
         """.trimMargin()
    )

    marks.forEachIndexed { index, c ->
      com.flop.idea.fim.FimPlugin.getMark()
        .setMark(myFixture.editor.fim, c, myFixture.editor.logicalPositionToOffset(LogicalPosition(index, 0)))
    }
  }

  private fun getMark(ch: Char): Mark? {
    return com.flop.idea.fim.FimPlugin.getMark().getMark(myFixture.editor.fim, ch)
  }

  fun `test delete single mark`() {
    setUpMarks("a")
    typeText(commandToKeys("delmarks a"))

    assertNull(getMark('a'), "Mark was not deleted")
  }

  fun `test delete multiple marks`() {
    setUpMarks("abAB")
    typeText(commandToKeys("delmarks Ab"))

    arrayOf('A', 'b')
      .forEach { ch -> assertNull(getMark(ch), "Mark $ch was not deleted") }

    arrayOf('a', 'B')
      .forEach { ch -> assertNotNull(getMark(ch), "Mark $ch was unexpectedly deleted") }
  }

  fun `test delete ranges (inclusive)`() {
    setUpMarks("abcde")
    typeText(commandToKeys("delmarks b-d"))

    arrayOf('b', 'c', 'd')
      .forEach { ch -> assertNull(getMark(ch), "Mark $ch was not deleted") }

    arrayOf('a', 'e')
      .forEach { ch -> assertNotNull(getMark(ch), "Mark $ch was unexpectedly deleted") }
  }

  fun `test delete multiple ranges and marks with whitespace`() {
    setUpMarks("abcdeABCDE")
    typeText(commandToKeys("delmarks b-dC-E a"))

    arrayOf('a', 'b', 'c', 'd', 'C', 'D', 'E', 'a')
      .forEach { ch -> assertNull(getMark(ch), "Mark $ch was not deleted") }

    arrayOf('e', 'A', 'B')
      .forEach { ch -> assertNotNull(getMark(ch), "Mark $ch was unexpectedly deleted") }
  }

  fun `test invalid range throws exception without deleting any marks`() {
    setUpMarks("a")
    typeText(commandToKeys("delmarks a-C"))
    assertPluginError(true)

    assertNotNull(getMark('a'), "Mark was deleted despite invalid command given")
  }

  fun `test invalid characters throws exception`() {
    setUpMarks("a")
    typeText(commandToKeys("delmarks bca# foo"))
    assertPluginError(true)

    assertNotNull(getMark('a'), "Mark was deleted despite invalid command given")
  }

  fun `test delmarks! with trailing spaces`() {
    setUpMarks("aBcAbC")
    typeText(commandToKeys("delmarks!"))

    arrayOf('a', 'b', 'c')
      .forEach { ch -> assertNull(getMark(ch), "Mark $ch was not deleted") }

    arrayOf('A', 'B', 'C')
      .forEach { ch -> assertNotNull(getMark(ch), "Global mark $ch was deleted by delmarks!") }
  }

  fun `test delmarks! with other arguments fails`() {
    setUpMarks("aBcAbC")
    typeText(commandToKeys("delmarks!a"))

    assertPluginError(true)
    arrayOf('a', 'b', 'c', 'A', 'B', 'C')
      .forEach { ch -> assertNotNull(getMark(ch), "Mark $ch was deleted despite invalid command given") }
  }

  fun `test trailing spaces ignored`() {
    setUpMarks("aBcAbC")
    typeText(commandToKeys("delmarks!   "))

    arrayOf('a', 'b', 'c')
      .forEach { ch -> assertNull(getMark(ch), "Mark $ch was not deleted") }

    arrayOf('A', 'B', 'C')
      .forEach { ch -> assertNotNull(getMark(ch), "Global mark $ch was deleted by delmarks!") }
  }

  fun `test alias (delm)`() {
    setUpMarks("a")
    typeText(commandToKeys("delm a"))

    assertNull(getMark('a'), "Mark was not deleted")

    setUpMarks("aBcAbC")
    typeText(commandToKeys("delm!"))

    arrayOf('a', 'b', 'c')
      .forEach { ch -> assertNull(getMark(ch), "Mark $ch was not deleted") }

    arrayOf('A', 'B', 'C')
      .forEach { ch -> assertNotNull(getMark(ch), "Global mark $ch was deleted by delm!") }
  }
}
