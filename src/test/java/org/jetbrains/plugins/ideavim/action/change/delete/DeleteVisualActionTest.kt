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

package org.jetbrains.plugins.ideafim.action.change.delete

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.group.visual.IdeaSelectionControl
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.jetbrains.plugins.ideafim.waitAndAssertMode

/**
 * @author Alex Plate
 */
class DeleteVisualActionTest : FimTestCase() {
  fun `test delete block SE direction`() {
    val keys = listOf("<C-V>e2j", "d")
    val before = """
            A Discovery

            I |${c}found| it in a legendary land
            al|l roc|ks and lavender and tufted grass,
            wh|ere i|t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I |$c| it in a legendary land
            al||ks and lavender and tufted grass,
            wh||t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test delete block SW direction`() {
    val keys = listOf("<C-V>b2j", "d")
    val before = """
            A Discovery

            I |foun${c}d| it in a legendary land
            al|l roc|ks and lavender and tufted grass,
            wh|ere i|t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I |$c| it in a legendary land
            al||ks and lavender and tufted grass,
            wh||t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test delete block NW direction`() {
    val keys = listOf("<C-V>b2k", "d")
    val before = """
            A Discovery

            I |found| it in a legendary land
            al|l roc|ks and lavender and tufted grass,
            wh|ere ${c}i|t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I |$c| it in a legendary land
            al||ks and lavender and tufted grass,
            wh||t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test delete block NE direction`() {
    val keys = listOf("<C-V>2e2k", "d")
    val before = """
            A Discovery

            I |found| it in a legendary land
            al|l roc|ks and lavender and tufted grass,
            wh|${c}ere i|t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I |$c| it in a legendary land
            al||ks and lavender and tufted grass,
            wh||t was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test delete after extend selection`() {
    // This test emulates deletion after structural selection
    // In short, when caret is not on the selection end
    configureByText(
      """
            A Discovery

            ${s}I found it in a legendary land
            all rocks ${c}and lavender and tufted grass,
            where it was settled on some sodden sand
            ${se}hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    typeText(injector.parser.parseKeys("d"))
    assertState(
      """
            A Discovery

            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertState(FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test delete with dollar motion`() {
    val keys = listOf("<C-V>3j$", "d")
    val before = """
            A Discovery

            I |${c}found it in a legendary land
            al|l rocks and lavender and tufted grass,[ additional symbols]
            wh|ere it was settled on some sodden sand
            ha|rd by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I |
            al|
            wh|
            ha|
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }
}
