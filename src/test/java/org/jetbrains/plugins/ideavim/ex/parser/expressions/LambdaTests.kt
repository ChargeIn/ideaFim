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
import com.flop.idea.fim.fimscript.model.expressions.LambdaExpression
import com.flop.idea.fim.fimscript.model.expressions.LambdaFunctionCallExpression
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.expressions.operators.BinaryOperator
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(Theories::class)
class LambdaTests {

  companion object {
    @JvmStatic
    val values = listOf("", " ") @DataPoints get
  }

  @Theory
  fun `lambda with no args test`(sp1: String, sp2: String, sp3: String) {
    val lambdaExpression = FimscriptParser.parseExpression("{$sp1->$sp2'error'$sp3}") as LambdaExpression
    assertEquals(0, lambdaExpression.args.size)
    assertEquals(SimpleExpression("error"), lambdaExpression.expr)
  }

  @Theory
  fun `lambda with multiple args test`(sp1: String, sp2: String, sp3: String, sp4: String, sp5: String, sp6: String) {
    val lambdaExpression = FimscriptParser.parseExpression("{${sp1}a$sp2,${sp3}b$sp4->${sp5}a+b$sp6}") as LambdaExpression
    assertEquals(listOf("a", "b"), lambdaExpression.args)
    assertEquals(BinExpression(Variable(null, "a"), Variable(null, "b"), BinaryOperator.ADDITION), lambdaExpression.expr)
  }

  @Theory
  fun `lambda function call with no args test`() {
    val functionCall = FimscriptParser.parseExpression("{->'error'}()") as LambdaFunctionCallExpression
    assertEquals(0, functionCall.arguments.size)
    assertEquals(0, functionCall.lambda.args.size)
    assertEquals(SimpleExpression("error"), functionCall.lambda.expr)
  }

  @Theory
  fun `lambda function call with multiple args test`(sp1: String, sp2: String, sp3: String, sp4: String) {
    val functionCall = FimscriptParser.parseExpression("{->'error'}(${sp1}a$sp2,${sp3}b$sp4)") as LambdaFunctionCallExpression
    assertEquals(2, functionCall.arguments.size)
    assertEquals(listOf(Variable(null, "a"), Variable(null, "b")), functionCall.arguments)
    assertEquals(0, functionCall.lambda.args.size)
    assertEquals(SimpleExpression("error"), functionCall.lambda.expr)
  }
}
