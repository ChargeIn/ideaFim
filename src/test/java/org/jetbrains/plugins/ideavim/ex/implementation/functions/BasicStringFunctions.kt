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

import org.jetbrains.plugins.ideafim.FimTestCase

class BasicStringFunctions : FimTestCase() {

  fun `test toupper`() {
    configureByText("\n")
    typeText(commandToKeys("echo toupper('Fim is awesome')"))
    assertExOutput("VIM IS AWESOME\n")
  }

  fun `test tolower`() {
    configureByText("\n")
    typeText(commandToKeys("echo toupper('Fim is awesome')"))
    assertExOutput("fim is awesome\n")
  }

  fun `test join`() {
    configureByText("\n")
    typeText(commandToKeys("echo join(['Fim', 'is', 'awesome'], '_')"))
    assertExOutput("Fim_is_awesome\n")
  }

  fun `test join without second argument`() {
    configureByText("\n")
    typeText(commandToKeys("echo join(['Fim', 'is', 'awesome'])"))
    assertExOutput("Fim is awesome\n")
  }

  fun `test join with wrong first argument type`() {
    configureByText("\n")
    typeText(commandToKeys("echo join('Fim is awesome')"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E714: List required")
  }
}
