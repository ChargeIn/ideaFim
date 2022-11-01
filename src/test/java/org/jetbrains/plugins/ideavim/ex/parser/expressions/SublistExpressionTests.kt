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
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.SublistExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(Theories::class)
class SublistExpressionTests {

  companion object {
    @JvmStatic
    val values = listOf("", " ") @DataPoints get
  }

  @Theory
  fun `sublist with no range specified`(sp1: String, sp2: String) {
    val ex = FimscriptParser.parseExpression("1[$sp1:$sp2]")
    assertTrue(ex is SublistExpression)
    assertEquals(FimInt(1), ex.expression.evaluate())
    assertNull(ex.from)
    assertNull(ex.to)
  }

  @Theory
  fun `sublist with only start specified`(sp1: String, sp2: String, sp3: String) {
    val ex = FimscriptParser.parseExpression("'text'[${sp1}2$sp2:$sp3]")
    assertTrue(ex is SublistExpression)
    assertEquals(FimString("text"), ex.expression.evaluate())
    assertEquals(FimInt(2), ex.from!!.evaluate())
    assertNull(ex.to)
  }

  @Theory
  fun `sublist with only end specified`(sp1: String, sp2: String, sp3: String) {
    val ex = FimscriptParser.parseExpression("var[$sp1:${sp2}32$sp3]")
    assertTrue(ex is SublistExpression)
    assertTrue(ex.expression is Variable)
    assertEquals("var", (ex.expression as Variable).name.evaluate().asString())
    assertNull(ex.from)
    assertEquals(FimInt(32), ex.to!!.evaluate())
  }

  @Theory
  fun `sublist with range specified`(sp1: String, sp2: String, sp3: String, sp4: String) {
    val ex = FimscriptParser.parseExpression("[1, 2, 3, 4, 5, 6][${sp1}1$sp2:${sp3}4$sp4]")
    assertTrue(ex is SublistExpression)
    assertEquals(
      FimList(mutableListOf(FimInt(1), FimInt(2), FimInt(3), FimInt(4), FimInt(5), FimInt(6))),
      ex.expression.evaluate()
    )
    assertEquals(FimInt(1), ex.from!!.evaluate())
    assertEquals(FimInt(4), ex.to!!.evaluate())
  }

  @Theory
  fun `sublist with non int expressions in ranges`(sp1: String, sp2: String, sp3: String, sp4: String, sp5: String, sp6: String) {
    val ex = FimscriptParser.parseExpression("[1, 2, 3, 4, 5, 6][${sp1}1$sp2+${sp3}5$sp4:$sp5'asd'$sp6]")
    assertTrue(ex is SublistExpression)
    assertEquals(
      FimList(mutableListOf(FimInt(1), FimInt(2), FimInt(3), FimInt(4), FimInt(5), FimInt(6))),
      ex.expression.evaluate()
    )
    assertEquals(FimInt(6), ex.from!!.evaluate())
    assertEquals(FimString("asd"), ex.to!!.evaluate())
  }
}
