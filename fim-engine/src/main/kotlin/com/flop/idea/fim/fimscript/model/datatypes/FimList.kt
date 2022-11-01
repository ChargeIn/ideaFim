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

import com.flop.idea.fim.ex.ExException

data class FimList(val values: MutableList<FimDataType>) : FimDataType() {

  operator fun get(index: Int): FimDataType = this.values[index]

  override fun asDouble(): Double {
    throw ExException("E745: Using a List as a Number")
  }

  override fun asString(): String {
    throw ExException("E730: Using a List as a String")
  }

  override fun toFimNumber(): FimInt {
    throw ExException("E745: Using a List as a Number")
  }

  override fun toString(): String {
    val result = StringBuffer("[")
    result.append(values.joinToString(separator = ", ") { if (it is FimString) "'$it'" else it.toString() })
    result.append("]")
    return result.toString()
  }

  override fun asBoolean(): Boolean {
    throw ExException("E745: Using a List as a Number")
  }

  override fun deepCopy(level: Int): FimDataType {
    return if (level > 0) {
      FimList(values.map { it.deepCopy(level - 1) }.toMutableList())
    } else {
      this
    }
  }

  override fun lockVar(depth: Int) {
    this.isLocked = true
    if (depth > 1) {
      for (value in values) {
        value.lockVar(depth - 1)
      }
    }
  }

  override fun unlockVar(depth: Int) {
    this.isLocked = false
    if (depth > 1) {
      for (value in values) {
        value.unlockVar(depth - 1)
      }
    }
  }
}
