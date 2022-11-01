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

import com.flop.idea.fim.fimscript.model.Executable
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.OneElementSublistExpression
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.SimpleExpression
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.statements.AnonymousFunctionDeclaration
import com.flop.idea.fim.fimscript.model.statements.CatchBlock
import com.flop.idea.fim.fimscript.model.statements.FinallyBlock
import com.flop.idea.fim.fimscript.model.statements.FinishStatement
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag
import com.flop.idea.fim.fimscript.model.statements.IfStatement
import com.flop.idea.fim.fimscript.model.statements.ReturnStatement
import com.flop.idea.fim.fimscript.model.statements.ThrowStatement
import com.flop.idea.fim.fimscript.model.statements.TryBlock
import com.flop.idea.fim.fimscript.model.statements.TryStatement
import com.flop.idea.fim.fimscript.model.statements.loops.BreakStatement
import com.flop.idea.fim.fimscript.model.statements.loops.ContinueStatement
import com.flop.idea.fim.fimscript.model.statements.loops.ForLoop
import com.flop.idea.fim.fimscript.model.statements.loops.ForLoopWithList
import com.flop.idea.fim.fimscript.model.statements.loops.WhileLoop
import com.flop.idea.fim.fimscript.parser.generated.FimscriptBaseVisitor
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser

object ExecutableVisitor : FimscriptBaseVisitor<Executable>() {

  override fun visitBlockMember(ctx: FimscriptParser.BlockMemberContext): Executable? {
    return when {
      ctx.command() != null -> CommandVisitor.visit(ctx.command())
      ctx.breakStatement() != null -> BreakStatement
      ctx.continueStatement() != null -> ContinueStatement
      ctx.finishStatement() != null -> FinishStatement
      ctx.returnStatement() != null -> visitReturnStatement(ctx.returnStatement())
      ctx.ifStatement() != null -> visitIfStatement(ctx.ifStatement())
      ctx.forLoop() != null -> visitForLoop(ctx.forLoop())
      ctx.whileLoop() != null -> visitWhileLoop(ctx.whileLoop())
      ctx.functionDefinition() != null -> visitFunctionDefinition(ctx.functionDefinition())
      ctx.throwStatement() != null -> visitThrowStatement(ctx.throwStatement())
      ctx.tryStatement() != null -> visitTryStatement(ctx.tryStatement())
      else -> null
    }
  }

  override fun visitWhileLoop(ctx: FimscriptParser.WhileLoopContext): Executable {
    val condition: Expression = ExpressionVisitor.visit(ctx.expr())
    val body: List<Executable> = ctx.blockMember().mapNotNull { visitBlockMember(it) }
    return WhileLoop(condition, body)
  }

  override fun visitForLoop(ctx: FimscriptParser.ForLoopContext): Executable {
    val iterable = ExpressionVisitor.visit(ctx.expr())
    val body = ctx.blockMember().mapNotNull { visitBlockMember(it) }
    return if (ctx.argumentsDeclaration() == null) {
      val variable = Variable(Scope.getByValue(ctx.variableScope()?.text ?: ""), ctx.variableName().text)
      ForLoop(variable, iterable, body)
    } else {
      val variables = ctx.argumentsDeclaration().variableName().map { it.text }
      ForLoopWithList(variables, iterable, body)
    }
  }

  override fun visitFunctionDefinition(ctx: FimscriptParser.FunctionDefinitionContext): Executable {
    val functionScope = if (ctx.functionScope() != null) Scope.getByValue(ctx.functionScope().text) else null
    val args = ctx.argumentsDeclaration().variableName().map { it.text }
    val defaultArgs = ctx.argumentsDeclaration().defaultValue()
      .map { Pair<String, Expression>(it.variableName().text, ExpressionVisitor.visit(it.expr())) }
    val body = ctx.blockMember().mapNotNull { visitBlockMember(it) }
    val replaceExisting = ctx.replace != null
    val flags = mutableSetOf<FunctionFlag?>()
    val hasOptionalArguments = ctx.argumentsDeclaration().ETC() != null
    for (flag in ctx.functionFlag()) {
      flags.add(FunctionFlag.getByName(flag.text))
    }
    return if (ctx.functionName() != null) {
      val functionName = ctx.functionName().text
      FunctionDeclaration(functionScope, functionName, args, defaultArgs, body, replaceExisting, flags.filterNotNull().toSet(), hasOptionalArguments)
    } else {
      var sublistExpression = OneElementSublistExpression(SimpleExpression(ctx.literalDictionaryKey(1).text), Variable(functionScope, ctx.literalDictionaryKey(0).text))
      for (i in 2 until ctx.literalDictionaryKey().size) {
        sublistExpression = OneElementSublistExpression(SimpleExpression(ctx.literalDictionaryKey(i).text), sublistExpression)
      }
      AnonymousFunctionDeclaration(sublistExpression, args, defaultArgs, body, replaceExisting, flags.filterNotNull().toSet(), hasOptionalArguments)
    }
  }

  override fun visitTryStatement(ctx: FimscriptParser.TryStatementContext): Executable {
    val tryBlock = TryBlock(ctx.tryBlock().blockMember().mapNotNull { visitBlockMember(it) })
    val catchBlocks: MutableList<CatchBlock> = mutableListOf()
    for (catchBlock in ctx.catchBlock()) {
      catchBlocks.add(
        CatchBlock(
          catchBlock.pattern()?.patternBody()?.text ?: ".",
          catchBlock.blockMember().mapNotNull { visitBlockMember(it) }
        )
      )
    }
    var finallyBlock: FinallyBlock? = null
    if (ctx.finallyBlock() != null) {
      finallyBlock = FinallyBlock(ctx.finallyBlock().blockMember().mapNotNull { visitBlockMember(it) })
    }
    return TryStatement(tryBlock, catchBlocks, finallyBlock)
  }

  override fun visitReturnStatement(ctx: FimscriptParser.ReturnStatementContext): Executable {
    val expression: Expression = ctx.expr()?.let { ExpressionVisitor.visit(ctx.expr()) } ?: SimpleExpression(0)
    return ReturnStatement(expression)
  }

  override fun visitThrowStatement(ctx: FimscriptParser.ThrowStatementContext): Executable {
    val expression: Expression = ExpressionVisitor.visit(ctx.expr())
    return ThrowStatement(expression)
  }

  override fun visitIfStatement(ctx: FimscriptParser.IfStatementContext): Executable {
    val conditionToBody: MutableList<Pair<Expression, List<Executable>>> = mutableListOf()
    conditionToBody.add(
      ExpressionVisitor.visit(ctx.ifBlock().expr()) to ctx.ifBlock().blockMember()
        .mapNotNull { visitBlockMember(it) }
    )
    if (ctx.elifBlock() != null) {
      conditionToBody.addAll(
        ctx.elifBlock().map {
          ExpressionVisitor.visit(it.expr()) to it.blockMember().mapNotNull { it2 -> visitBlockMember(it2) }
        }
      )
    }
    if (ctx.elseBlock() != null) {
      conditionToBody.add(
        SimpleExpression(1) to ctx.elseBlock().blockMember()
          .mapNotNull { visitBlockMember(it) }
      )
    }
    return IfStatement(conditionToBody)
  }
}
