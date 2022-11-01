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

package com.flop.idea.fim.fimscript.parser.visitors

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.expressions.BinExpression
import com.flop.idea.fim.fimscript.model.expressions.CurlyBracesName
import com.flop.idea.fim.fimscript.model.expressions.DictionaryExpression
import com.flop.idea.fim.fimscript.model.expressions.EnvVariableExpression
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.FalsyExpression
import com.flop.idea.fim.fimscript.model.expressions.FuncrefCallExpression
import com.flop.idea.fim.fimscript.model.expressions.FunctionCallExpression
import com.flop.idea.fim.fimscript.model.expressions.LambdaExpression
import com.flop.idea.fim.fimscript.model.expressions.LambdaFunctionCallExpression
import com.flop.idea.fim.fimscript.model.expressions.ListExpression
import com.flop.idea.fim.fimscript.model.expressions.OneElementSublistExpression
import com.flop.idea.fim.fimscript.model.expressions.OptionExpression
import com.flop.idea.fim.fimscript.model.expressions.Register
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.ScopeExpression
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.SublistExpression
import com.flop.idea.fim.fimscript.model.expressions.TernaryExpression
import com.flop.idea.fim.fimscript.model.expressions.UnaryExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.expressions.operators.BinaryOperator
import com.flop.idea.fim.fimscript.model.expressions.operators.UnaryOperator
import com.flop.idea.fim.fimscript.parser.generated.FimscriptBaseVisitor
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.BlobExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.DictionaryExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.EnvVariableExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.FalsyExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.FloatExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.FunctionCallExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.IntExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.ListExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.LiteralDictionaryExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.OneElementSublistExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.OptionExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.RegisterExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.StringExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.SublistExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.TernaryExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.UnaryExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.VariableContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.VariableExpressionContext
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser.WrappedExpressionContext
import org.antlr.v4.runtime.ParserRuleContext

object ExpressionVisitor : FimscriptBaseVisitor<Expression>() {

  override fun visitDictionaryExpression(ctx: DictionaryExpressionContext): Expression {
    val dict: LinkedHashMap<Expression, Expression> = LinkedHashMap()
    for (dictEntry in ctx.dictionary().dictionaryEntry()) {
      dict[visit(dictEntry.expr(0))] = visit(dictEntry.expr(1))
    }
    val result = DictionaryExpression(dict)
    result.originalString = ctx.text
    return result
  }

  override fun visitLiteralDictionaryExpression(ctx: LiteralDictionaryExpressionContext): Expression {
    val dict: LinkedHashMap<Expression, Expression> = LinkedHashMap()
    for (dictEntry in ctx.literalDictionary().literalDictionaryEntry()) {
      dict[SimpleExpression(dictEntry.literalDictionaryKey().text)] = visit(dictEntry.expr())
    }
    val result = DictionaryExpression(dict)
    result.originalString = ctx.text
    return result
  }

  override fun visitIntExpression(ctx: IntExpressionContext): Expression {
    val result = SimpleExpression(FimInt(ctx.text))
    result.originalString = ctx.text
    return result
  }

  override fun visitStringExpression(ctx: StringExpressionContext): Expression {
    var text = ctx.text
    val firstSymbol = text[0]
    if (firstSymbol == '"') {
      text = injector.parser.parseFimScriptString(text.substring(1, text.length - 1))
    } else if (firstSymbol == '\'') {
      text = text
        .substring(1, text.length - 1)
        .replace("''", "'")
    }
    val result = SimpleExpression(text)
    result.originalString = ctx.text
    return result
  }

  override fun visitListExpression(ctx: ListExpressionContext): Expression {
    val result = ListExpression((ctx.list().expr().map { visit(it) }.toMutableList()))
    result.originalString = ctx.text
    return result
  }

  override fun visitBinExpression1(ctx: FimscriptParser.BinExpression1Context): Expression {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    val operatorString = ctx.binaryOperator1().text
    val operator = BinaryOperator.getByValue(operatorString) ?: throw RuntimeException()
    val result = BinExpression(left, right, operator)
    result.originalString = ctx.text
    return result
  }

