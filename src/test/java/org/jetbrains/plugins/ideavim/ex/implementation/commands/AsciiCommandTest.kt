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
import org.jetbrains.plugins.ideafim.FimTestCase

class AsciiCommandTest : FimTestCase() {
  fun `test shows ascii value under caret`() {
    configureByText("${c}Hello world")
    enterCommand("ascii")
    assertEquals("<H>  72,  Hex 48,  Oct 110", com.flop.idea.fim.FimPlugin.getMessage())
  }

  fun `test show ascii for space`() {
    configureByText("$c ")
    enterCommand("ascii")
    assertEquals("< >  32,  Hex 20,  Oct 040, Digr SP", com.flop.idea.fim.FimPlugin.getMessage())
  }

  fun `test shows unprintable ascii code`() {
    configureByText("${c}\u0009")
    enterCommand("ascii")
    assertEquals("<^I>  9,  Hex 09,  Oct 011, Digr HT", com.flop.idea.fim.FimPlugin.getMessage())
  }

  fun `test shows unprintable ascii code 2`() {
    configureByText("${c}\u007f")
    enterCommand("ascii")
    assertEquals("<^?>  127,  Hex 7f,  Oct 177, Digr DT", com.flop.idea.fim.FimPlugin.getMessage())
  }

  fun `test shows unprintable ascii code 3`() {
    configureByText("${c}\u0006")
    enterCommand("ascii")
    assertEquals("<^F>  6,  Hex 06,  Oct 006, Digr AK", com.flop.idea.fim.FimPlugin.getMessage())
  }

  fun `test unicode char with 3 hex digits`() {
    configureByText("${c}œ")
    enterCommand("ascii")
    assertEquals("<œ> 339, Hex 0153, Oct 523, Digr oe", com.flop.idea.fim.FimPlugin.getMessage())
  }

  fun `test unicode char with 4 hex digits`() {
    configureByText("✓")
    enterCommand("ascii")
    assertEquals("<✓> 10003, Hex 2713, Oct 23423, Digr OK", com.flop.idea.fim.FimPlugin.getMessage())
  }
}
