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

data class FimDictionary(val dictionary: LinkedHashMap<FimString, FimDataType>) : FimDataType() {

  override fun asDouble(): Double {
    throw ExException("E728: Using a Dictionary as a Number")
  }

  override fun asString(): String {
    throw ExException("E731: Using a Dictionary as a String")
  }

  override fun toFimNumber(): FimInt {
    throw ExException("E728: Using a Dictionary as a Number")
  }

  override fun toString(): String {
    val result = StringBuffer("{")
    result.append(dictionary.map { stringOfEntry(it) }.joinToString(separator = ", "))
    result.append("}")
    return result.toString()
  }

  private fun stringOfEntry(entry: Map.Entry<FimString, FimDataType>): String {
    val valueString = when (entry.value) {
      is FimString -> "'${entry.value}'"
      else -> entry.value.toString()
    }
    return "'${entry.key}': $valueString"
  }

  override fun asBoolean(): Boolean {
    throw ExException("E728: Using a Dictionary as a Number")
  }

  override fun deepCopy(level: Int): FimDictionary {
    return if (level > 0) {
      FimDictionary(linkedMapOf(*(dictionary.map { it.key.copy() to it.value.deepCopy(level - 1) }.toTypedArray())))
    } else {
      this
    }
  }

  override fun lockVar(depth: Int) {
    this.isLocked = true
    if (depth > 1) {
      for (value in dictionary.values) {
        value.lockVar(depth - 1)
      }
    }
  }

  override fun unlockVar(depth: Int) {
    this.isLocked = false
    if (depth > 1) {
      for (value in dictionary.values) {
        value.unlockVar(depth - 1)
      }
    }
  }
}
