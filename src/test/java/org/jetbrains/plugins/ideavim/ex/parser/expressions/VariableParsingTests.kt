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

import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VariableParsingTests {

  @Test
  fun variableTest() {
    val variable = FimscriptParser.parseExpression("variableName")
    assertTrue(variable is Variable)
    assertTrue(variable.scope == null)
    assertEquals("variableName", variable.name.evaluate().asString())
  }

  @Test
  fun variableTest2() {
    val variable = FimscriptParser.parseExpression("t:variableName")
    assertTrue(variable is Variable)
    assertEquals(Scope.TABPAGE_VARIABLE, variable.scope)
    assertEquals("variableName", variable.name.evaluate().asString())
  }
}