  override fun visitBinExpression2(ctx: FimscriptParser.BinExpression2Context): Expression {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    val operatorString = ctx.binaryOperator2().text
    val result = if (operatorString == "." && !containsSpaces(ctx) && evaluationResultCouldBeADictionary(left) && matchesLiteralDictionaryKey(ctx.expr(1).text)) {
      val index = SimpleExpression(ctx.expr(1).text)
      OneElementSublistExpression(index, left)
    } else if (operatorString == "-" && left is OneElementSublistExpression && !containsSpaces(ctx) && matchesLiteralDictionaryKey(
        ctx.expr(1).text
      )
    ) {
      val postfix = "-" + ctx.expr(1).text
      val newIndex = SimpleExpression((left.index as SimpleExpression).data.asString() + postfix)
      OneElementSublistExpression(newIndex, left.expression)
    } else if (operatorString == "." && !containsSpaces(ctx) && evaluationResultCouldBeADictionary(left) && right is OneElementSublistExpression && matchesLiteralDictionaryKey(right.expression.originalString)) {
      OneElementSublistExpression(right.index, OneElementSublistExpression(SimpleExpression(right.expression.originalString), left))
    } else if (operatorString == "." && !containsSpaces(ctx) && right is FunctionCallExpression && evaluationResultCouldBeADictionary(left)) {
      val index = right.functionName
      FuncrefCallExpression(OneElementSublistExpression(index, left), right.arguments)
    } else {
      val operator = BinaryOperator.getByValue(operatorString) ?: throw RuntimeException()
      BinExpression(left, right, operator)
    }
    result.originalString = ctx.text
    return result
  }

  private fun containsSpaces(ctx: ParserRuleContext): Boolean {
    for (child in ctx.children) {
      if (child.text.isBlank()) return true
    }
    return false
  }

  private fun matchesLiteralDictionaryKey(string: String): Boolean {
    return string.matches(Regex("[a-zA-Z0-9_-]+"))
  }

  private fun evaluationResultCouldBeADictionary(ctx: Expression): Boolean {
    return when (ctx) {
      is ListExpression, is UnaryExpression -> false
      is SimpleExpression -> ctx.data is FimDictionary
      else -> true
    }
  }

  override fun visitBinExpression3(ctx: FimscriptParser.BinExpression3Context): Expression {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    val operatorString = ctx.binaryOperator3().text
    val operator = BinaryOperator.getByValue(operatorString) ?: throw RuntimeException()
    val result = BinExpression(left, right, operator)
    result.originalString = ctx.text
    return result
  }

  override fun visitBinExpression4(ctx: FimscriptParser.BinExpression4Context): Expression {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    val operatorString = ctx.binaryOperator4().text
    val operator = BinaryOperator.getByValue(operatorString) ?: throw RuntimeException()
    val result = BinExpression(left, right, operator)
    result.originalString = ctx.text
    return result
  }

  override fun visitBinExpression5(ctx: FimscriptParser.BinExpression5Context): Expression {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    val operatorString = ctx.binaryOperator5().text
    val operator = BinaryOperator.getByValue(operatorString) ?: throw RuntimeException()
    val result = BinExpression(left, right, operator)
    result.originalString = ctx.text
    return result
  }

  override fun visitUnaryExpression(ctx: UnaryExpressionContext): Expression {
    val expression = visit(ctx.expr())
    val operator = UnaryOperator.getByValue(ctx.getChild(0).text)
    val result = UnaryExpression(operator, expression)
    result.originalString = ctx.text
    return result
  }

  override fun visitFloatExpression(ctx: FloatExpressionContext): Expression {
    val result = SimpleExpression(ctx.unsignedFloat().text.toDouble())
    result.originalString = ctx.text
    return result
  }

  override fun visitVariableExpression(ctx: VariableExpressionContext): Expression {
    val result = visitVariable(ctx.variable())
    result.originalString = ctx.text
    return result
  }

  override fun visitWrappedExpression(ctx: WrappedExpressionContext): Expression? {
    val result = visit(ctx.expr())
    result.originalString = ctx.text
    return result
  }

  override fun visitOptionExpression(ctx: OptionExpressionContext): Expression {
    val result = OptionExpression(Scope.getByValue(ctx.option()?.text ?: "") ?: Scope.GLOBAL_VARIABLE, ctx.option().optionName().text)
    result.originalString = ctx.text
    return result
  }

  override fun visitTernaryExpression(ctx: TernaryExpressionContext): Expression {
    val condition = visit(ctx.expr(0))
    val then = visit(ctx.expr(1))
    val otherwise = visit(ctx.expr(2))
    val result = TernaryExpression(condition, then, otherwise)
    result.originalString = ctx.text
    return result
  }

  override fun visitFunctionAsMethodCall1(ctx: FimscriptParser.FunctionAsMethodCall1Context): FunctionCallExpression {
    val functionCall = visitFunctionCall(ctx.functionCall())
    functionCall.arguments.add(0, visit(ctx.expr()))
    functionCall.originalString = ctx.text
    return functionCall
  }

