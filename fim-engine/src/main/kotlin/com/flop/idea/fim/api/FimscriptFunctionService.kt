package com.flop.idea.fim.api

import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration

interface FimscriptFunctionService {

  fun deleteFunction(name: String, scope: Scope? = null, fimContext: FimLContext)
  fun storeFunction(declaration: FunctionDeclaration)
  fun getFunctionHandler(scope: Scope?, name: String, fimContext: FimLContext): FunctionHandler
  fun getFunctionHandlerOrNull(scope: Scope?, name: String, fimContext: FimLContext): FunctionHandler?
  fun getUserDefinedFunction(scope: Scope?, name: String, fimContext: FimLContext): FunctionDeclaration?
  fun getBuiltInFunction(name: String): FunctionHandler?
  fun registerHandlers()
  fun addHandler(handlerHolder: Any)
}
