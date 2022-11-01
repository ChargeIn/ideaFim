package com.flop.idea.fim.option

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.options.Option
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimInt

/**
 * COMPATIBILITY-LAYER: Moved out of class and to a different package
 * Please see: https://jb.gg/zo8n0r
 */
class ToggleOption(name: String, abbrev: String, defaultValue: FimInt) : Option<FimInt>(name, abbrev, defaultValue) {
  constructor(name: String, abbrev: String, defaultValue: Boolean) : this(name, abbrev, if (defaultValue) FimInt.ONE else FimInt.ZERO)

  override fun checkIfValueValid(value: FimDataType, token: String) {
    if (value !is FimInt) {
      throw ExException("E474: Invalid argument: $token")
    }
  }

  override fun getValueIfAppend(currentValue: FimDataType, value: String, token: String): FimInt {
    throw ExException("E474: Invalid argument: $token")
  }

  override fun getValueIfPrepend(currentValue: FimDataType, value: String, token: String): FimInt {
    throw ExException("E474: Invalid argument: $token")
  }

  override fun getValueIfRemove(currentValue: FimDataType, value: String, token: String): FimInt {
    throw ExException("E474: Invalid argument: $token")
  }

  /**
   * COMPATIBILITY-LAYER: Method added
   * Please see: https://jb.gg/zo8n0r
   */
  fun isSet(): Boolean {
    return injector.optionService.getOptionValue(OptionScope.GLOBAL, name).asBoolean()
  }

  /**
   * COMPATIBILITY-LAYER: Method added
   * Please see: https://jb.gg/zo8n0r
   */
  @Suppress("DEPRECATION", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
  override val value: java.lang.Boolean
    get() = if (injector.optionService.getOptionValue(OptionScope.GLOBAL, name).asBoolean()) {
      java.lang.Boolean(true)
    } else {
      java.lang.Boolean(false)
    }
}
