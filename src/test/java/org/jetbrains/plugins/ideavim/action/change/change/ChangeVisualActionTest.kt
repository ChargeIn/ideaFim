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

@file:Suppress("RemoveCurlyBracesFromTemplate")

package org.jetbrains.plugins.ideafim.action.change.change

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.FimTestCase

class ChangeVisualActionTest : FimTestCase() {
  fun `test multiple line change`() {
    val keys = "VjcHello<esc>"
    val before = """
            ${c}A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            Hello
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test multiple line change in text middle`() {
    val keys = "Vjc"
    val before = """
            A Discovery

            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            
            ${c}
  """
  )
  fun `test multiple line change till the end`() {
    val keys = "Vjc"
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            
            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            ${c}
            
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test multiple line change till the end with two new lines`() {
    val keys = "Vjc"
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            
            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
            
            
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
           
            ${c}
            
            
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(description = "Wrong caret position")
  fun `test change with dollar motion`() {
    val keys = listOf("<C-V>3j$", "c", "Hello<Esc>")
    val before = """
            A Discovery

            I |${c}found it in a legendary land
            al|l rocks and lavender and tufted grass,[ additional symbols]
            wh|ere it was settled on some sodden sand
            ha|rd by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I |Hello
            al|Hello
            wh|Hello
            ha|Hello
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test replace first line`() {
    val keys = "VcHello<esc>"
    val before = "${c}A Discovery"
    val after = "Hello"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test change visual action`() {
    typeTextInFile(
      injector.parser.parseKeys("v2lc" + "aaa" + "<ESC>"),
      "abcd${c}ffffff${c}abcde${c}aaaa\n"
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    assertState("abcdaa${c}afffaa${c}adeaa${c}aa\n")
  }

  // VIM-1379 |CTRL-V| |j| |v_b_c|
  @FimBehaviorDiffers(description = "Different caret position")
  fun `test change visual block with empty line in the middle`() {
    doTest(
      listOf("ll", "<C-V>", "ljjc", "_quux_", "<Esc>"),
      """
        foo foo
        
        bar bar
        
      """.trimIndent(),
      """
        fo_quux_foo
        
        ba_quux_bar
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-1379 |CTRL-V| |j| |v_b_c|
  @FimBehaviorDiffers(description = "Different caret position")
  fun `test change visual block with shorter line in the middle`() {
    doTest(
      listOf("ll", "<C-V>", "ljjc", "_quux_", "<Esc>"),
      """
        foo foo
        x
        bar bar
        
      """.trimIndent(),
      """
        fo_quux_foo
        x
        ba_quux_bar
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }
}
