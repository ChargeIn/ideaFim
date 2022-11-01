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

package org.jetbrains.plugins.ideafim.action.change.delete

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimOptionDefaultAll
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption

class DeleteVisualLinesEndActionTest : FimOptionTestCase(OptionConstants.virtualeditName) {
  @FimOptionDefaultAll
  fun `test simple deletion`() {
    val keys = listOf("v", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(FimTestOption(OptionConstants.virtualeditName, OptionValueType.STRING, OptionConstants.virtualedit_onemore))
  fun `test virtual edit delete middle to end`() {
    doTest(
      "D",
      """
            Yesterday it w${c}orked
            Today it is not working
            The test is like that.
      """.trimIndent(),
      """
            Yesterday it w${c}
            Today it is not working
            The test is like that.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(FimTestOption(OptionConstants.virtualeditName, OptionValueType.STRING, OptionConstants.virtualedit_onemore))
  fun `test virtual edit delete end to end`() {
    doTest(
      "D",
      """
            Yesterday it worke${c}d
            Today it is not working
            The test is like that.
      """.trimIndent(),
      """
            Yesterday it worke${c}
            Today it is not working
            The test is like that.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(FimTestOption(OptionConstants.virtualeditName, OptionValueType.STRING, OptionConstants.virtualedit_onemore))
  fun `test virtual edit delete to end from virtual space`() {
    doTest(
      "D",
      """
            Yesterday it worked${c}
            Today it is not working
            The test is like that.
      """.trimIndent(),
      """
            Yesterday it worke${c}
            Today it is not working
            The test is like that.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @FimOptionDefaultAll
  fun `test simple deletion with indent`() {
    val keys = listOf("v", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

                ${c}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test simple deletion with indent and nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    val keys = listOf("v", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

              ${c}  all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion empty line`() {
    val keys = listOf("v", "D")
    val before = """
            A Discovery
            ${c}
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery
            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion last line`() {
    val keys = listOf("v", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the ${c}torrent of a mountain pass.

    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            ${c}
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion first line`() {
    val keys = listOf("v", "D")
    val before = """
            A ${c}Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            ${c}
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion before empty`() {
    val keys = listOf("v", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,

            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            ${c}
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion last line without empty line`() {
    val keys = listOf("v", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            ${c}where it was settled on some sodden sand
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion multiline`() {
    val keys = listOf("vj", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test simple deletion multiline motion up`() {
    val keys = listOf("vk", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test delete visual lines end action`() {
    typeTextInFile(
      injector.parser.parseKeys("v" + "2j" + "D"),
      """
                    a${c}bcde
                    abcde
                    abcde
                    abcde
                    abcd${c}e
                    abcde
                    abcde

      """.trimIndent()
    )
    assertState("${c}abcde\n${c}")
  }

  @FimOptionDefaultAll
  fun `test line simple deletion`() {
    val keys = listOf("V", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test line deletion with indent`() {
    val keys = listOf("V", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

                ${c}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test line deletion with indent and nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    val keys = listOf("V", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

              ${c}  all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test line deletion empty line`() {
    val keys = listOf("V", "D")
    val before = """
            A Discovery
            ${c}
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery
            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test line deletion last line`() {
    val keys = listOf("V", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the ${c}torrent of a mountain pass.

    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            ${c}
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test line deletion last line without empty line`() {
    val keys = listOf("V", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the ${c}torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            ${c}where it was settled on some sodden sand
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test line deletion multiline`() {
    val keys = listOf("Vj", "D")
    val before = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test line deletion multiline motion up`() {
    val keys = listOf("Vk", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all ${c}rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test line delete visual lines end action`() {
    typeTextInFile(
      injector.parser.parseKeys("V" + "2j" + "D"),
      """
                    a${c}bcde
                    abcde
                    abcde
                    abcde
                    abcd${c}e
                    abcde
                    abcde

      """.trimIndent()
    )
    assertState("${c}abcde\n${c}")
  }

  @FimOptionDefaultAll
  fun `test block simple deletion`() {
    val keys = listOf("<C-V>", "D")
    val before = """
            A Discovery

            I${c} found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test block deletion empty line`() {
    val keys = listOf("<C-V>", "D")
    val before = """
            A Discovery
            ${c}
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery
            ${c}
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test block deletion last line`() {
    val keys = listOf("<C-V>", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the${c} torrent of a mountain pass.

    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the

    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test block deletion last line without empty line`() {
    val keys = listOf("<C-V>", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the${c} torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test block deletion multiline`() {
    val keys = listOf("<C-V>j", "D")
    val before = """
            A Discovery

            I${c} found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I
            a
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test block deletion multiline motion up`() {
    val keys = listOf("<C-V>k", "D")
    val before = """
            A Discovery

            I found it in a legendary land
            all${c} rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I f
            all
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @FimOptionDefaultAll
  fun `test delete visual block line end action`() {
    typeTextInFile(
      injector.parser.parseKeys("<C-V>" + "2j" + "2l" + "D"),
      """
                    abcde
                    a${c}bcde
                    abcde
                    abcde
                    abcde

      """.trimIndent()
    )
    assertState(
      """
    abcde
    ${c}a
    a
    a
    abcde

      """.trimIndent()
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(FimTestOption(OptionConstants.virtualeditName, OptionValueType.STRING, OptionConstants.virtualedit_onemore))
  fun `test change dollar`() {
    doTest(
      "c$",
      """
            Yesterday it w${c}orked
            Today it is not working
            The test is like that.
      """.trimIndent(),
      """
            Yesterday it w${c}
            Today it is not working
            The test is like that.
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }
}
