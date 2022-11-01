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

package com.flop.idea.fim.options

import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import java.util.*

/**
 * COMPATIBILITY-LAYER: switched from sealed to abstract
 * Please see: https://jb.gg/zo8n0r
 */
/*sealed*/abstract class Option<T : FimDataType>(val name: String, val abbrev: String, private val defaultValue: T) {

  open fun getDefaultValue(): T {
    return defaultValue
  }

  private val listeners = mutableSetOf<OptionChangeListener<FimDataType>>()

  open fun addOptionChangeListener(listener: OptionChangeListener<FimDataType>) {
    listeners.add(listener)
  }

  open fun removeOptionChangeListener(listener: OptionChangeListener<FimDataType>) {
    listeners.remove(listener)
  }

  fun onChanged(scope: OptionScope, oldValue: FimDataType) {
    for (listener in listeners) {
      when (scope) {
        is OptionScope.GLOBAL -> listener.processGlobalValueChange(oldValue)
        is OptionScope.LOCAL -> {
          if (listener is LocalOptionChangeListener) {
            listener.processLocalValueChange(oldValue, scope.editor)
          } else {
            listener.processGlobalValueChange(oldValue)
          }
        }
      }
    }
  }

  /**
   * COMPATIBILITY-LAYER: Method added
   * Please see: https://jb.gg/zo8n0r
   */
  @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
  open val value: java.lang.Boolean
    get() = TODO()

  // todo 1.9 should return Result with exceptions
  abstract fun checkIfValueValid(value: FimDataType, token: String)

  abstract fun getValueIfAppend(currentValue: FimDataType, value: String, token: String): T
  abstract fun getValueIfPrepend(currentValue: FimDataType, value: String, token: String): T
  abstract fun getValueIfRemove(currentValue: FimDataType, value: String, token: String): T
}

open class StringOption(name: String, abbrev: String, defaultValue: FimString, private val isList: Boolean = false, private val boundedValues: Collection<String>? = null) : Option<FimString>(name, abbrev, defaultValue) {
  constructor(name: String, abbrev: String, defaultValue: String, isList: Boolean = false, boundedValues: Collection<String>? = null) : this(name, abbrev, FimString(defaultValue), isList, boundedValues)

  override fun checkIfValueValid(value: FimDataType, token: String) {
    if (value !is FimString) {
      throw ExException("E474: Invalid argument: $token")
    }

    if (value.value.isEmpty()) {
      return
    }

    if (boundedValues != null && split(value.value).any { !boundedValues.contains(it) }) {
      throw ExException("E474: Invalid argument: $token")
    }
  }

  override fun getValueIfAppend(currentValue: FimDataType, value: String, token: String): FimString {
    val currentString = (currentValue as FimString).value
    if (split(currentString).contains(value)) return currentValue

    val builder = StringBuilder(currentString)
    if (currentString.isNotEmpty()) {
      val separator = if (isList) "," else ""
      builder.append(separator)
    }
    builder.append(value)
    return FimString(builder.toString())
  }

  override fun getValueIfPrepend(currentValue: FimDataType, value: String, token: String): FimString {
    val currentString = (currentValue as FimString).value
    if (split(currentString).contains(value)) return currentValue

    val builder = StringBuilder(value)
    if (currentString.isNotEmpty()) {
      val separator = if (isList) "," else ""
      builder.append(separator).append(currentString)
    }
    return FimString(builder.toString())
  }

  override fun getValueIfRemove(currentValue: FimDataType, value: String, token: String): FimString {
    val currentValueAsString = (currentValue as FimString).value
    val newValue = if (isList) {
      val valuesToRemove = split(value)
      val elements = split(currentValueAsString).toMutableList()
      if (Collections.indexOfSubList(elements, valuesToRemove) != -1) {
        // see `:help set`
        // When the option is a list of flags, {value} must be
        // exactly as they appear in the option.  Remove flags
        // one by one to avoid problems.
        elements.removeAll(valuesToRemove)
      }
      elements.joinToString(separator = ",")
    } else {
      currentValueAsString.replace(value, "")
    }
    return FimString(newValue)
  }

  open fun split(value: String): List<String> {
    return if (isList) {
      value.split(",")
    } else {
      listOf(value)
    }
  }
}
