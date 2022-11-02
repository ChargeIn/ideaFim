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

package com.flop.idea.fim.fimscript.services

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointName
import com.flop.idea.fim.api.FimscriptFunctionService
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.CommandLineFimLContext
import com.flop.idea.fim.fimscript.model.Script
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.functions.DefinedFunctionHandler
import com.flop.idea.fim.fimscript.model.functions.FunctionBeanClass
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration

object FunctionStorage : FimscriptFunctionService {

  private val logger = logger<FunctionStorage>()

  private val globalFunctions: MutableMap<String, FunctionDeclaration> = mutableMapOf()

  private val extensionPoint = ExtensionPointName.create<FunctionBeanClass>("IdeaFIM.fimLibraryFunction")
  private val builtInFunctions: MutableMap<String, FunctionHandler> = mutableMapOf()

  override fun deleteFunction(name: String, scope: Scope?, fimContext: FimLContext) {
    if (name[0].isLowerCase() && scope != Scope.SCRIPT_VARIABLE) {
      throw ExException("E128: Function name must start with a capital or \"s:\": $name")
    }

    if (scope != null)
      when (scope) {
        Scope.GLOBAL_VARIABLE -> {
          if (globalFunctions.containsKey(name)) {
            globalFunctions[name]!!.isDeleted = true
            globalFunctions.remove(name)
            return
          } else {
            throw ExException("E130: Unknown function: ${scope.c}:$name")
          }
        }
        Scope.SCRIPT_VARIABLE -> {
          if (fimContext.getFirstParentContext() !is Script) {
            throw ExException("E81: Using <SID> not in a script context")
          }

          if (getScriptFunction(name, fimContext) != null) {
            deleteScriptFunction(name, fimContext)
            return
          } else {
            throw ExException("E130: Unknown function: ${scope.c}:$name")
          }
        }
        else -> throw ExException("E130: Unknown function: ${scope.c}:$name")
      }

    if (globalFunctions.containsKey(name)) {
      globalFunctions[name]!!.isDeleted = true
      globalFunctions.remove(name)
      return
    }

    val firstParentContext = fimContext.getFirstParentContext()
    if (firstParentContext is Script && getScriptFunction(name, fimContext) != null) {
      deleteScriptFunction(name, fimContext)
      return
    }
    throw ExException("E130: Unknown function: $name")
  }

  override fun storeFunction(declaration: FunctionDeclaration) {
    val scope: Scope = declaration.scope ?: getDefaultFunctionScope()
    when (scope) {
      Scope.GLOBAL_VARIABLE -> {
        if (globalFunctions.containsKey(declaration.name) && !declaration.replaceExisting) {
          throw ExException("E122: Function ${declaration.name} already exists, add ! to replace it")
        } else {
          globalFunctions[declaration.name] = declaration
        }
      }
      Scope.SCRIPT_VARIABLE -> {
        if (declaration.getFirstParentContext() !is Script) {
          throw ExException("E81: Using <SID> not in a script context")
        }

        if (getScriptFunction(declaration.name, declaration) != null && !declaration.replaceExisting) {
          throw ExException("E122: Function ${declaration.name} already exists, add ! to replace it")
        } else {
          storeScriptFunction(declaration)
        }
      }
      else -> throw ExException("E884: Function name cannot contain a colon: ${scope.c}:${declaration.name}")
    }
  }

  override fun getFunctionHandler(scope: Scope?, name: String, fimContext: FimLContext): FunctionHandler {
    return getFunctionHandlerOrNull(scope, name, fimContext)
      ?: throw ExException("E117: Unknown function: ${scope?.toString() ?: ""}$name")
  }

  override fun getFunctionHandlerOrNull(scope: Scope?, name: String, fimContext: FimLContext): FunctionHandler? {
    if (scope == null) {
      val builtInFunction = getBuiltInFunction(name)
      if (builtInFunction != null) {
        return builtInFunction
      }
    }

    val definedFunction = getUserDefinedFunction(scope, name, fimContext)
    if (definedFunction != null) {
      return DefinedFunctionHandler(definedFunction)
    }
    return null
  }

  override fun getUserDefinedFunction(scope: Scope?, name: String, fimContext: FimLContext): FunctionDeclaration? {
    return when (scope) {
      Scope.GLOBAL_VARIABLE -> globalFunctions[name]
      Scope.SCRIPT_VARIABLE -> getScriptFunction(name, fimContext)
      null -> {
        val firstParentContext = fimContext.getFirstParentContext()
        when (firstParentContext) {
          is CommandLineFimLContext -> globalFunctions[name]
          is Script -> globalFunctions[name] ?: getScriptFunction(name, fimContext)
          else -> throw RuntimeException("Unknown parent context")
        }
      }
      else -> null
    }
  }

  override fun getBuiltInFunction(name: String): FunctionHandler? {
    return builtInFunctions[name]
  }

  private fun storeScriptFunction(functionDeclaration: FunctionDeclaration) {
    val script = functionDeclaration.getScript() ?: throw ExException("E81: Using <SID> not in a script context")
    script.scriptFunctions[functionDeclaration.name] = functionDeclaration
  }

  private fun getScriptFunction(name: String, fimContext: FimLContext): FunctionDeclaration? {
    val script = fimContext.getScript() ?: throw ExException("E120: Using <SID> not in a script context: s:$name")
    return script.scriptFunctions[name]
  }

  private fun deleteScriptFunction(name: String, fimContext: FimLContext) {
    val script = fimContext.getScript() ?: throw ExException("E81: Using <SID> not in a script context")
    if (script.scriptFunctions[name] != null) {
      script.scriptFunctions[name]!!.isDeleted = true
    }
    script.scriptFunctions.remove(name)
  }

  private fun getDefaultFunctionScope(): Scope {
    return Scope.GLOBAL_VARIABLE
  }

  override fun registerHandlers() {
    extensionPoint.extensions().forEach(FunctionBeanClass::register)
  }

  override fun addHandler(handlerHolder: Any) {
    handlerHolder as FunctionBeanClass
    if (handlerHolder.name != null) {
      builtInFunctions[handlerHolder.name!!] = handlerHolder.instance
    } else {
      logger.error("Received function handler with null name")
    }
  }
}
