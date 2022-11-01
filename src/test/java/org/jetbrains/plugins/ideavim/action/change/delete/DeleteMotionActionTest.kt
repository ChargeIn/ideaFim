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
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class DeleteMotionActionTest : FimTestCase() {

  fun `test delete last line`() {
    typeTextInFile(
      injector.parser.parseKeys("dd"),
      """
        def xxx():
          expression one
          expression${c} two
      """.trimIndent()
    )
    assertState(
      """
        def xxx():
          ${c}expression one
      """.trimIndent()
    )
  }

  fun `test on line in middle`() {
    typeTextInFile(
      injector.parser.parseKeys("dd"),
      """
        def xxx():
          expression${c} one
          expression two
      """.trimIndent()
    )
    assertState(
      """
        def xxx():
          ${c}expression two
      """.trimIndent()
    )
  }

  fun `test delete single line`() {
    typeTextInFile(
      injector.parser.parseKeys("dd"),
      """
        def x${c}xx():
      """.trimIndent()
    )
    assertState(c)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test delete last line with nostartofline`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.startoflineName)
    typeTextInFile(
      injector.parser.parseKeys("dd"),
      """
        |def xxx():
        |  expression one
        |  expression${c} two
      """.trimMargin()
    )
    assertState(
      """
        |def xxx():
        |  expression${c} one
      """.trimMargin()
    )
  }

  @FimBehaviorDiffers(originalFimAfter = "  expression two\n")
  fun `test delete last line stored with new line`() {
    typeTextInFile(
      injector.parser.parseKeys("dd"),
      """
        def xxx():
          expression one
          expression${c} two
      """.trimIndent()
    )
    val savedText = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: ""
    assertEquals("  expression two\n", savedText)
  }

  fun `test delete line action multicaret`() {
    typeTextInFile(
      injector.parser.parseKeys("d3d"),
      """
        abc${c}de
        abcde
        abcde
        abcde
        ab${c}cde
        abcde
        abcde
        
      """.trimIndent()
    )
    assertState("${c}abcde\n${c}")
  }

  fun `test delete motion action multicaret`() {
    typeTextInFile(
      injector.parser.parseKeys("dt)"),
      """|public class Foo {
         |  int foo(int a, int b) {
         |    boolean bar = (a < 0 && (b < 0 || a > 0)${c} || b != 0);
         |    if (bar${c} || b != 0) {
         |      return a;
         |    }
         |    else {
         |      return b;
         |    }
         |  }
         |}
        """.trimMargin()
    )
    assertState(
      """|public class Foo {
         |  int foo(int a, int b) {
         |    boolean bar = (a < 0 && (b < 0 || a > 0)${c});
         |    if (bar${c}) {
         |      return a;
         |    }
         |    else {
         |      return b;
         |    }
         |  }
         |}
         """.trimMargin()
    )
  }

  fun `test delete empty line`() {
    val file = """
            A Discovery
            ${c}
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val newFile = """
            A Discovery
            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("dd"), file)
    assertState(newFile)
  }

  fun `test delete on last line`() {
    doTest(
      "dd",
      """
            A Discovery
            
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            ${c}
      """.trimIndent(),
      """
            A Discovery
            
            I found it in a legendary land
            ${c}all rocks and lavender and tufted grass,
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test empty line`() {
    doTest(
      "dd",
      """
            A Discovery
            
            ${c}
            
            
            I found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent(),
      """
            A Discovery
            
            ${c}
            
            I found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
