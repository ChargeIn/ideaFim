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

package org.jetbrains.plugins.ideafim.command

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class FimStateMachineTest : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in normal`() {
    configureByText("123")
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in insert`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("INSERT", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in replace`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("R"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("REPLACE", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in visual`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("v"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- VISUAL --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in visual line`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("V"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- VISUAL LINE --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in visual block`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("<C-V>"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- VISUAL BLOCK --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in select`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("gh"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- SELECT --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in select line`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("gH"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- SELECT LINE --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in select block`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("g<C-H>"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- SELECT BLOCK --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O>"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command visual`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O>v"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) VISUAL --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command visual block`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O><C-V>"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) VISUAL BLOCK --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command visual line`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O>V"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) VISUAL LINE --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command select`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O>gh"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) SELECT --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command select block`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O>g<C-H>"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) SELECT BLOCK --", statusString)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test status string in one command select line`() {
    configureByText("123")
    typeText(injector.parser.parseKeys("i<C-O>gH"))
    val statusString = myFixture.editor.fim.fimStateMachine.getStatusString()
    assertEquals("-- (insert) SELECT LINE --", statusString)
  }
}
