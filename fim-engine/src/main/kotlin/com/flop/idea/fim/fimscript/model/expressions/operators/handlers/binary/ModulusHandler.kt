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

package com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary

import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import com.flop.idea.fim.fimscript.model.datatypes.FimInt

object ModulusHandler : BinaryOperatorHandler() {

  private fun modulus(l: Int, r: Int): Int {
    return if (r == 0) 0 else l % r
  }

  override fun performOperation(left: FimDataType, right: FimDataType): FimDataType {
    if (left is FimFloat || right is FimFloat) {
      throw ExException("E804: Connot use '%' with Float")
    } else {
      return FimInt(modulus(left.asDouble().toInt(), right.asDouble().toInt()))
    }
  }
}