  override fun visitFunctionAsMethodCall2(ctx: FimscriptParser.FunctionAsMethodCall2Context): LambdaFunctionCallExpression {
    val lambda = visitLambda(ctx.lambda())
    val arguments = mutableListOf(visit(ctx.expr()))
    arguments.addAll(visitFunctionArgs(ctx.functionArguments()))
    val result = LambdaFunctionCallExpression(lambda, arguments)
    result.originalString = ctx.text
    return result
  }

  override fun visitFunctionCallExpression(ctx: FunctionCallExpressionContext): Expression {
    val result = visitFunctionCall(ctx.functionCall())
    result.originalString = ctx.text
    return result
  }

  override fun visitFunctionCall(ctx: FimscriptParser.FunctionCallContext): FunctionCallExpression {
    val functionName = visitCurlyBracesName(ctx.functionName().curlyBracesName())
    var scope: Scope? = null
    if (ctx.functionScope() != null) {
      scope = Scope.getByValue(ctx.functionScope().text)
    }
    val functionArguments = visitFunctionArgs(ctx.functionArguments()).toMutableList()
    val result = FunctionCallExpression(scope, functionName, functionArguments)
    result.originalString = ctx.text
    return result
  }

  override fun visitLambdaFunctionCallExpression(ctx: FimscriptParser.LambdaFunctionCallExpressionContext): LambdaFunctionCallExpression {
    val lambda = visitLambda(ctx.lambda())
    val arguments = visitFunctionArgs(ctx.functionArguments())
    val result = LambdaFunctionCallExpression(lambda, arguments)
    result.originalString = ctx.text
    return result
  }

  private fun visitFunctionArgs(args: FimscriptParser.FunctionArgumentsContext): List<Expression> {
    val result = mutableListOf<Expression>()
    for (arg in args.functionArgument()) {
      if (arg.anyScope() != null) {
        result.add(ScopeExpression(Scope.getByValue(arg.anyScope().text)!!))
      } else if (arg.expr() != null) {
        result.add(visit(arg.expr()))
      }
    }
    return result
  }

  override fun visitLambdaExpression(ctx: FimscriptParser.LambdaExpressionContext): Expression {
    val result = super.visitLambdaExpression(ctx)
    result.originalString = ctx.text
    return result
  }

  override fun visitLambda(ctx: FimscriptParser.LambdaContext): LambdaExpression {
    val arguments = ctx.argumentsDeclaration().variableName().map { it.text }
    val expr = visit(ctx.expr())
    val result = LambdaExpression(arguments, expr)
    result.originalString = ctx.text
    return result
  }

  override fun visitSublistExpression(ctx: SublistExpressionContext): Expression {
    val ex = visit(ctx.expr(0))
    val from = if (ctx.from != null) visit(ctx.from) else null
    val to = if (ctx.to != null) visit(ctx.to) else null
    val result = SublistExpression(from, to, ex)
    result.originalString = ctx.text
    return result
  }

  override fun visitOneElementSublistExpression(ctx: OneElementSublistExpressionContext): Expression {
    val ex = visit(ctx.expr(0))
    val fromTo = visit(ctx.expr(1))
    val result = OneElementSublistExpression(fromTo, ex)
    result.originalString = ctx.text
    return result
  }

  override fun visitEnvVariableExpression(ctx: EnvVariableExpressionContext): Expression {
    val result = EnvVariableExpression(ctx.envVariable().envVariableName().text)
    result.originalString = ctx.text
    return result
  }

  override fun visitRegisterExpression(ctx: RegisterExpressionContext): Expression {
    val result = Register(ctx.text.replaceFirst("@", "")[0])
    result.originalString = ctx.text
    return result
  }

  override fun visitVariable(ctx: VariableContext): Variable {
    val scope = if (ctx.variableScope() == null) null else Scope.getByValue(ctx.variableScope().text)
    val result = Variable(scope, visitCurlyBracesName(ctx.variableName().curlyBracesName()))
    result.originalString = ctx.text
    return result
  }

  override fun visitFalsyExpression(ctx: FalsyExpressionContext): Expression {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    val result = FalsyExpression(left, right)
    result.originalString = ctx.text
    return result
  }

  override fun visitCurlyBracesName(ctx: FimscriptParser.CurlyBracesNameContext): CurlyBracesName {
    val parts = ctx.element().map { if (it.expr() != null) visit(it.expr()) else SimpleExpression(it.text) }
    val result = CurlyBracesName(parts)
    result.originalString = ctx.text
    return result
  }

  override fun visitBlobExpression(ctx: BlobExpressionContext?): Expression {
    TODO()
  }
}
