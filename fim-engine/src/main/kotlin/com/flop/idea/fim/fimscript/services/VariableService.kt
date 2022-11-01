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

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.expressions.Variable
import org.jetbrains.annotations.TestOnly

/**
 * COMPATIBILITY-LAYER: Renamed from FimVariableService
 * Please see: https://jb.gg/zo8n0r
 */
interface VariableService {
  /**
   * Stores variable.
   *
   * The `v:` scope currently is not supported.
   * @param variable variable to store, if it's scope is null, the default scope for fimContext will be chosen
   * @param value variable value
   * @param editor editor
   * @param context execution context
   * @param fimContext fim context
   * @throws ExException("The 'v:' scope is not implemented yet :(")
   */
  fun storeVariable(variable: Variable, value: FimDataType, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext)

  /**
   * Get global scope variable value.
   * @param name variable name
   */
  fun getGlobalVariableValue(name: String): FimDataType?

  /**
   * Gets variable value
   *
   * The `v:` scope currently is not supported
   * @param variable variable, if it's scope is null, the default scope for fimContext will be chosen
   * @param editor editor
   * @param context execution context
   * @param fimContext fim context
   * @throws ExException("The 'v:' scope is not implemented yet :(")
   */
  fun getNullableVariableValue(variable: Variable, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType?

  /**
   * Gets variable value.
   *
   * The `v:` scope currently is not supported.
   * @param variable variable, if it's scope is null, the default scope for fimContext will be chosen
   * @param editor editor
   * @param context execution context
   * @param fimContext fim context
   * @throws ExException("The 'v:' scope is not implemented yet :(")
   * @throws ExException("E121: Undefined variable: ${scope}:${name}")
   */
  fun getNonNullVariableValue(variable: Variable, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): FimDataType

  /**
   * Checks if the variable locked.
   *
   * Returns false if the variable does not exist.
   *
   * See `:h lockvar`.
   * @param variable variable, if it's scope is null, the default scope for fimContext will be chosen
   * @param editor editor
   * @param context execution context
   * @param fimContext fim context
   */
  fun isVariableLocked(variable: Variable, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext): Boolean

  /**
   * Locks variable.
   *
   * See `:h lockvar`.
   * @param variable variable, if it's scope is null, the default scope for fimContext will be chosen
   * @param depth lock depth
   * @param editor editor
   * @param context execution context
   * @param fimContext fim context
   */
  fun lockVariable(variable: Variable, depth: Int, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext)

  /**
   * Unlocks variable.
   *
   * See `:h lockvar`.
   * @param variable variable, if it's scope is null, the default scope for fimContext will be chosen
   * @param depth lock depth
   * @param editor editor
   * @param context execution context
   * @param fimContext fim context
   */
  fun unlockVariable(variable: Variable, depth: Int, editor: FimEditor, context: ExecutionContext, fimContext: FimLContext)

  fun getGlobalVariables(): Map<String, FimDataType>

  /**
   * Clears all global variables
   */
  @TestOnly
  fun clear()
}
