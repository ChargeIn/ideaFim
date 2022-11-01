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

package com.flop.idea.fim.fimscript.model.functions.handlers

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimFuncref
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler
import com.flop.idea.fim.fimscript.services.FunctionStorage

object FunctionFunctionHandler : FunctionHandler() {
  override val name = "function"
  override val minimumNumberOfArguments: Int = 1
  override val maximumNumberOfArguments: Int = 3

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: FimEditor,
    context: ExecutionContext,
    fimContext: FimLContext,
  ): FimFuncref {
    val arg1 = argumentValues[0].evaluate(editor, context, fimContext)
    if (arg1 !is FimString) {
      throw ExException("E129: Function name required")
    }
    val scopeAndName = arg1.value.extractScopeAndName()
    val function = FunctionStorage.getFunctionHandlerOrNull(scopeAndName.first, scopeAndName.second, fimContext)
      ?: throw ExException("E700: Unknown function: ${if (scopeAndName.first != null) scopeAndName.first!!.c + ":" else ""}${scopeAndName.second}")

    var arglist: FimList? = null
    var dictionary: FimDictionary? = null
    val arg2 = argumentValues.getOrNull(1)?.evaluate(editor, context, fimContext)
    val arg3 = argumentValues.getOrNull(2)?.evaluate(editor, context, fimContext)

    if (arg2 is FimDictionary && arg3 is FimDictionary) {
      throw ExException("E923: Second argument of function() must be a list or a dict")
    }

    if (arg2 != null) {
      when (arg2) {
        is FimList -> arglist = arg2
        is FimDictionary -> dictionary = arg2
        else -> throw ExException("E923: Second argument of function() must be a list or a dict")
      }
    }

    if (arg3 != null && arg3 !is FimDictionary) {
      throw ExException("E922: expected a dict")
    }
    val funcref = FimFuncref(function, arglist ?: FimList(mutableListOf()), dictionary, FimFuncref.Type.FUNCTION)
    if (dictionary != null) {
      funcref.isSelfFixed = true
    }
    return funcref
  }
}

object FuncrefFunctionHandler : FunctionHandler() {
  override val name = "function"
  override val minimumNumberOfArguments: Int = 1
  override val maximumNumberOfArguments: Int = 3

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: FimEditor,
    context: ExecutionContext,
    fimContext: FimLContext,
  ): FimFuncref {
    val arg1 = argumentValues[0].evaluate(editor, context, fimContext)
    if (arg1 !is FimString) {
      throw ExException("E129: Function name required")
    }
    val scopeAndName = arg1.value.extractScopeAndName()
    val function = FunctionStorage.getUserDefinedFunction(scopeAndName.first, scopeAndName.second, fimContext)
      ?: throw ExException("E700: Unknown function: ${scopeAndName.first?.toString() ?: ""}${scopeAndName.second}")
    val handler = DefinedFunctionHandler(function)

    var arglist: FimList? = null
    var dictionary: FimDictionary? = null
    val arg2 = argumentValues.getOrNull(1)?.evaluate(editor, context, fimContext)
    val arg3 = argumentValues.getOrNull(2)?.evaluate(editor, context, fimContext)

    if (arg2 is FimDictionary && arg3 is FimDictionary) {
      throw ExException("E923: Second argument of function() must be a list or a dict")
    }

    if (arg2 != null) {
      when (arg2) {
        is FimList -> arglist = arg2
        is FimDictionary -> dictionary = arg2
        else -> throw ExException("E923: Second argument of function() must be a list or a dict")
      }
    }

    if (arg3 != null && arg3 !is FimDictionary) {
      throw ExException("E922: expected a dict")
    }
    return FimFuncref(handler, arglist ?: FimList(mutableListOf()), dictionary, FimFuncref.Type.FUNCREF)
  }
}

private fun String.extractScopeAndName(): Pair<Scope?, String> {
  val colonIndex = this.indexOf(":")
  if (colonIndex == -1) {
    return Pair(null, this)
  }
  val scopeString = this.substring(0, colonIndex)
  val nameString = this.substring(colonIndex + 1)
  return Pair(Scope.getByValue(scopeString), nameString)
}
