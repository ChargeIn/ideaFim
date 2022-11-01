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

package com.flop.idea.fim.fimscript.model.datatypes

abstract class FimDataType {

  abstract fun asDouble(): Double

  // string value that is used in arithmetic expressions (concatenation etc.)
  abstract fun asString(): String

  abstract fun toFimNumber(): FimInt

  // string value that is used in echo-like commands
  override fun toString(): String {
    throw NotImplementedError("implement me :(")
  }

  open fun asBoolean(): Boolean {
    return asDouble() != 0.0
  }

  abstract fun deepCopy(level: Int = 100): FimDataType

  var lockOwner: Any? = null
  var isLocked: Boolean = false

  abstract fun lockVar(depth: Int)
  abstract fun unlockVar(depth: Int)

  // use in cases when FimDataType's value should be inserted into document
  // e.g. expression register or substitute with expression
  fun toInsertableString(): String {
    return when (this) {
      is FimList -> {
        this.values.joinToString(separator = "") { it.toString() + "\n" }
      }
      is FimDictionary -> this.asString()
      else -> this.toString()
    }
  }
}
