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

package org.jetbrains.plugins.ideafim.action.motion.select

import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class SelectEnableBlockModeActionHandlerTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.SELECT_MODE)
  fun `test entering select mode`() {
    doTest(
      listOf("g<C-H>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I$c$se found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.SELECT_MODE)
  fun `test entering select mode at the end of file`() {
    doTest(
      "g<C-H>",
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass$c.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass$s.$c$se
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }

  fun `test entering select mode on empty line`() {
    doTest(
      "g<C-H>",
      """
                A Discovery
                $c
                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery
                $s$c$se
                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.SELECT_MODE)
  fun `test entering select mode multicaret`() {
    doTest(
      listOf("g<C-H>"),
      """
                A Discovery
                $c
                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was ${c}settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was ${s}s$c${se}ettled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }
}
