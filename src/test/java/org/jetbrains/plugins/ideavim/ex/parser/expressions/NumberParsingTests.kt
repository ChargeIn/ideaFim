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

import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals

class NumberParsingTests {

  @Test
  fun `one digit decimal number`() {
    assertEquals(FimInt(4), FimscriptParser.parseExpression("4")!!.evaluate())
  }

  @Test
  fun `decimal number`() {
    assertEquals(FimInt(12), FimscriptParser.parseExpression("12")!!.evaluate())
  }

  @Test
  fun `negative decimal number`() {
    assertEquals(FimInt(-10), FimscriptParser.parseExpression("-10")!!.evaluate())
  }

  @Test
  fun `hex number`() {
    assertEquals(FimInt(256), FimscriptParser.parseExpression("0x100")!!.evaluate())
  }

  @Test
  fun `negative hex number`() {
    assertEquals(FimInt(-16), FimscriptParser.parseExpression("-0x10")!!.evaluate())
  }

  @Test
  fun `upper and lower case hex number`() {
    assertEquals(FimInt(171), FimscriptParser.parseExpression("0XaB")!!.evaluate())
  }

  @Test
  fun `decimal number with leading zero`() {
    assertEquals(FimInt(19), FimscriptParser.parseExpression("019")!!.evaluate())
  }

  @Test
  fun `decimal number with multiple leading zeros`() {
    assertEquals(FimInt(19), FimscriptParser.parseExpression("00019")!!.evaluate())
  }

  @Test
  fun `one digit octal number`() {
    assertEquals(FimInt(7), FimscriptParser.parseExpression("07")!!.evaluate())
  }

  @Test
  fun `octal number`() {
    assertEquals(FimInt(15), FimscriptParser.parseExpression("017")!!.evaluate())
  }

  @Test
  fun `octal number with multiple leading zeros`() {
    assertEquals(FimInt(15), FimscriptParser.parseExpression("00017")!!.evaluate())
  }

  @Test
  fun `negative octal number`() {
    assertEquals(FimInt(-24), FimscriptParser.parseExpression("-030")!!.evaluate())
  }

  @Test
  fun `float number`() {
    assertEquals(FimFloat(4.0), FimscriptParser.parseExpression("4.0")!!.evaluate())
  }

  @Test
  fun `float number in scientific notation`() {
    assertEquals(FimFloat(4.0), FimscriptParser.parseExpression("0.4e+1")!!.evaluate())
  }

  @Test
  fun `float number in scientific notation with + omitted`() {
    assertEquals(FimFloat(4.0), FimscriptParser.parseExpression("0.4e1")!!.evaluate())
  }

  @Test
  fun `float number in scientific notation with negative exponent`() {
    assertEquals(FimFloat(0.48), FimscriptParser.parseExpression("4.8e-1")!!.evaluate())
  }

  @Test
  fun `negative float number`() {
    assertEquals(FimFloat(-12.1), FimscriptParser.parseExpression("-12.1")!!.evaluate())
  }

  @Test
  fun `negative float number in scientific notation`() {
    assertEquals(FimFloat(-124.56), FimscriptParser.parseExpression("-12.456e1")!!.evaluate())
  }
}
