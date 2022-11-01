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

package org.jetbrains.plugins.ideafim.action.motion.gn

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.Direction
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import javax.swing.KeyStroke

class GnPreviousTextObjectTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test delete word`() {
    doTestWithSearch(
      injector.parser.parseKeys("dgN"),
      """
      Hello, ${c}this is a test here
      """.trimIndent(),
      """
        Hello, this is a ${c} here
      """.trimIndent()
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test delete second word`() {
    doTestWithSearch(
      injector.parser.parseKeys("2dgN"),
      """
      Hello, this is a test here
      Hello, this is a test ${c}here
      """.trimIndent(),
      """
        Hello, this is a ${c} here
        Hello, this is a test here
      """.trimIndent()
    )
  }

  fun `test gn uses last used pattern not just search pattern`() {
    doTest(
      listOf("/is<CR>", ":s/test/tester/<CR>", "$", "dgN"),
      "Hello, ${c}this is a test here",
      "Hello, this is a ${c}er here",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  private fun doTestWithSearch(keys: List<KeyStroke>, before: String, after: String) {
    configureByText(before)
    com.flop.idea.fim.FimPlugin.getSearch().setLastSearchState(myFixture.editor, "test", "", Direction.FORWARDS)
    typeText(keys)
    assertState(after)
    assertState(FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }
}
