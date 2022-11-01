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

package org.jetbrains.plugins.ideafim.action.motion.leftright

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

class MotionShiftRightActionHandlerTest : FimOptionTestCase(OptionConstants.keymodelName, OptionConstants.selectmodeName) {
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test visual right`() {
    doTest(
      listOf("<S-Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}f${c}o${se}und it in a legendary land
                all rocks and lavender and tufted grass,
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
  fun `test visual right twice`() {
    doTest(
      listOf("<S-Right><S-Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}fo${c}u${se}nd it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, OptionConstants.selectmode_key)
  )
  fun `test select right`() {
    doTest(
      listOf("<S-Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}f${c}${se}ound it in a legendary land
                all rocks and lavender and tufted grass,
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
  fun `test select right twice`() {
    doTest(
      listOf("<S-Right><S-Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}fo${c}${se}und it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
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
  fun `test simple motion char mode`() {
    doTest(
      listOf("gh", "<S-Right>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I $c${se}found it in a legendary land
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
  fun `test at the lineend char mode`() {
    doTest(
      listOf("gh", "<S-Right>"),
      """
                A Discovery

                I found it in a legendary la${c}nd
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary la${s}nd$c$se
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
  fun `test out of line char mode`() {
    doTest(
      listOf("gh", "<S-Right>".repeat(2)),
      """
                A Discovery

                I found it in a legendary lan${c}d
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary lan${s}d$c$se
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
  fun `test file end char mode`() {
    doTest(
      listOf("gh", "<S-Right>".repeat(2)),
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
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test file char mode multicaret`() {
    doTest(
      listOf("gh", "<S-Right>".repeat(2)),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass$c.
      """.trimIndent(),
      """
                A Discovery

                I ${s}fou$c${se}nd it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass$s.$c$se
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
  fun `test simple motion line mode`() {
    doTest(
      listOf("gH", "<S-Right>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I$c found it in a legendary land
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
  fun `test lineend line mode`() {
    doTest(
      listOf("gH", "<S-Right>"),
      """
                A Discovery

                I found it in a legendary lan${c}d
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I found it in a legendary land$c
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
  fun `test out of line line mode`() {
    doTest(
      listOf("gH", "<S-Right>".repeat(2)),
      """
                A Discovery

                I found it in a legendary lan${c}d
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I found it in a legendary land$c
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
  fun `test fileend line mode`() {
    doTest(
      listOf("gH", "<S-Right>"),
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
                ${s}hard by the torrent of a mountain pass.$c$se
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
      listOf("gH", "<S-Right>"),
      """
                A Discovery

                I found ${c}it in ${c}a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass$c.
      """.trimIndent(),
      """
                A Discovery

                ${s}I found i${c}t in a legendary land
                ${se}all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                ${s}hard by the torrent of a mountain pass.$c$se
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
  fun `test simple motion block mode`() {
    doTest(
      listOf("g<C-H>", "<S-Right>"),
      """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${s}I $c${se}found it in a legendary land
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
  fun `test at the lineend block mode`() {
    doTest(
      listOf("g<C-H>", "<S-Right>"),
      """
                A Discovery

                I found it in a legendary la${c}nd
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary la${s}nd$c$se
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
  fun `test out of line block mode`() {
    doTest(
      listOf("g<C-H>", "<S-Right>".repeat(2)),
      """
                A Discovery

                I found it in a legendary lan${c}d
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary lan${s}d$c$se
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
  fun `test file end block mode`() {
    doTest(
      listOf("g<C-H>", "<S-Right>".repeat(2)),
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

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continueselect),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test to longer line block mode`() {
    doTest(
      listOf("g<C-H>", "<S-Down>", "<S-Right>".repeat(3)),
      """
                A Discovery

                I found it in a legendary lan${c}d
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I found it in a legendary lan${s}d$se
                all rocks and lavender and tu${s}fted$c$se grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.SELECT,
      FimStateMachine.SubMode.VISUAL_BLOCK
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_continuevisual),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test continuevisual`() {
    doTest(
      listOf("v", "<S-Right>".repeat(3)),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}fou${c}n${se}d it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, ""),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test no continueselect`() {
    doTest(
      listOf("gh", "<S-Right>".repeat(3)),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}found it in ${c}${se}a legendary land
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
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, ""),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test no continuevisual`() {
    doTest(
      listOf("v", "<S-Right>".repeat(3)),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I ${s}found it in ${c}a${se} legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL,
      FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }
}
