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

package org.jetbrains.plugins.ideafim.action.change.change

import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.FimTestCase

class ChangeLineActionTest : FimTestCase() {
  fun `test on empty file`() {
    setupChecks {
      this.neoFim.ignoredRegisters = setOf('1', '"')
    }
    doTest("cc", "", "", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test on empty file with S`() {
    setupChecks {
      this.neoFim.ignoredRegisters = setOf('1', '"')
    }
    doTest("S", "", "", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test on last line with S`() {
    doTest(
      "S",
      """
            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
      """.trimIndent(),
      """
            I found it in a legendary land
            $c
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test on last line with new line with S`() {
    doTest(
      "S",
      """
            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
            
      """.trimIndent(),
      """
            I found it in a legendary land
            $c
            
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test on very last line with new line with S`() {
    doTest(
      "S",
      """
            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
      """.trimIndent(),
      """
            I found it in a legendary land
            $c
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test on very last line with new line with S2`() {
    doTest(
      "S",
      """
            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
            
      """.trimIndent(),
      """
            I found it in a legendary land
            $c
            
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test on first line with new line with S`() {
    doTest(
      "S",
      """
            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent(),
      """
            $c
            all rocks and lavender and tufted grass,
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test on last line with new line with cc`() {
    doTest(
      "cc",
      """
            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
            
      """.trimIndent(),
      """
            I found it in a legendary land
            $c
            
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test on last line`() {
    doTest(
      "cc",
      """
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            $c
      """.trimIndent(),
      """
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            $c
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test S with count`() {
    doTest(
      "3S",
      """
            ${c}I found it in a legendary land
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
      """.trimIndent(),
      """
            $c
            hard by the torrent of a mountain pass.

            The features it combines mark it as new
            to science: shape and shade -- the special tinge,
            akin to moonlight, tempering its blue,
            the dingy underside, the checquered fringe.

            My needles have teased out its sculpted sex;
            corroded tissues could no longer hide
            that priceless mote now dimpling the convex
            and limpid teardrop on a lighted slide.
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }
}
