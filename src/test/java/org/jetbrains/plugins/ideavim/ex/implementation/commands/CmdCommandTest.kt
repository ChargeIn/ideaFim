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

package org.jetbrains.plugins.ideafim.ex.implementation.commands

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Elliot Courant
 */
class CmdCommandTest : FimTestCase() {
  fun `test recursive`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command Recur1 Recur2"))
    assertPluginError(false)
    typeText(commandToKeys("command Recur2 Recur1"))
    assertPluginError(false)
    typeText(commandToKeys("Recur1"))
    assertPluginError(true) // Recursive command should error.
  }

  fun `test list aliases`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command"))
    assertPluginError(false)
    assertExOutput("Name        Args       Definition\n") // There should not be any aliases.

    typeText(commandToKeys("command Vs vs"))
    assertPluginError(false)
    typeText(commandToKeys("command Wq wq"))
    assertPluginError(false)
    typeText(commandToKeys("command WQ wq"))
    assertPluginError(false)
    typeText(commandToKeys("command-nargs=* Test1 echo"))
    assertPluginError(false)
    typeText(commandToKeys("command-nargs=? Test2 echo"))
    assertPluginError(false)
    typeText(commandToKeys("command-nargs=+ Test3 echo"))
    assertPluginError(false)
    typeText(commandToKeys("command-nargs=1 Test4 echo"))
    assertPluginError(false)
    typeText(commandToKeys("command"))
    assertPluginError(false)
    // The added alias should be listed
    assertExOutput(
      """Name        Args       Definition
            |Test1       *          echo
            |Test2       ?          echo
            |Test3       +          echo
            |Test4       1          echo
            |Vs          0          vs
            |Wq          0          wq
            |WQ          0          wq
        """.trimMargin()
    )

    typeText(commandToKeys("command W"))
    assertPluginError(false)
    // The filtered aliases should be listed
    assertExOutput(
      """Name        Args       Definition
            |Wq          0          wq
            |WQ          0          wq
        """.trimMargin()
    )
  }

  fun `test bad alias`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("Xyz"))
    assertPluginError(true)
    typeText(commandToKeys("command Xyz yank"))
    assertPluginError(false)
    typeText(commandToKeys("Xyz"))
    assertPluginError(false)
  }

  fun `test lowercase should fail`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command lowercase vs"))
    assertPluginError(true)
  }

  fun `test blacklisted alias should fail`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command X vs"))
    assertPluginError(true)
    typeText(commandToKeys("command Next vs"))
    assertPluginError(true)
    typeText(commandToKeys("command Print vs"))
    assertPluginError(true)
  }

  fun `test add an existing alias and overwrite`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command Existing1 vs"))
    assertPluginError(false)
    typeText(commandToKeys("command Existing1 wq"))
    assertPluginError(true)
    typeText(commandToKeys("command! Existing1 wq"))
    assertPluginError(false)
  }

  fun `test add command with arguments`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command -nargs=* Error echo <args>"))
    assertPluginError(false)
  }

  fun `test add command with range`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command! -range Error echo <args>"))
    assertPluginError(false)
    assertEquals("'-range' is not supported by `command`", injector.messages.getStatusBarMessage())
  }

  fun `test add command with complete`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command! -complete=color Error echo <args>"))
    assertPluginError(false)
    assertEquals("'-complete' is not supported by `command`", injector.messages.getStatusBarMessage())
  }

  fun `test add command with arguments short`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command-nargs=* Error echo <args>"))
    assertPluginError(false)
  }

  fun `test add command with arguments even shorter`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("com-nargs=* Error echo <args>"))
    assertPluginError(false)
  }

  fun `test add command with various arguments`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command! -nargs=0 Error echo <args>"))
    assertPluginError(false)
    typeText(commandToKeys("command! -nargs=1 Error echo <args>"))
    assertPluginError(false)
    typeText(commandToKeys("command! -nargs=* Error echo <args>"))
    assertPluginError(false)
    typeText(commandToKeys("command! -nargs=? Error echo <args>"))
    assertPluginError(false)
    typeText(commandToKeys("command! -nargs=+ Error echo <args>"))
    assertPluginError(false)
  }

  fun `test add command with invalid arguments`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command! -nargs= Error echo <args>"))
    assertPluginError(true)
    typeText(commandToKeys("command! -nargs=-1 Error echo <args>"))
    assertPluginError(true)
    typeText(commandToKeys("command! -nargs=# Error echo <args>"))
    assertPluginError(true)
  }

  fun `test run command with arguments`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("let test = \"Hello!\""))
    assertPluginError(false)
    typeText(commandToKeys("command! -nargs=1 Error echo <args>"))
    assertPluginError(false)
    typeText(commandToKeys("Error test"))
    assertPluginError(false)
    assertExOutput("Hello!\n")

    typeText(commandToKeys("command! -nargs=1 Error echo <q-args>"))
    assertPluginError(false)
    typeText(commandToKeys("Error test message"))
    assertPluginError(false)
    assertExOutput("test message\n")
  }

  fun `test run command that creates another command`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command! -nargs=1 CreateCommand command -nargs=1 <args> <lt>q-args>"))
    assertPluginError(false)
    typeText(commandToKeys("CreateCommand Show echo"))
    assertPluginError(false)
    typeText(commandToKeys("Show test"))
    assertPluginError(false)
    assertExOutput("test\n")
  }

  fun `test run command missing required argument`() {
    com.flop.idea.fim.FimPlugin.getCommand().resetAliases()
    configureByText("\n")
    typeText(commandToKeys("command! -nargs=1 Error echo <q-args>"))
    assertPluginError(false)
    typeText(commandToKeys("Error"))
  }
}
