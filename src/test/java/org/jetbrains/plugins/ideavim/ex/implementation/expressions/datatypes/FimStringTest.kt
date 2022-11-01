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

package org.jetbrains.plugins.ideafim.ex.implementation.expressions.datatypes

import com.flop.idea.fim.fimscript.model.datatypes.FimString
import org.junit.Test
import kotlin.test.assertEquals

class FimStringTest {

  @Test
  fun `string as number`() {
    assertEquals(0.0, FimString("oh, hi Mark").asDouble())
  }

  @Test
  fun `string with zero as number`() {
    assertEquals(0.0, FimString("0oh, hi Mark").asDouble())
  }

  @Test
  fun `string with minus`() {
    assertEquals(0.0, FimString("-oh, hi Mark").asDouble())
  }

  @Test
  fun `string with minus zero`() {
    assertEquals(0.0, FimString("-0oh, hi Mark").asDouble())
  }

  @Test
  fun `string with float`() {
    assertEquals(4.0, FimString("4.67oh, hi Mark").asDouble())
  }

  @Test
  fun `string with digit`() {
    assertEquals(5.0, FimString("5oh, hi Mark").asDouble())
  }

  @Test
  fun `string with integer`() {
    assertEquals(53.0, FimString("53oh, hi Mark").asDouble())
  }

  @Test
  fun `string with negative integer`() {
    assertEquals(-1464.0, FimString("-1464 oh, hi Mark").asDouble())
  }

  @Test
  fun `string with octal number`() {
    assertEquals(83.0, FimString("0123 oh, hi Mark").asDouble())
  }

  @Test
  fun `string with negative octal number`() {
    assertEquals(-83.0, FimString("-0123 oh, hi Mark").asDouble())
  }

  @Test
  fun `string with octal number with multiple leading zeros`() {
    assertEquals(83.0, FimString("000123 oh, hi Mark").asDouble())
  }

  @Test
  fun `string with hex number`() {
    assertEquals(17.0, FimString("0x11 oh, hi Mark").asDouble())
  }

  @Test
  fun `string with negative hex number`() {
    assertEquals(-17.0, FimString("-0x11 oh, hi Mark").asDouble())
  }

  @Test
  fun `string with hex number with multiple leading zeroz`() {
    assertEquals(0.0, FimString("000x11 oh, hi Mark").asDouble())
  }

  @Test
  fun `string as boolean`() {
    assertEquals(false, FimString("oh, hi Mark").asBoolean())
  }

  @Test
  fun `string as boolean2`() {
    assertEquals(true, FimString("3oh, hi Mark").asBoolean())
  }
}
