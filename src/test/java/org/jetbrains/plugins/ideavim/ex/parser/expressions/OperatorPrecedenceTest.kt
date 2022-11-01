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

package org.jetbrains.plugins.ideafim.ex.parser.expressions

import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals

class OperatorPrecedenceTest {

  @Test
  fun `boolean operators`() {
    assertEquals(FimInt(0), FimscriptParser.parseExpression("0 || 1 && 0")!!.evaluate())
  }

  @Test
  fun `boolean operators 2`() {
    assertEquals(FimInt(0), FimscriptParser.parseExpression("!1 || 0")!!.evaluate())
  }

  @Test
  fun `concatenation and multiplication`() {
    assertEquals(FimString("410"), FimscriptParser.parseExpression("4 . 5 * 2")!!.evaluate())
  }

  @Test
  fun `concatenation and multiplication 2`() {
    assertEquals(FimString("202"), FimscriptParser.parseExpression("4 * 5 . 2")!!.evaluate())
  }

  @Test
  fun `arithmetic operators`() {
    assertEquals(FimInt(6), FimscriptParser.parseExpression("2 + 2 * 2")!!.evaluate())
  }

  @Test
  fun `comparison operators`() {
    assertEquals(FimInt(1), FimscriptParser.parseExpression("10 < 5 + 29")!!.evaluate())
  }

  @Test
  fun `sublist operator`() {
    assertEquals(FimString("ab"), FimscriptParser.parseExpression("'a' . 'bc'[0]")!!.evaluate())
  }

  @Test
  fun `not with sublist`() {
    assertEquals(FimInt(0), FimscriptParser.parseExpression("!{'a': 1}['a']")!!.evaluate())
  }
}
