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

import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.TernaryExpression
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(Theories::class)
class TernaryExpressionTests {

  companion object {
    @JvmStatic
    val values = listOf("", " ") @DataPoints get
  }

  @Theory
  fun `simple expression`(sp1: String, sp2: String, sp3: String, sp4: String, sp5: String, sp6: String, sp7: String, sp8: String) {
    val expression = FimscriptParser.parseExpression("1$sp1?$sp2'2'$sp3:3")
    assertTrue(expression is TernaryExpression)
    assertEquals(SimpleExpression(1), expression.condition)
    assertEquals(SimpleExpression("2"), expression.then)
    assertEquals(SimpleExpression(3), expression.otherwise)
  }
}
