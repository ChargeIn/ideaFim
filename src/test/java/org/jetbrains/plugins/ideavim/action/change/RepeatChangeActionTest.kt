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

package org.jetbrains.plugins.ideafim.action.change

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class RepeatChangeActionTest : FimTestCase() {
  fun `test simple repeat`() {
    val keys = listOf("v2erXj^", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXX in a legendary land
                ${c}XXXXXXXXXXand lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test simple repeat with dollar motion`() {
    val keys = listOf("v\$rXj^", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ${c}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test repeat to line end`() {
    val keys = listOf("v2erXj\$b", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXX in a legendary land
                all rocks and lavender and tufted ${c}XXXXXX
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(description = "Different caret position")
  fun `test repeat multiline`() {
    val keys = listOf("vjlrXj", ".")
    val before = """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                I XXXXXXXXXXXXXXXXXXXXXXXXXXXX
                XXXXrocks and lavender and tufted grass,
                whe${c}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                XXXX by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test count doesn't affect repeat`() {
    val keys = listOf("v2erXj^", "10.")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXX in a legendary land
                ${c}XXXXXXXXXXand lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test multicaret`() {
    val keys = listOf("v2erXj^", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where ${c}it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXX in a legendary land
                ${c}XXXXXXXXXXand lavender and tufted grass,
                where XXXXXX settled on some sodden sand
                ${c}XXXXXXy the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test line motion`() {
    val keys = listOf("VrXj^", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ${c}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(description = "Wrong caret position")
  fun `test line motion to end`() {
    val keys = listOf("VjrX2j^", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                where it was settled on some sodden sand
                ${c}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(description = "Wrong caret position")
  fun `test line motion shift`() {
    val keys = listOf("V3j<", ".")
    val before = """
                |A Discovery
                |
                |        ${c}I found it in a legendary land
                |        all rocks and lavender and tufted grass,
                |        where it was settled on some sodden sand
                |        hard by the torrent of a mountain pass.
                """.trimMargin()
    val after = """
                |A Discovery
                |
                |${c}I found it in a legendary land
                |all rocks and lavender and tufted grass,
                |where it was settled on some sodden sand
                |hard by the torrent of a mountain pass.
                """.trimMargin()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(description = "Wrong caret position")
  fun `test block motion`() {
    val keys = listOf("<C-V>jerXll", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXound it in a legendary land
                XXX ${c}XXXks and lavender and tufted grass,
                wherXXXt was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(
    """
                A Discovery

                XXXXXnd it in a legendary land
                XXXXXocks and lavender and tufted grass,
                XXXXX it was settled on some sodden sand
                hard ${c}XXXXXe torrent of a mountain pass.

    """
  )
  fun `test block motion to end`() {
    val keys = listOf("<C-V>jjerXjl", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.

    """.trimIndent()
    val after = """
                A Discovery

                XXXXXnd it in a legendary land
                XXXXXocks and lavender and tufted grass,
                XXXXX it was settled on some sodden sand
                XXXXX${c}Xy the torrent of a mountain pass.

    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR)
  fun `test block with dollar motion`() {
    val keys = listOf("<C-V>j\$rXj^", ".")
    val before = """
                A Discovery

                ${c}I found it in a legendary land[additional characters]
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand[additional characters]
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                ${c}XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
                XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun `test repeat with count`() {
    val keys = listOf("4x", "j", ".")
    val before = """
              A Discovery
  
              ${c}I found it in a legendary land
              all rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
              A Discovery
  
              und it in a legendary land
              ${c}rocks and lavender and tufted grass,
              where it was settled on some sodden sand
              hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
    
        Three
        Two
        One
  """
  )
  fun `test redo register feature`() {
    doTest(
      listOf("dd", "dd", "dd", "\"1p", ".", "."),
      """
        One
        Two
        Three
      """.trimIndent(),
      """
        Three
        Two
        One
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
