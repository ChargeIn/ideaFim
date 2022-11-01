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
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.FromDataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(Theories::class)
class ConcatenationOperatorTest {

  companion object {
    @JvmStatic
    val operator = listOf(".", "..")
      @DataPoints("operator") get
    @JvmStatic
    val spaces = listOf("", " ")
      @DataPoints("spaces") get
  }

  @Theory
  fun `integer and integer`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp: String) {
    assertEquals(FimString("23"), FimscriptParser.parseExpression("2$sp$operator 3")!!.evaluate())
    assertEquals(FimString("23"), FimscriptParser.parseExpression("2 $operator${sp}3")!!.evaluate())
  }

  @Theory
  fun `integer and float`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("3.4$sp1$operator${sp2}2")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E806: using Float as a String", e.message)
    }
  }

  @Theory
  fun `float and float`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("3.4$sp1$operator${sp2}2.2")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E806: using Float as a String", e.message)
    }
  }

  @Theory
  fun `string and float`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("'string'$sp1$operator${sp2}3.4")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E806: using Float as a String", e.message)
    }
  }

  @Theory
  fun `string and string`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    assertEquals(
      FimString("stringtext"),
      FimscriptParser.parseExpression("'string'$sp1$operator$sp2'text'")!!.evaluate()
    )
  }

  @Theory
  fun `string and integer`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    assertEquals(
      FimString("string3"),
      FimscriptParser.parseExpression("'string'$sp1$operator${sp2}3")!!.evaluate()
    )
  }

  @Theory
  fun `String and list`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("2$sp1$operator$sp2[1, 2]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E730: Using a List as a String", e.message)
    }
  }

  @Theory
  fun `string and list`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("'string'$sp1$operator$sp2[1, 2]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E730: Using a List as a String", e.message)
    }
  }

  @Theory
  fun `list and list`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("[3]$sp1$operator$sp2[1, 2]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E730: Using a List as a String", e.message)
    }
  }

  @Theory
  fun `dict and integer`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      if (sp1 == "" && sp2 == "") { // it is not a concatenation, so let's skip this case
        throw ExException("E731: Using a Dictionary as a String")
      }
      FimscriptParser.parseExpression("{'key' : 21}$sp1$operator${sp2}1")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E731: Using a Dictionary as a String", e.message)
    }
  }

  @Theory
  fun `dict and float`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("{'key' : 21}$sp1$operator${sp2}1.4")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E731: Using a Dictionary as a String", e.message)
    }
  }

  @Theory
  fun `dict and string`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("{'key' : 21}$sp1$operator$sp2'string'")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E731: Using a Dictionary as a String", e.message)
    }
  }

  @Theory
  fun `dict and list`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("{'key' : 21}$sp1$operator$sp2[1]")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E731: Using a Dictionary as a String", e.message)
    }
  }

  @Theory
  fun `dict and dict`(@FromDataPoints("operator") operator: String, @FromDataPoints("spaces") sp1: String, @FromDataPoints("spaces") sp2: String) {
    try {
      FimscriptParser.parseExpression("{'key' : 21}$sp1$operator$sp2{'key2': 33}")!!.evaluate()
    } catch (e: ExException) {
      assertEquals("E731: Using a Dictionary as a String", e.message)
    }
  }
}
