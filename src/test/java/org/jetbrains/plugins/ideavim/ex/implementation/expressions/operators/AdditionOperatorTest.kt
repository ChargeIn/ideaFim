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

package org.jetbrains.plugins.ideafim.ex.implementation.expressions.operators

import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals

class AdditionOperatorTest {

  @Test
  fun `integer plus integer`() {
    assertEquals(FimInt(5), FimscriptParser.parseExpression("2 + 3")!!.evaluate())
  }

  @Test
  fun `integer plus float`() {
    assertEquals(FimFloat(5.4), FimscriptParser.parseExpression("2 + 3.4")!!.evaluate())
  }

  @Test
  fun `float plus float`() {
    assertEquals(FimFloat(5.6), FimscriptParser.parseExpression("2.2 + 3.4")!!.evaluate())
  }

  @Test
  fun `string plus float`() {
    assertEquals(FimFloat(3.4), FimscriptParser.parseExpression("'string' + 3.4")!!.evaluate())
  }

  @Test
  fun `string plus string`() {
    assertEquals(FimInt(0), FimscriptParser.parseExpression("'string' + 'text'")!!.evaluate())
  }

  @Test
  fun `string plus integer`() {
    assertEquals(FimInt(3), FimscriptParser.parseExpression("'string' + 3")!!.evaluate())
  }

  @Test
  fun `number plus list`() {
    try {
      FimscriptParser.parseExpression("2 + [1, 2]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E745: Using a List as a Number", e.message)
    }
  }

  @Test
  fun `string plus list`() {
    try {
      FimscriptParser.parseExpression("'string' + [1, 2]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E745: Using a List as a Number", e.message)
    }
  }

  @Test
  fun `list plus list`() {
    assertEquals(
      FimList(mutableListOf(FimInt(3), FimInt(1), FimInt(2))),
      FimscriptParser.parseExpression("[3] + [1, 2]")!!.evaluate()
    )
  }

  @Test
  fun `dict plus integer`() {
    try {
      FimscriptParser.parseExpression("{'key' : 21} + 1")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E728: Using a Dictionary as a Number", e.message)
    }
  }

  @Test
  fun `dict plus float`() {
    try {
      FimscriptParser.parseExpression("{'key' : 21} + 1.4")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E728: Using a Dictionary as a Number", e.message)
    }
  }

  @Test
  fun `dict plus string`() {
    try {
      FimscriptParser.parseExpression("{'key' : 21} + 'string'")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E728: Using a Dictionary as a Number", e.message)
    }
  }

  @Test
  fun `dict plus list`() {
    try {
      FimscriptParser.parseExpression("{'key' : 21} + [1]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E728: Using a Dictionary as a Number", e.message)
    }
  }

  @Test
  fun `dict plus dict`() {
    try {
      FimscriptParser.parseExpression("{'key' : 21} + {'key2': 33}")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E728: Using a Dictionary as a Number", e.message)
    }
  }
}
