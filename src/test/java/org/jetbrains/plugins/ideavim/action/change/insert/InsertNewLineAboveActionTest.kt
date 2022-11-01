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

package org.jetbrains.plugins.ideafim.action.change.insert

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class InsertNewLineAboveActionTest : FimTestCase() {
  fun `test insert new line above`() {
    val before = """I found it in a legendary land
        |${c}all rocks and lavender and tufted grass,
        |where it was settled on some sodden sand
        |hard by the torrent of a mountain pass.""".trimMargin()
    val after = """I found it in a legendary land
        |$c
        |all rocks and lavender and tufted grass,
        |where it was settled on some sodden sand
        |hard by the torrent of a mountain pass.""".trimMargin()
    doTest("O", before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test insert new line above with caret in middle of line`() {
    val before = """I found it in a legendary land
        |all rocks and ${c}lavender and tufted grass,
        |where it was settled on some sodden sand
        |hard by the torrent of a mountain pass.""".trimMargin()
    val after = """I found it in a legendary land
        |$c
        |all rocks and lavender and tufted grass,
        |where it was settled on some sodden sand
        |hard by the torrent of a mountain pass.""".trimMargin()
    doTest("O", before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test insert new line above matches indent for plain text`() {
    val before = """    I found it in a legendary land
        |    all rocks and lavender and tufted grass,
        |    ${c}where it was settled on some sodden sand
        |    hard by the torrent of a mountain pass.""".trimMargin()
    val after = """    I found it in a legendary land
        |    all rocks and lavender and tufted grass,
        |    $c
        |    where it was settled on some sodden sand
        |    hard by the torrent of a mountain pass.""".trimMargin()
    doTest("O", before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  fun `test insert new line above matches indent for first line of plain text`() {
    val before = """    ${c}I found it in a legendary land
        |    all rocks and lavender and tufted grass,
        |    where it was settled on some sodden sand
        |    hard by the torrent of a mountain pass.""".trimMargin()
    val after = """    $c
        |    I found it in a legendary land
        |    all rocks and lavender and tufted grass,
        |    where it was settled on some sodden sand
        |    hard by the torrent of a mountain pass.""".trimMargin()
    doTest("O", before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN) // Java support would be a neofim plugin
  fun `test insert new line above matches indent for java`() {
    val before = """public class C {
      |  Integer a;
      |  ${c}Integer b;
      |}
    """.trimMargin()
    val after = """public class C {
      |  Integer a;
      |  $c
      |  Integer b;
      |}
    """.trimMargin()
    configureByJavaText(before)
    typeText(injector.parser.parseKeys("O"))
    assertState(after)
  }

  fun `test insert new line above with multiple carets`() {
    val before = """    I fou${c}nd it in a legendary land
        |    all rocks and laven${c}der and tufted grass,
        |    where it was sett${c}led on some sodden sand
        |    hard by the tor${c}rent of a mountain pass.""".trimMargin()
    val after = """    $c
        |    I found it in a legendary land
        |    $c
        |    all rocks and lavender and tufted grass,
        |    $c
        |    where it was settled on some sodden sand
        |    $c
        |    hard by the torrent of a mountain pass.""".trimMargin()
    doTest("O", before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test insert new line above at top of screen does not scroll top of screen`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName, FimInt(10))
    configureByLines(50, "I found it in a legendary land")
    setPositionAndScroll(5, 15)
    typeText(injector.parser.parseKeys("O"))
    assertPosition(15, 0)
    assertVisibleArea(5, 39)
  }

  fun `test insert new line above first line`() {
    val before = """${c}I found it in a legendary land
        |all rocks and lavender and tufted grass,
        |where it was settled on some sodden sand
        |hard by the torrent of a mountain pass.""".trimMargin()
    val after = """
        |$c
        |I found it in a legendary land
        |all rocks and lavender and tufted grass,
        |where it was settled on some sodden sand
        |hard by the torrent of a mountain pass.""".trimMargin()
    doTest("O", before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
  }
}
