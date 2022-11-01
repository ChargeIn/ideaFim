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

class MotionShiftEndActionTest : FimOptionTestCase(OptionConstants.keymodelName, OptionConstants.selectmodeName) {
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionDefaultAll
  fun `test simple end`() {
    val keys = listOf("<S-End>")
    val before = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a legendary lan${c}d
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  fun `test start visual`() {
    val keys = listOf("<S-End>")
    val before = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a ${s}legendary land${c}${se}
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, OptionConstants.keymodel_startsel),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, OptionConstants.selectmode_key)
  )
  fun `test start select`() {
    val keys = listOf("<S-End>")
    val before = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            I found it in a ${s}legendary land${c}${se}
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)
  }

  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, ""),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test continue visual`() {
    val before = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${s}I found it in a legendary land${c}${se}
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("<S-End>"))
    assertState(FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    typeText(injector.parser.parseKeys("0v" + "<S-End>"))
    assertState(after)
    assertState(FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER)
  }

  @FimOptionTestConfiguration(
    FimTestOption(OptionConstants.keymodelName, OptionValueType.STRING, ""),
    FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, "")
  )
  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test continue select`() {
    val before = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
            A Discovery

            ${s}I found it in a legendary land${c}${se}
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("<S-End>"))
    assertState(FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    typeText(injector.parser.parseKeys("0gh" + "<S-End>"))
    assertState(after)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)
  }
}
