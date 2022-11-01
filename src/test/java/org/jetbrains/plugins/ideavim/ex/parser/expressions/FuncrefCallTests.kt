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

import com.flop.idea.fim.fimscript.model.expressions.BinExpression
import com.flop.idea.fim.fimscript.model.expressions.CurlyBracesName
import com.flop.idea.fim.fimscript.model.expressions.FuncrefCallExpression
import com.flop.idea.fim.fimscript.model.expressions.OneElementSublistExpression
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.expressions.operators.BinaryOperator
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuncrefCallTests {

  @Test
  fun `test dictionary funcref call`() {
    val funcrefCall = FimscriptParser.parseExpression("dict.len()")
    assertTrue(funcrefCall is FuncrefCallExpression)
    assertEquals(
      OneElementSublistExpression(CurlyBracesName(listOf(SimpleExpression("len"))), Variable(null, "dict")),
      funcrefCall.expression
    )
    assertEquals(0, funcrefCall.args.size)
  }

  @Test
  fun `test inner dictionary funcref call`() {
    val funcrefCall = FimscriptParser.parseExpression("dict.innerDict.len()")
    assertTrue(funcrefCall is FuncrefCallExpression)
    assertEquals(
      OneElementSublistExpression(
        CurlyBracesName(listOf(SimpleExpression("len"))),
        OneElementSublistExpression(SimpleExpression("innerDict"), Variable(null, "dict"))
      ),
      funcrefCall.expression
    )
    assertEquals(0, funcrefCall.args.size)
  }

  @Test
  fun `test dictionary funcref call with args`() {
    val funcrefCall = FimscriptParser.parseExpression("dict.len(a, 5 + 10)")
    assertTrue(funcrefCall is FuncrefCallExpression)
    assertEquals(
      OneElementSublistExpression(CurlyBracesName(listOf(SimpleExpression("len"))), Variable(null, "dict")),
      funcrefCall.expression
    )
    assertEquals(2, funcrefCall.args.size)
    assertEquals(Variable(null, "a"), funcrefCall.args[0])
    assertEquals(BinExpression(SimpleExpression(5), SimpleExpression(10), BinaryOperator.ADDITION), funcrefCall.args[1])
  }
}
