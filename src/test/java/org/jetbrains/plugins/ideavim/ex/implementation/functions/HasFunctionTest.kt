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

package org.jetbrains.plugins.ideafim.ex.implementation.functions

import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class HasFunctionTest : FimTestCase() {

  fun `test has for supported feature`() {
    configureByText("\n")
    typeText(commandToKeys("echo has('ide')"))
    assertExOutput("1\n")
  }

  fun `test has for unsupported feature`() {
    configureByText("\n")
    typeText(commandToKeys("echo has('autocmd')"))
    assertExOutput("0\n")
  }

  fun `test has for int as an argument`() {
    configureByText("\n")
    typeText(commandToKeys("echo has(42)"))
    assertExOutput("0\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN_ERROR)
  fun `test has for list as an argument`() {
    configureByText("\n")
    typeText(commandToKeys("echo has([])"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E730: Using a List as a String")
  }
}
