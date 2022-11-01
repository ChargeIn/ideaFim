package com.flop.idea.fim.option

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.options.Option
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.parseNumber

/**
 * COMPATIBILITY-LAYER: Moved out of class and to a different package
 * Please see: https://jb.gg/zo8n0r
 */
open class NumberOption(name: String, abbrev: String, defaultValue: FimInt) :
  Option<FimInt>(name, abbrev, defaultValue) {
  constructor(name: String, abbrev: String, defaultValue: Int) : this(name, abbrev, FimInt(defaultValue))

  override fun checkIfValueValid(value: FimDataType, token: String) {
    if (value !is FimInt) {
      throw ExException("E521: Number required after =: $token")
    }
  }

  override fun getValueIfAppend(currentValue: FimDataType, value: String, token: String): FimInt {
    val valueToAdd = parseNumber(token) ?: throw ExException("E521: Number required after =: $token")
    return FimInt((currentValue as FimInt).value + valueToAdd)
  }

  override fun getValueIfPrepend(currentValue: FimDataType, value: String, token: String): FimInt {
    val valueToAdd = parseNumber(token) ?: throw ExException("E521: Number required after =: $token")
    return FimInt((currentValue as FimInt).value * valueToAdd)
  }

  override fun getValueIfRemove(currentValue: FimDataType, value: String, token: String): FimInt {
    val valueToAdd = parseNumber(token) ?: throw ExException("E521: Number required after =: $token")
    return FimInt((currentValue as FimInt).value - valueToAdd)
  }

  fun value(): Int {
    return injector.optionService.getOptionValue(OptionScope.GLOBAL, name).asDouble().toInt()
  }
}
