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

import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals

class FalsyOperatorTest {

  @Test
  fun `left expression is true`() {
    assertEquals(FimInt("42"), FimscriptParser.parseExpression("42 ?? 999")!!.evaluate())
  }

  @Test
  fun `left expression is false`() {
    assertEquals(FimInt("42"), FimscriptParser.parseExpression("0 ?? 42")!!.evaluate())
  }

  @Test
  fun `empty list as a left expression`() {
    assertEquals(FimString("list is empty"), FimscriptParser.parseExpression("[] ?? 'list is empty'")!!.evaluate())
  }

  @Test
  fun `nonempty list as a left expression`() {
    assertEquals(
      FimList(mutableListOf(FimInt(1), FimInt(2), FimInt(3))),
      FimscriptParser.parseExpression("[1, 2, 3] ?? 'list is empty'")!!.evaluate()
    )
  }
}
