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

package org.jetbrains.plugins.ideafim.action.copy

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.newapi.fim
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.junit.Ignore

/**
 * @author Alex Plate
 */

class PutVisualTextMoveCursorActionTest : FimTestCase() {

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test put visual text`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("v2e" + "2gp"))
    val after = "legendarylegendary$c in a legendary land"
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test put visual text linewise`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.LINE_WISE, false)
    typeText(injector.parser.parseKeys("v2e" + "gp"))
    val after = """

            legendary
            $c in a legendary land
    """.trimIndent()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test put visual text line linewise`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("V" + "gp"))
    val after = "legendary\n$c"
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test replace row`() {
    val file = """
            A Discovery

            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val newFile = """
            A Discovery

            Discovery
            ${c}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val editor = configureByText(file)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(2, 11), SelectionType.LINE_WISE, false)
    typeText(injector.parser.parseKeys("V" + "gp"))
    assertState(newFile)
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
            A Discovery

            ound it in a legendary land
             rocks and lavender and tufted grass,
            re it was settled on some sodden sand
            d by the torrent of a mountain pass.
            ${c}A Discovery
    """
  )
  fun `test put line in block selection`() {
    val file = """
            ${c}A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val newFile = """
            A Discovery

            ound it in a legendary land
             rocks and lavender and tufted grass,
            re it was settled on some sodden sand
            d by the torrent of a mountain pass.
            A Discovery
            $c
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("Y" + "2j" + "<C-v>" + "2l" + "3j" + "gp"), file)
    assertState(newFile)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test Put visual text linewise`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.LINE_WISE, false)
    typeText(injector.parser.parseKeys("v2e" + "gP"))
    val after = """

            legendary
            $c in a legendary land
    """.trimIndent()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test Put visual text`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("v2e" + "2gP"))
    val after = "legendarylegendary$c in a legendary land"
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test Put visual text full line`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("v$" + "2gP"))
    val after = "legendarylegendar${c}y"
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test Put visual text line linewise`() {
    val before = "${c}I found it in a legendary land"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 25), SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("V" + "gP"))
    val after = "legendary\n$c"
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.CTRL_CODES)
  fun `test Put line in block selection`() {
    val file = """
            ${c}A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val newFile = """
            A Discovery

            A Discovery
            ${c}ound it in a legendary land
             rocks and lavender and tufted grass,
            re it was settled on some sodden sand
            d by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("Y" + "2j" + "<C-v>" + "2l" + "3j" + "gP"), file)
    assertState(newFile)
  }

  // Legacy tests
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test put visual text linewise multicaret`() {
    val before = """
            q${c}werty
            as${c}dfgh
            ${c}zxcvbn

    """.trimIndent()
    configureByText(before)
    injector.registerGroup.storeText('*', "zxcvbn\n", SelectionType.LINE_WISE)
    typeText(injector.parser.parseKeys("vl" + "\"*gp"))
    val after = """
            q
            zxcvbn
            ${c}rty
            as
            zxcvbn
            ${c}gh

            zxcvbn
            ${c}cvbn

    """.trimIndent()
    assertState(after)
  }

  @Suppress("unused")
  @Ignore
  fun `ingoretest put visual block visual line mode`() {
    val before = """
            qw${c}e
            asd
            zxc
            rty
            fgh
            vbn
    """.trimIndent()
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 19), SelectionType.BLOCK_WISE, false)
    typeText(injector.parser.parseKeys("<S-v>" + "gp"))
    val after = """
            ${c}fgh
            asd
            zxc
            rty
            fgh
            vbn
    """.trimIndent()
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test put visual block linewise`() {
    val before = """
            qw${c}e
            asd
            zxc
            rty
            fgh
            vbn
    """.trimIndent()
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 19), SelectionType.LINE_WISE, false)
    typeText(injector.parser.parseKeys("<C-v>" + "h" + "gp"))
    val after = """
            q
            fgh
            $c
            asd
            zxc
            rty
            fgh
            vbn
    """.trimIndent()
    assertState(after)
  }

  @Suppress("unused")
  @Ignore
  fun `ignoretest put visual text multicaret`() {
    val before = "${c}qwe asd ${c}zxc rty ${c}fgh vbn"
    val editor = configureByText(before)
    com.flop.idea.fim.FimPlugin.getRegister().storeText(editor.fim, TextRange(16, 19), SelectionType.CHARACTER_WISE, false)
    typeText(injector.parser.parseKeys("v2e" + "2gp"))
    val after = "fghfgh$c fghfgh$c fghfgh$c"
    assertState(after)
  }
}
