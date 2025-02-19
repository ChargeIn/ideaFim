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

package org.jetbrains.plugins.ideafim.action.change.insert

import com.intellij.codeInsight.daemon.impl.HintRenderer
import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class VisualBlockInsertActionTest : FimTestCase() {

  // VIM-1110 |CTRL-V| |v_b_i| |zc|
  @TestWithoutNeofim(SkipNeofimReason.FOLDING)
  fun `test block insert after folds`() {
    configureByJavaText(
      """$c/**
 * Something to fold.
 */
foo
bar
"""
    )

    myFixture.editor.foldingModel.runBatchFoldingOperation {
      CodeFoldingManager.getInstance(myFixture.project).updateFoldRegions(myFixture.editor)
      FoldingUtil.findFoldRegionStartingAtLine(myFixture.editor, 0)!!.isExpanded = false
    }

    typeText(injector.parser.parseKeys("j" + "<C-V>" + "j" + "I" + "X" + "<Esc>"))
    assertState(
      """/**
 * Something to fold.
 */
${c}Xfoo
Xbar
"""
    )
  }

  // VIM-1379 |CTRL-V| |j| |v_b_I|
  @TestWithoutNeofim(SkipNeofimReason.VISUAL_BLOCK_MODE)
  fun `test insert visual block with empty line in the middle`() {
    doTest(
      listOf("ll", "<C-V>", "jjI", "_quux_", "<Esc>"),
      """
                    foo

                    bar

      """.trimIndent(),
      """
                    fo_quux_o

                    ba_quux_r

      """.trimIndent()
    )
  }

  // VIM-632 |CTRL-V| |v_b_I|
  @TestWithoutNeofim(SkipNeofimReason.VISUAL_BLOCK_MODE)
  fun `test change visual block`() {
    doTest(
      listOf("<C-V>", "j", "I", "quux ", "<Esc>"),
      """
                    foo bar
                    ${c}baz quux
                    spam eggs

      """.trimIndent(),
      (
        """
                    foo bar
                    ${c}quux baz quux
                    quux spam eggs

        """.trimIndent()
        )
    )
  }

  fun `test visual block insert`() {
    val before = """
            ${c}int a;
            int b;
            int c;
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("<C-V>" + "2j" + "I" + "const " + "<Esc>"), before)
    val after = """
            ${c}const int a;
            const int b;
            const int c;
    """.trimIndent()
    assertState(after)
  }

  // VIM-1379 |CTRL-V| |j| |v_b_I|
  @TestWithoutNeofim(SkipNeofimReason.VISUAL_BLOCK_MODE)
  fun `test insert visual block with shorter line in the middle`() {
    doTest(
      listOf("ll", "<C-V>", "jjI", "_quux_", "<Esc>"),
      """
                    foo
                    x
                    bar

      """.trimIndent(),
      (
        """
                    fo_quux_o
                    x
                    ba_quux_r

        """.trimIndent()
        )
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.VISUAL_BLOCK_MODE)
  fun `test insert in non block mode`() {
    doTest(
      listOf("vwIHello<esc>"),
      """
                ${c}A Discovery

                ${c}I found it in a legendary land
                all rocks and ${c}lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                Hell${c}oA Discovery

                Hell${c}oI found it in a legendary land
                Hell${c}oall rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.VISUAL_BLOCK_MODE)
  fun `test block mode with inlays`() {
    val before = """
                A Discovery

                I found it in ${c}a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
                    """
    doTest(
      listOf("<C-V>" + "jjI" + " Hello " + "<ESC>"),
      before.trimIndent(),
      """
                A Discovery

                I found it in  Hello a legendary land
                all rocks and  Hello lavender and tufted grass,
                where it was s Hello ettled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
    ) {
      it.inlayModel.addInlineElement(before.indexOf("found"), HintRenderer("Hello"))
      it.inlayModel.addInlineElement(before.indexOf("l rocks"), HintRenderer("Hello"))
      it.inlayModel.addInlineElement(before.indexOf("ere it"), HintRenderer("Hello"))
    }
  }

  @TestWithoutNeofim(SkipNeofimReason.VISUAL_BLOCK_MODE)
  fun `test insert with block on one line`() {
    val before = """
                A Discovery

                I found it in ${c}a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
                    """
    doTest(
      listOf("<C-V>", "lll", "I"),
      before.trimIndent(),
      before.trimIndent(),
      FimStateMachine.Mode.INSERT,
      FimStateMachine.SubMode.NONE
    )
  }
}
