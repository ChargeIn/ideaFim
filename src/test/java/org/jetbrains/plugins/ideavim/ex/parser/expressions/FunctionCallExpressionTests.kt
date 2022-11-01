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
import com.flop.idea.fim.fimscript.model.expressions.FunctionCallExpression
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.ScopeExpression
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FunctionCallExpressionTests {

  @Test
  fun `function call with no arguments`() {
    val ex = FimscriptParser.parseExpression("doSomething()")
    assertTrue(ex is FunctionCallExpression)
    assertEquals("doSomething", ex.functionName.evaluate().asString())
    assertNull(ex.scope)
    assertEquals(0, ex.arguments.size)
  }

  @Test
  fun `scoped function call`() {
    val ex = FimscriptParser.parseExpression("s:doSomething()")
    assertTrue(ex is FunctionCallExpression)
    assertEquals("doSomething", ex.functionName.evaluate().asString())
    assertNotNull(ex.scope)
    assertEquals(Scope.SCRIPT_VARIABLE, ex.scope)
    assertEquals(0, ex.arguments.size)
  }

  @Test
  fun `function call with simple arguments`() {
    val ex = FimscriptParser.parseExpression("f(0, 'string')")
    assertTrue(ex is FunctionCallExpression)
    assertEquals("f", ex.functionName.evaluate().asString())
    assertNull(ex.scope)
    assertNotNull(ex.arguments)
    assertEquals(2, ex.arguments.size)
    assertEquals(FimInt(0), ex.arguments[0].evaluate())
    assertEquals(FimString("string"), ex.arguments[1].evaluate())
  }

  @Test
  fun `scope as a function call argument`() {
    val ex = FimscriptParser.parseExpression("f(s:, 'string')")
    assertTrue(ex is FunctionCallExpression)
    assertEquals("f", ex.functionName.evaluate().asString())
    assertNull(ex.scope)
    assertNotNull(ex.arguments)
    assertEquals(2, ex.arguments.size)
    assertEquals(ScopeExpression(Scope.SCRIPT_VARIABLE), ex.arguments[0])
    assertEquals(FimString("string"), ex.arguments[1].evaluate())
  }
}
