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

package org.jetbrains.plugins.ideafim.ex.implementation.statements

import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class TryCatchTest : FimTestCase() {

  fun `test successful catch of a throw statement`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          "  throw 'my exception' |" +
          "catch /my exception/ |" +
          "  echo 'caught' |" +
          "endtry"
      )
    )
    assertPluginError(false)
    assertExOutput("caught\n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test unsuccessful catch of a throw statement`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          " throw 'my exception' |" +
          "catch /E117:/ |" +
          "endtry"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("my exception")
  }

  fun `test fim statement successful catch`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          " echo undefinedVariable |" +
          "catch /E121: Undefined variable:/ |" +
          "endtry"
      )
    )
    assertPluginError(false)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test fim statement unsuccessful catch`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          " echo undefinedVariable |" +
          "catch /E117:/ |" +
          "endtry"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: undefinedVariable")
  }

  fun `test multiple catches`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          "  throw 'my exception' |" +
          "catch /E117:/ |" +
          "  echo 'failure' |" +
          "catch /my exception/ |" +
          "endtry"
      )
    )
    assertPluginError(false)
    assertNoExOutput()
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test no matching catch among multiple`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          "  throw 'my exception' |" +
          "catch /E117:/ |" +
          "catch /E118:/ |" +
          "endtry"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("my exception")
  }

  fun `test finally after catch`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          "  throw 'my exception' |" +
          "catch /E117:/ |" +
          "catch /E118:/ |" +
          "catch /my exception/ |" +
          "finally |" +
          "  echo 'finally block' |" +
          "endtry"
      )
    )
    assertPluginError(false)
    assertExOutput("finally block\n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test finally after unsuccessful catch`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "try |" +
          "  throw 'my exception' |" +
          "catch /E117:/ |" +
          "catch /E118:/ |" +
          "finally |" +
          "  echo 'finally block' |" +
          "endtry"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("my exception")
    assertExOutput("finally block\n")
  }

  fun `test finish in try catch`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        """
        let x = 0 |
        let y = 0 |
        try |
          finish |
          let x = 1 |
        finally |
          let y = 1 |
        endtry |
        """.trimIndent()
      )
    )
    typeText(commandToKeys("echo x .. ' ' .. y"))
    assertExOutput("0 1\n")
  }
}
