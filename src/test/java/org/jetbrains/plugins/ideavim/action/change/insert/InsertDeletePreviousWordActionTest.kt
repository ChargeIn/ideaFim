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

package org.jetbrains.plugins.ideafim.action.change.insert

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.FimTestCase

class InsertDeletePreviousWordActionTest : FimTestCase() {
  // VIM-1655
  fun `test deleted word is not yanked`() {
    doTest(
      listOf("yiw", "3wea", "<C-W>", "<ESC>p"),
      """
            I found ${c}it in a legendary land
      """.trimIndent(),
      """
            I found it in a i${c}t land
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun `test word removed`() {
    doTest(
      listOf("i", "<C-W>"),
      """
            I found${c} it in a legendary land
      """.trimIndent(),
      """
            I ${c} it in a legendary land
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test non alpha chars`() {
    doTest(
      listOf("i", "<C-W>", "<C-W>", "<C-W>", "<C-W>"),
      """
            I found (it)${c} in a legendary land
      """.trimIndent(),
      """
            I ${c} in a legendary land
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  fun `test indents and spaces`() {
    doTest(
      listOf("i", "<C-W>", "<C-W>", "<C-W>", "<C-W>"),
      """
            A Discovery
            
                 I${c} found it in a legendary land
      """.trimIndent(),
      """
            A Discovery${c} found it in a legendary land
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  @FimBehaviorDiffers(
    """
            If (found) {
               if (it) {
                  legendary
               }
            ${c}
  """
  )
  fun `test delete starting from the line end`() {
    doTest(
      listOf("i", "<C-W>"),
      """
            If (found) {
               if (it) {
                  legendary
               }
            }${c}
      """.trimIndent(),
      """
            If (found) {
               if (it) {
                  legendary
               ${c}
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-569 |a| |i_CTRL-W|
  fun `test delete previous word dot eol`() {
    doTest(
      listOf("a", "<C-W>"),
      "this is a sentence<caret>.\n", "this is a sentence<caret>\n", FimStateMachine.Mode.INSERT,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-569 |a| |i_CTRL-W|
  fun `test delete previous word last after whitespace`() {
    doTest(
      listOf("A", "<C-W>"),
      "<caret>this is a sentence\n", "this is a <caret>\n", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-513 |A| |i_CTRL-W|
  fun `test delete previous word eol`() {
    doTest(
      listOf("A", "<C-W>"),
      "<caret>\$variable\n", "$<caret>\n", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-112 |i| |i_CTRL-W|
  fun `test insert delete previous word`() {
    typeTextInFile(
      injector.parser.parseKeys("i" + "one two three" + "<C-W>"),
      "hello\n" + "<caret>\n"
    )
    assertState("hello\n" + "one two \n")
  }

  fun `test insert delete previous word action`() {
    typeTextInFile(
      injector.parser.parseKeys("i" + "<C-W>" + "<ESC>"),
      "one tw<caret>o three<caret> four   <caret>\n"
    )
    assertState("one<caret> o<caret> <caret> \n")
  }
}
