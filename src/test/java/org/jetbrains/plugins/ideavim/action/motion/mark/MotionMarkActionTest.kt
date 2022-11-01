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

package org.jetbrains.plugins.ideafim.action.motion.mark

import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.ide.bookmark.LineBookmark
import com.intellij.testFramework.PlatformTestUtil
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.group.createLineBookmark
import com.flop.idea.fim.group.mnemonic
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.fimscript.services.IjFimOptionService
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

class MotionMarkActionTest : FimOptionTestCase(IjFimOptionService.ideamarksName) {
  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideamarksName, OptionValueType.NUMBER, "1"))
  fun `test simple add mark`() {
    val keys = injector.parser.parseKeys("mA")
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    typeText(keys)
    checkMarks('A' to 2)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideamarksName, OptionValueType.NUMBER, "1"))
  fun `test simple add multiple marks`() {
    val keys = injector.parser.parseKeys("mAj" + "mBj" + "mC")
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    typeText(keys)
    checkMarks('A' to 2, 'B' to 3, 'C' to 4)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideamarksName, OptionValueType.NUMBER, "1"))
  fun `test simple add multiple marks on same line`() {
    val keys = injector.parser.parseKeys("mA" + "mB" + "mC")
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    typeText(keys)
    checkMarks('A' to 2)

    // Previously it was like this, but now it's impossible to set multiple bookmarks on the same line.
//    checkMarks('A' to 2, 'B' to 2, 'C' to 2)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideamarksName, OptionValueType.NUMBER, "1"))
  fun `test move to another line`() {
    val keys = injector.parser.parseKeys("mAjj" + "mA")
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    typeText(keys)
    checkMarks('A' to 4)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideamarksName, OptionValueType.NUMBER, "1"))
  fun `test simple system mark`() {
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    myFixture.project.createLineBookmark(myFixture.editor, 2, 'A')
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    val fimMarks = com.flop.idea.fim.FimPlugin.getMark().getMarks(myFixture.editor.fim)
    TestCase.assertEquals(1, fimMarks.size)
    TestCase.assertEquals('A', fimMarks[0].key)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.ideamarksName, OptionValueType.NUMBER, "1"))
  fun `test system mark move to another line`() {
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)

    val bookmark = myFixture.project.createLineBookmark(myFixture.editor, 2, 'A')

    BookmarksManager.getInstance(myFixture.project)?.remove(bookmark!!)
    myFixture.project.createLineBookmark(myFixture.editor, 4, 'A')
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    val fimMarks = com.flop.idea.fim.FimPlugin.getMark().getMarks(myFixture.editor.fim)
    TestCase.assertEquals(1, fimMarks.size)
    TestCase.assertEquals('A', fimMarks[0].key)
    TestCase.assertEquals(4, fimMarks[0].logicalLine)
  }

  private fun checkMarks(vararg marks: Pair<Char, Int>) {
    val project = myFixture.project
    val validBookmarks = BookmarksManager.getInstance(project)!!.bookmarks.sortedBy { it.mnemonic(project) }
    assertEquals(marks.size, validBookmarks.size)
    marks.sortedBy { it.first }.forEachIndexed { index, (mn, line) ->
      assertEquals(mn, validBookmarks[index].mnemonic(project))
      assertEquals(line, (validBookmarks[index] as LineBookmark).line)
    }
  }
}
