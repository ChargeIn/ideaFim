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

package org.jetbrains.plugins.ideafim.ex

import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.CommandLineFimLContext
import com.flop.idea.fim.fimscript.model.Script
import com.flop.idea.fim.fimscript.model.commands.EchoCommand
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.statements.IfStatement
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FimLContextTest {

  @Test
  fun `get first context test`() {
    val echoCommand = EchoCommand(Ranges(), listOf(SimpleExpression("oh, hi Mark")))
    val ifStatement1 = IfStatement(listOf(Pair(SimpleExpression(1), listOf(echoCommand))))
    val ifStatement2 = IfStatement(listOf(Pair(SimpleExpression(1), listOf(ifStatement1))))
    val script = Script(listOf(ifStatement2))

    echoCommand.fimContext = ifStatement1
    ifStatement1.fimContext = ifStatement2
    ifStatement2.fimContext = script

    assertEquals(script, echoCommand.getFirstParentContext())
  }

  @Test
  fun `script is a first parent`() {
    assertTrue(Script().isFirstParentContext())
  }

  @Test
  fun `command line is a first parent`() {
    assertTrue(CommandLineFimLContext.isFirstParentContext())
  }
}
