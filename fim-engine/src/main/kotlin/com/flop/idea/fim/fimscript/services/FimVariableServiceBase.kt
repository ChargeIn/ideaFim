package com.flop.idea.fim.fimscript.services

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.Key
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.ExecutableContext
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.expressions.Scope
import com.flop.idea.fim.fimscript.model.expressions.Variable
import com.flop.idea.fim.fimscript.model.statements.FunctionDeclaration
import com.flop.idea.fim.fimscript.model.statements.FunctionFlag

abstract class FimVariableServiceBase : VariableService {
  private var globalVariables: MutableMap<String, FimDataType> = mutableMapOf()
  private val windowVariablesKey = Key<MutableMap<String, FimDataType>>("TabVariables")
  private val bufferVariablesKey = Key<MutableMap<String, FimDataType>>("BufferVariables")
  private val tabVariablesKey = Key<MutableMap<String, FimDataType>>("WindowVariables")

  private fun getWindowVariables(editor: FimEditor): MutableMap<String, FimDataType> {
    val storedVariableMap = injector.fimStorageService.getDataFromEditor(editor, windowVariablesKey)
    if (storedVariableMap != null) {
      return storedVariableMap
    }
    val windowVariables = mutableMapOf<String, FimDataType>()
    injector.fimStorageService.putDataToEditor(editor, windowVariablesKey, windowVariables)
    return windowVariables
  }

  private fun getBufferVariables(editor: FimEditor): MutableMap<String, FimDataType> {
    val storedVariableMap = injector.fimStorageService.getDataFromBuffer(editor, bufferVariablesKey)
    if (storedVariableMap != null) {
      return storedVariableMap
    }
    val bufferVariables = mutableMapOf<String, FimDataType>()
    injector.fimStorageService.putDataToBuffer(editor, bufferVariablesKey, bufferVariables)
    return bufferVariables
  }

  private fun getTabVariables(editor: FimEditor): MutableMap<String, FimDataType> {
    val storedVariableMap = injector.fimStorageService.getDataFromTab(editor, tabVariablesKey)
    if (storedVariableMap != null) {
      return storedVariableMap
    }
    val tabVariables = mutableMapOf<String, FimDataType>()
    injector.fimStorageService.putDataToTab(editor, tabVariablesKey, tabVariables)
    return tabVariables
  }

  protected fun getDefaultVariableScope(executable: FimLContext): Scope {
    return when (executable.getExecutableContext(executable)) {
      ExecutableContext.SCRIPT, ExecutableContext.COMMAND_LINE -> Scope.GLOBAL_VARIABLE
      ExecutableContext.FUNCTION -> Scope.LOCAL_VARIABLE
    }
  }

  override fun isVariableLocked(variable: Variable, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): Boolean {
    return getNullableVariableValue(variable, editor, context, fimContext)?.isLocked ?: false
  }

  override fun lockVariable(variable: Variable, depth: Int, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext) {
    val value = getNullableVariableValue(variable, editor, context, fimContext) ?: return
    value.lockOwner = variable
    value.lockVar(depth)
  }

  override fun unlockVariable(variable: Variable, depth: Int, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext) {
    val value = getNullableVariableValue(variable, editor, context, fimContext) ?: return
    value.unlockVar(depth)
  }

  override fun getGlobalVariables(): Map<String, FimDataType> {
    return globalVariables
  }

  override fun storeVariable(variable: Variable, value: FimDataType, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext) {
    val scope = variable.scope ?: getDefaultVariableScope(fimContext)
    val name = variable.name.evaluate(editor, context, fimContext).value
    when (scope) {
      Scope.GLOBAL_VARIABLE -> storeGlobalVariable(name, value)
      Scope.SCRIPT_VARIABLE -> storeScriptVariable(name, value, fimContext)
      Scope.WINDOW_VARIABLE -> storeWindowVariable(name, value, editor)
      Scope.TABPAGE_VARIABLE -> storeTabVariable(name, value, editor)
      Scope.FUNCTION_VARIABLE -> storeFunctionVariable(name, value, fimContext)
      Scope.LOCAL_VARIABLE -> storeLocalVariable(name, value, fimContext)
      Scope.BUFFER_VARIABLE -> storeBufferVariable(name, value, editor)
      Scope.VIM_VARIABLE -> storeFimVariable(name, value, editor, context, fimContext)
    }
  }

