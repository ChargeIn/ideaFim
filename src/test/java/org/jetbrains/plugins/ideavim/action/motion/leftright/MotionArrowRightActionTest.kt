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

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimOptionDefaultAll
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

class MotionArrowRightActionTest : FimOptionTestCase(OptionConstants.keymodelName) {

  @TestWithoutNeofim(SkipNeofimReason.INLAYS)
  @FimOptionDefaultAll
  fun `test with inlay related to preceding text and block caret`() {
    val before = "I f${c}ound it in a legendary land"
    val after = "I fo${c}und it in a legendary land"
    configureByText(before)
    assertOffset(3)

    // Inlay shares offset 4 with the 'u' in "found", inserts a new visual column 4 and is related to the text at
    // offset 3/visual column 3.
    // Moving <Right> from offset 3 (visual column 3) to offset 4 should position the caret between the inlay and its
    // related text, at visual column 4, but then the block caret would paint over the inlay, which looks wrong.
    // Position at visual column 5 instead.
    // Before: "I f|o|«:test»und it in a legendary land"
    // After:  "I fo«:test»|u|nd it in a legendary land"
    addInlay(4, true, 5)

    typeText(injector.parser.parseKeys("<Right>"))
    assertState(after)

    assertOffset(4)
    assertVisualPosition(0, 5)
  }

  @TestWithoutNeofim(SkipNeofimReason.INLAYS)
  @FimOptionDefaultAll
  fun `test with inlay related to preceding text and block caret 2`() {
    val before = "I fo${c}und it in a legendary land"
    val after = "I fou${c}nd it in a legendary land"
    configureByText(before)
    assertOffset(4)

    // Inlay shares offset 4 with the 'u' in "found", inserts a new visual column 4 and is related to the text at
    // offset 3/visual column 3.
    // Moving <Right> from offset 4 (visual column 5 for text) will move to offset 5, which is visual column 6.
    // Before: "I fo«:test»|u|nd it in a legendary land."
    // After: "I fo«:test»u|n|d it in a legendary land."
    addInlay(4, true, 5)

    typeText(injector.parser.parseKeys("<Right>"))
    assertState(after)

    assertOffset(5)
    assertVisualPosition(0, 6)
  }

  @TestWithoutNeofim(SkipNeofimReason.INLAYS)
  @FimOptionDefaultAll
  fun `test with inlay related to preceding text and bar caret`() {
    val before = "I f${c}ound it in a legendary land"
    val after = "I fo${c}und it in a legendary land"
    configureByText(before)
    assertOffset(3)

    // Inlay shares offset 4 with the 'u' in "found", inserts a new visual column 4 and is related to the text at
    // offset 3/visual column 3.
    // Moving <Right> from offset 3 (visual column 3) to offset 4 should position the caret between the inlay and the
    // related text at visual column 4, which is the inlay. This is appropriate for the bar caret, which renders
    // "in between columns".
    // Before: "I f|o«:test»und it in a legendary land"
    // After:  "I fo|«:test»und it in a legendary land"
    addInlay(4, true, 5)

    typeText(injector.parser.parseKeys("i" + "<Right>"))
    assertState(after)

    assertOffset(4)
    assertVisualPosition(0, 4)

    typeText(injector.parser.parseKeys("<Esc>"))
    assertOffset(3)
    assertVisualPosition(0, 3)
  }

