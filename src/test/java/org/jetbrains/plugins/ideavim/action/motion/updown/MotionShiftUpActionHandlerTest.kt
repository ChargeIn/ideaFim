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

package org.jetbrains.plugins.ideafim.action.motion.updown

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

class MotionShiftUpActionHandlerTest : FimOptionTestCase(OptionConstants.selectmodeName, OptionConstants.keymodelName) {
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test visual up`() {
    doTest(
      listOf("<S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                al${c}l rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}${c}found it in a legendary land
                all${se} rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test visual up twice`() {
    doTest(
      listOf("<S-Up><S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                wh${c}ere it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                whe${se}re it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test save column`() {
    doTest(
      listOf("<S-Up><S-Up><S-Up>"),
      """
                A Discovery

                I found it in a legendary land[additional chars]
                all rocks and lavender and tufted grass,[additional chars]
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.[additio${c}nal chars]
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary land[additional chars${s}${c}]
                all rocks and lavender and tufted grass,[additional chars]
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.[addition${se}al chars]
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, OptionConstants.selectmode_key)
  )
  fun `test select up`() {
    doTest(
      listOf("<S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                al${c}l rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}${c}found it in a legendary land
                al${se}l rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, OptionConstants.selectmode_key)
  )
  fun `test select up twice`() {
    doTest(
      listOf("<S-Up><S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                wh${c}ere it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                wh${se}ere it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test char mode simple motion`() {
    doTest(
      listOf("gh", "<S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                ${c}all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I$s$c found it in a legendary land
                ${se}all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test char mode to empty line`() {
    doTest(
      listOf("gh", "<S-Up>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery
                $s$c
                ${se}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test char mode from empty line`() {
    doTest(
      listOf("gh", "<S-Up>"),
      """
                A Discovery
                $c
                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                $s${c}A Discovery
                $se
                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test char mode on file start`() {
    doTest(
      listOf("gh", "<S-Up>"),
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A ${s}D$c${se}iscovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test char mode multicaret`() {
    doTest(
      listOf("gh", "<S-Up>"),
      """
                A ${c}Discovery

                I found ${c}it in a legendary land
                all rocks and lavender and tufted grass,
                where it was ${c}settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A ${s}D$c${se}iscovery
                $s$c
                I found ${se}it in a legendary land
                all rocks and $s${c}lavender and tufted grass,
                where it was ${se}settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test line mode simple motion`() {
    doTest(
      listOf("gH", "<S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                ${c}all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                $s${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                ${se}where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_LINE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test line mode to empty line`() {
    doTest(
      listOf("gH", "<S-Up>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery
                $s$c
                I found it in a legendary land
                ${se}all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_LINE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test line mode from empty line`() {
    doTest(
      listOf("gH", "<S-Up>"),
      """
                A Discovery
                $c
                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                $s${c}A Discovery

                ${se}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_LINE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test line mode to line start`() {
    doTest(
      listOf("gH", "<S-Up>"),
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                ${s}A ${c}Discovery$se

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_LINE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test line mode multicaret`() {
    doTest(
      listOf("gH", "<S-Up>"),
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks ${c}and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                ${s}A ${c}Discovery$se

                ${s}I found it$c in a legendary land
                all rocks and lavender and tufted grass,
                ${se}where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_LINE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test block mode simple motion`() {
    doTest(
      listOf("g<C-H>", "<S-Up>"),
      """
                A Discovery

                I found it in a legendary land
                ${c}all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I$c$se found it in a legendary land
                ${s}a$c${se}ll rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test block mode to empty line`() {
    doTest(
      listOf("g<C-H>", "<S-Up>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery
                $s$c$se
                ${s}$c${se}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test block mode from empty line`() {
    doTest(
      listOf("g<C-H>", "<S-Up>"),
      """
                A Discovery
                $c
                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                $s$c${se}A Discovery
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

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test block mode to line start`() {
    doTest(
      listOf("g<C-H>", "<S-Up>"),
      """
                A ${c}Discovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A ${s}D$c${se}iscovery

                I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }
}
