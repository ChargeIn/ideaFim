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

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.FimTestCase

class ChangeVisualLinesEndActionTest : FimTestCase() {
  fun `test change last line`() {
    val keys = "VC"
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            ${c}hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            ${c}
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test last empty line`() {
    val keys = "vC"
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
            ${c}
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
            ${c}
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            ${c}
  """
  )
  fun `test change last two lines`() {
    val keys = "vjC"
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass${c}.
            
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            ${c}
            
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }
}
