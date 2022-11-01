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

package org.jetbrains.plugins.ideafim.action

import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class CommandCountTest : FimTestCase() {
  fun `test count operator motion`() {
    configureByText("${c}1234567890")
    typeText(injector.parser.parseKeys("3dl"))
    assertState("4567890")
  }

  fun `test operator count motion`() {
    configureByText("${c}1234567890")
    typeText(injector.parser.parseKeys("d3l"))
    assertState("4567890")
  }

  fun `test count operator count motion`() {
    configureByText("${c}1234567890")
    typeText(injector.parser.parseKeys("2d3l"))
    assertState("7890")
  }

  // See https://github.com/fim/fim/blob/b376ace1aeaa7614debc725487d75c8f756dd773/src/normal.c#L631
  fun `test count resets to 999999999L if gets too large`() {
    configureByText("1")
    typeText(injector.parser.parseKeys("12345678901234567890<C-A>"))
    assertState("1000000000")
  }

  fun `test count select register count operator count motion`() {
    configureByText("${c}123456789012345678901234567890")
    typeText(injector.parser.parseKeys("2\"a3d4l")) // Delete 24 characters
    assertState("567890")
  }

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun `test multiple select register counts`() {
    configureByText("${c}12345678901234567890123456789012345678901234567890")
    typeText(injector.parser.parseKeys("2\"a2\"b2\"b2d2l")) // Delete 32 characters
    assertState("345678901234567890")
  }
}