  @TestWithoutNeofim(SkipNeofimReason.INLAYS)
  @FimOptionDefaultAll
  fun `test with inlay related to preceding text and bar caret 2`() {
    val before = "I fo${c}und it in a legendary land"
    val after = "I fou${c}nd it in a legendary land"
    configureByText(before)
    assertOffset(4)

    assertVisualPosition(0, 4)

    // Inlay shares offset 4 with the 'u' in "found", inserts a new visual column 4 and is related to the text at
    // offset 3/visual column 3.
    // Moving <Right> from offset 4 (visual column 4 because bar caret and related to preceding text!) will move to
    // offset 3, which is also visual column 3.
    // Initially (normal):  "I fo|u|nd it in a legendary land" (caret = vp4)
    // With inlay (normal): "I fo«:test»|u|nd it in a legendary land" (caret = vp5)
    // In insert mode:      "I fo|«:test»und it in a legendary land" (caret = vp4)
    // <Right>:             "I fo«:test»u|nd it in a legendary land" (caret = vp6)
    // <Esc>:               "I fo«:test»|u|nd it in a legendary land" (caret = vp5)
    addInlay(4, true, 5)

    typeText(injector.parser.parseKeys("i"))
    assertVisualPosition(0, 4)

    typeText(injector.parser.parseKeys("<Right>"))
    myFixture.checkResult(after)

    assertOffset(5)
    assertVisualPosition(0, 6)

    typeText(injector.parser.parseKeys("<Esc>"))
    assertOffset(4)
    assertVisualPosition(0, 5)
  }

  // Kotlin parameter hints are a good example of inlays related to following text
  @TestWithoutNeofim(SkipNeofimReason.INLAYS)
  @FimOptionDefaultAll
  fun `test with inlay related to following text and block caret`() {
    val before = "I f${c}ound it in a legendary land"
    val after = "I fo${c}und it in a legendary land"
    configureByText(before)
    assertOffset(3)

    // Inlay shares offset 4 with the 'u' in "found", inserts a new visual column 4 and is related to the text at
    // offset 4/visual column 5.
    // Moving <Right> from offset 3 to offset 4 should position the caret between the inlay and the related text, at
    // visual column 5, which is fine for block caret
    // Before: "I f|o|«test:»und it in a legendary land."
    // After: "I fo«test:»|u|nd it in a legendary land."
    addInlay(4, false, 5)

    typeText(injector.parser.parseKeys("<Right>"))
    assertState(after)

    assertOffset(4)
    assertVisualPosition(0, 5)
  }

  @TestWithoutNeofim(SkipNeofimReason.INLAYS)
  @FimOptionDefaultAll
  fun `test with inlay related to following text and bar caret`() {
    val before = "I f${c}ound it in a legendary land"
    val after = "I fo${c}und it in a legendary land"
    configureByText(before)
    assertOffset(3)

    // Inlay shares offset 4 with the 'u' in "found", inserts a new visual column 4 and is related to the text at
    // offset 4/visual column 5.
    // Moving <Right> from offset 3 to offset 4 should position the caret between the inlay and the related text, at
    // visual column 5, which is fine for bar caret
    // Before: "I f|o«test:»und it in a legendary land."
    // After: "I fo«test:»|und it in a legendary land."
    addInlay(4, false, 5)

    typeText(injector.parser.parseKeys("i" + "<Right>"))
    assertState(after)

    assertOffset(4)
    assertVisualPosition(0, 5)

    typeText(injector.parser.parseKeys("<Esc>"))
    assertOffset(3)
    assertVisualPosition(0, 3)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionDefaultAll
  fun `test visual default options`() {
    doTest(
      listOf("v", "<Right>"),
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
    FimTestOption(
      OptionConstants.keymodelName,
      OptionValueType.STRING,
      OptionConstants.keymodel_stopsel
    )
  )
  fun `test visual stopsel`() {
    doTest(
      listOf("v", "<Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I f${c}ound it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(
      OptionConstants.keymodelName,
      OptionValueType.STRING,
      OptionConstants.keymodel_stopselect
    )
  )
  fun `test visual stopselect`() {
    doTest(
      listOf("v", "<Right>"),
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
    FimTestOption(
      OptionConstants.keymodelName,
      OptionValueType.STRING,
      OptionConstants.keymodel_stopvisual
    )
  )
  fun `test visual stopvisual`() {
    doTest(
      listOf("v", "<Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I f${c}ound it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(
      OptionConstants.keymodelName,
      OptionValueType.STRING,
      OptionConstants.keymodel_stopvisual
    )
  )
  fun `test visual stopvisual multicaret`() {
    doTest(
      listOf("v", "<Right>"),
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and ${c}lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                I f${c}ound it in a legendary land
                all rocks and l${c}avender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
