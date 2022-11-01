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

package org.jetbrains.plugins.ideafim.ex.parser.commands

import com.flop.idea.fim.ex.ranges.LineNumberRange
import com.flop.idea.fim.ex.ranges.MarkRange
import com.flop.idea.fim.fimscript.model.commands.BufferCommand
import com.flop.idea.fim.fimscript.model.commands.DeleteLinesCommand
import com.flop.idea.fim.fimscript.model.commands.EchoCommand
import com.flop.idea.fim.fimscript.model.commands.LetCommand
import com.flop.idea.fim.fimscript.model.commands.PlugCommand
import com.flop.idea.fim.fimscript.model.commands.SetCommand
import com.flop.idea.fim.fimscript.model.commands.SplitCommand
import com.flop.idea.fim.fimscript.model.commands.SplitType
import com.flop.idea.fim.fimscript.model.commands.SubstituteCommand
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.BinExpression
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(Theories::class)
class CommandTests {

  companion object {
    @JvmStatic
    val values = listOf("", " ") @DataPoints get
  }

  @Theory
  fun `let command`() {
    val c = FimscriptParser.parseCommand("let g:catSound='Meow'")
    assertTrue(c is LetCommand)
    assertEquals(Variable(Scope.GLOBAL_VARIABLE, "catSound"), c.variable)
    assertEquals(SimpleExpression("Meow"), c.expression)
  }

  @Theory
  fun `echo command`() {
    val c = FimscriptParser.parseCommand("echo 4 5+7 'hi doggy'")
    assertTrue(c is EchoCommand)
    val expressions = c.args
    assertEquals(3, expressions.size)
    assertTrue(expressions[0] is SimpleExpression)
    assertEquals(FimInt(4), (expressions[0] as SimpleExpression).data)
    assertTrue(expressions[1] is BinExpression)
    assertEquals(FimInt(12), expressions[1].evaluate())
    assertTrue(expressions[2] is SimpleExpression)
    assertEquals(FimString("hi doggy"), (expressions[2] as SimpleExpression).data)
  }

  // VIM-2426
  @Theory
  fun `command with marks in range`(sp: String) {
    val command = FimscriptParser.parseCommand("'a,'b${sp}s/a/b/g")
    assertTrue(command is SubstituteCommand)
    assertEquals("s", command.command)
    assertEquals("/a/b/g", command.argument)
    assertEquals(2, command.ranges.size())
    assertEquals(MarkRange('a', 0, false), command.ranges.ranges[0])
    assertEquals(MarkRange('b', 0, false), command.ranges.ranges[1])
  }

  // https://github.com/JetBrains/ideafim/discussions/386
  @Theory
  fun `no space between command and argument`(sp: String) {
    val command = FimscriptParser.parseCommand("b${sp}1")
    assertTrue(command is BufferCommand)
    assertEquals("1", command.argument)
  }

  // VIM-2445
  @Theory
  fun `spaces in range`(sp1: String, sp2: String, sp3: String) {
    val command = FimscriptParser.parseCommand("10$sp1,${sp2}20${sp3}d")
    assertTrue(command is DeleteLinesCommand)
    assertEquals(2, command.ranges.size())
    assertEquals(LineNumberRange(9, 0, false), command.ranges.ranges[0])
    assertEquals(LineNumberRange(19, 0, false), command.ranges.ranges[1])
  }

  // VIM-2450
  @Theory
  fun `set command`() {
    val command = FimscriptParser.parseCommand("se nonu")
    assertTrue(command is SetCommand)
    assertEquals("nonu", command.argument)
  }

  // VIM-2453
  @Theory
  fun `split command`() {
    val command = FimscriptParser.parseCommand("sp")
    assertTrue(command is SplitCommand)
    assertEquals(SplitType.HORIZONTAL, command.splitType)
  }

  // VIM-2452
  fun `augroup test`() {
    // augusto was recognized as AUGROUP token ('au') and all the lines were ignored
    val script = FimscriptParser.parse(
      """
        Plug 'danilo-augusto/fim-afterglow'
        set nu rnu

        augroup myCmds
        augroup END
      """.trimIndent()
    )
    assertEquals(2, script.units.size)
    assertTrue(script.units[0] is PlugCommand)
    assertTrue(script.units[1] is SetCommand)
  }

  fun `augroup test 2`() {
    val script = FimscriptParser.parse(
      """
        augroup myCmds
          au smthing
        augroup END
        
        Plug 'danilo-augusto/fim-afterglow'
        set nu rnu
      """.trimIndent()
    )
    assertEquals(2, script.units.size)
    assertTrue(script.units[0] is PlugCommand)
    assertTrue(script.units[1] is SetCommand)
  }
}
