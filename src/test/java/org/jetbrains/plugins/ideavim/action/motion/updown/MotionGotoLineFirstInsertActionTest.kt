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

package org.jetbrains.plugins.ideafim.action.motion.updown

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class MotionGotoLineFirstInsertActionTest : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test simple motion`() {
    doTest(
      listOf("i", "<C-Home>", "<Esc>"),
      """
        |       A Discovery
        |
        |       I found it in a legendary land
        |       all ${c}rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard by the torrent of a mountain pass.
      """.trimMargin(),
      """
        |$c       A Discovery
        |
        |       I found it in a legendary land
        |       all rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard by the torrent of a mountain pass.
      """.trimMargin(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test simple motion ignores nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    doTest(
      listOf("i", "<C-Home>", "<Esc>"),
      """
        |       A Discovery
        |
        |       I found it in a legendary land
        |       all ${c}rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard by the torrent of a mountain pass.
      """.trimMargin(),
      """
        |$c       A Discovery
        |
        |       I found it in a legendary land
        |       all rocks and lavender and tufted grass,
        |       where it was settled on some sodden sand
        |       hard by the torrent of a mountain pass.
      """.trimMargin(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
