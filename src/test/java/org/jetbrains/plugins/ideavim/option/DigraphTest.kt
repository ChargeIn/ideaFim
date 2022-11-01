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

package org.jetbrains.plugins.ideafim.option

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */
// TODO: 2019-06-18 FimOptionsTestCase
class DigraphTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR, "backspace works strange")
  fun `test digraph`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.digraphName)

    doTest(
      "i B<BS>B",
      """
            A Discovery

            I found it$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
            A Discovery

            I found it ¦$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR, "backspace works strange")
  fun `test digraph stops`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.digraphName)

    doTest(
      "i B<BS>BHello",
      """
            A Discovery

            I found it$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
            A Discovery

            I found it ¦Hello$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR, "backspace works strange")
  fun `test digraph double backspace`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.digraphName)

    doTest(
      "i B<BS><BS>B",
      """
            A Discovery

            I found it$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
            A Discovery

            I found itB$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR, "backspace works strange")
  fun `test digraph backspace digraph`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.digraphName)

    doTest(
      "i B<BS>B<BS>B",
      """
            A Discovery

            I found it$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
            A Discovery

            I found it B$c in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }
}
