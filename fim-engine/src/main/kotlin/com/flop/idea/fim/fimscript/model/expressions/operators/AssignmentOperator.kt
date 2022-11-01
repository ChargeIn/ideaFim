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

package com.flop.idea.fim.fimscript.model.expressions.operators

import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.AdditionHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.ConcatenationHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.DivisionHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.ModulusHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.MultiplicationHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.SubtractionHandler

enum class AssignmentOperator(val value: String) {
  ASSIGNMENT("="),
  ADDITION("+="),
  SUBTRACTION("-="),
  MULTIPLICATION("*="),
  DIVISION("/="),
  MODULUS("%="),
  CONCATENATION(".=");

  companion object {
    fun getByValue(value: String): AssignmentOperator {
      return values().first { it.value == value }
    }
  }

  fun getNewValue(left: FimDataType?, right: FimDataType): FimDataType {
    return when (this) {
      ASSIGNMENT -> right
      ADDITION -> {
        // in this case we should update existing list instead of creating a new one
        if (left is FimList && right is FimList) {
          left.values.addAll(right.values)
          left
        } else {
          AdditionHandler.performOperation(left!!, right)
        }
      }
      SUBTRACTION -> SubtractionHandler.performOperation(left!!, right)
      MULTIPLICATION -> MultiplicationHandler.performOperation(left!!, right)
      DIVISION -> DivisionHandler.performOperation(left!!, right)
      MODULUS -> ModulusHandler.performOperation(left!!, right)
      CONCATENATION -> ConcatenationHandler.performOperation(left!!, right)
    }
  }
}