  override fun getNullableVariableValue(variable: Variable, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType? {
    val scope = variable.scope ?: getDefaultVariableScope(fimContext)
    val name = variable.name.evaluate(editor, context, fimContext).value
    return when (scope) {
      Scope.GLOBAL_VARIABLE -> getGlobalVariableValue(name)
      Scope.SCRIPT_VARIABLE -> getScriptVariable(name, fimContext)
      Scope.WINDOW_VARIABLE -> getWindowVariable(name, editor)
      Scope.TABPAGE_VARIABLE -> getTabVariable(name, editor)
      Scope.FUNCTION_VARIABLE -> getFunctionVariable(name, fimContext)
      Scope.LOCAL_VARIABLE -> getLocalVariable(name, fimContext)
      Scope.BUFFER_VARIABLE -> getBufferVariable(name, editor)
      Scope.VIM_VARIABLE -> getFimVariable(name, editor, context, fimContext)
    }
  }

  override fun getNonNullVariableValue(variable: Variable, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType {
    return getNullableVariableValue(variable, editor, context, fimContext)
      ?: throw ExException(
        "E121: Undefined variable: " +
          (if (variable.scope != null) variable.scope.c + ":" else "") +
          variable.name.evaluate(editor, context, fimContext).value
      )
  }

  override fun getGlobalVariableValue(name: String): FimDataType? {
    return globalVariables[name]
  }

  protected open fun getScriptVariable(name: String, fimContext: FimLContext): FimDataType? {
    val script = fimContext.getScript() ?: throw ExException("E121: Undefined variable: s:$name")
    return script.scriptVariables[name]
  }

  protected open fun getWindowVariable(name: String, editor: FimEditor): FimDataType? {
    return getWindowVariables(editor)[name]
  }

  protected open fun getTabVariable(name: String, editor: FimEditor): FimDataType? {
    return getTabVariables(editor)[name]
  }

  protected open fun getFunctionVariable(name: String, fimContext: FimLContext): FimDataType? {
    val visibleVariables = mutableListOf<Map<String, FimDataType>>()
    var node: FimLContext = fimContext
    while (!node.isFirstParentContext()) {
      if (node is FunctionDeclaration) {
        visibleVariables.add(node.functionVariables)
        if (!node.flags.contains(FunctionFlag.CLOSURE)) {
          break
        }
      }
      node = node.getPreviousParentContext()
    }

    visibleVariables.reverse()
    val functionVariablesMap = mutableMapOf<String, FimDataType>()
    for (map in visibleVariables) {
      functionVariablesMap.putAll(map)
    }
    return functionVariablesMap[name]
  }

  protected open fun getLocalVariable(name: String, fimContext: FimLContext): FimDataType? {
    val visibleVariables = mutableListOf<Map<String, FimDataType>>()
    var node: FimLContext = fimContext
    while (!node.isFirstParentContext()) {
      if (node is FunctionDeclaration) {
        visibleVariables.add(node.localVariables)
        if (!node.flags.contains(FunctionFlag.CLOSURE)) {
          break
        }
      }
      node = node.getPreviousParentContext()
    }

    visibleVariables.reverse()
    val localVariablesMap = mutableMapOf<String, FimDataType>()
    for (map in visibleVariables) {
      localVariablesMap.putAll(map)
    }
    return localVariablesMap[name]
  }

  protected open fun getBufferVariable(name: String, editor: FimEditor): FimDataType? {
    return getBufferVariables(editor)[name]
  }

  protected open fun getFimVariable(name: String, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType? {
    throw ExException("The 'v:' scope is not implemented yet :(")
  }

  protected open fun storeGlobalVariable(name: String, value: FimDataType) {
    globalVariables[name] = value
  }

  protected open fun storeScriptVariable(name: String, value: FimDataType, fimContext: FimLContext) {
    val script = fimContext.getScript() ?: throw ExException("E461: Illegal variable name: s:$name")
    script.scriptVariables[name] = value
  }

  protected open fun storeWindowVariable(name: String, value: FimDataType, editor: FimEditor) {
    getWindowVariables(editor)[name] = value
  }

  protected open fun storeTabVariable(name: String, value: FimDataType, editor: FimEditor) {
    getTabVariables(editor)[name] = value
  }

  protected open fun storeFunctionVariable(name: String, value: FimDataType, fimContext: FimLContext) {
    var node: FimLContext = fimContext
    while (!(node.isFirstParentContext() || node is FunctionDeclaration)) {
      node = node.getPreviousParentContext()
    }

    if (node is FunctionDeclaration) {
      node.functionVariables[name] = value
    } else {
      throw ExException("E461: Illegal variable name: a:$name")
    }
  }

  protected open fun storeLocalVariable(name: String, value: FimDataType, fimContext: FimLContext) {
    var node: FimLContext = fimContext
    while (!(node.isFirstParentContext() || node is FunctionDeclaration)) {
      node = node.getPreviousParentContext()
    }
    if (node is FunctionDeclaration) {
      node.localVariables[name] = value
    } else {
      throw ExException("E461: Illegal variable name: l:$name")
    }
  }

  protected open fun storeBufferVariable(name: String, value: FimDataType, editor: FimEditor) {
    getBufferVariables(editor)[name] = value
  }

  protected open fun storeFimVariable(name: String, value: FimDataType, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext) {
    throw ExException("The 'v:' scope is not implemented yet :(")
  }

  override fun clear() {
    globalVariables.clear()
  }
}
