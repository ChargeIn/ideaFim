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

import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.options.Option
import com.flop.idea.fim.options.OptionChangeListener
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType

/**
 * COMPATIBILITY-LAYER: Moved to a different package
 * Please see: https://jb.gg/zo8n0r
 */
interface OptionService {

  /**
   * Gets option value.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token")
   */
  fun getOptionValue(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName): FimDataType

  /**
   * Sets option value.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param value option value
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token")
   */
  fun setOptionValue(scope: com.flop.idea.fim.options.OptionScope, optionName: String, value: FimDataType, token: String = optionName)

  /**
   * Checks if the [value] is contained in string option.
   *
   * Returns false if there is no option with the given optionName, or it's type is different from string.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param value option value
   */
  fun contains(scope: com.flop.idea.fim.options.OptionScope, optionName: String, value: String): Boolean

  /**
   * Splits a string option into flags
   *
   * e.g. the `fileencodings` option with value "ucs-bom,utf-8,default,latin1" will result listOf("ucs-bom", "utf-8", "default", "latin1")
   *
   * returns null if there is no option with the given optionName, or its type is different from string.
   * @param scope global/local option scope
   * @param optionName option name or alias
   */
  fun getValues(scope: com.flop.idea.fim.options.OptionScope, optionName: String): List<String>?

  /**
   * Same as [setOptionValue], but automatically casts [value] to the required [FimDataType]
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param value option value
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the cast to FimDataType is impossible
   */
  fun setOptionValue(scope: com.flop.idea.fim.options.OptionScope, optionName: String, value: String, token: String = optionName)

  /**
   * Same as `set {option}+={value}` in Fim documentation.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param value option value
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the method was called for the [ToggleOption]
   * @throws ExException("E474: Invalid argument: $token") in case the method was called for the [StringOption] and the argument is invalid (does not satisfy the option bounded values)
   * @throws ExException("E521: Number required after =: $token") in case the cast to FimInt is impossible
   */
  fun appendValue(scope: com.flop.idea.fim.options.OptionScope, optionName: String, value: String, token: String = optionName)

  /**
   * Same as `set {option}^={value}` in Fim documentation.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param value option value
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the method was called for the [ToggleOption]
   * @throws ExException("E474: Invalid argument: $token") in case the method was called for the [StringOption] and the argument is invalid (does not satisfy the option bounded values)
   * @throws ExException("E521: Number required after =: $token") in case the cast to FimInt is impossible
   */
  fun prependValue(scope: com.flop.idea.fim.options.OptionScope, optionName: String, value: String, token: String = optionName)

  /**
   * Same as `set {option}-={value}` in Fim documentation.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param value option value
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the method was called for the [ToggleOption]
   * @throws ExException("E474: Invalid argument: $token") in case the method was called for the [StringOption] and the argument is invalid (does not satisfy the option bounded values)
   * @throws ExException("E521: Number required after =: $token") in case the cast to FimInt is impossible
   */
  fun removeValue(scope: com.flop.idea.fim.options.OptionScope, optionName: String, value: String, token: String = optionName)

  /**
   * Checks if the toggle option on.
   *
   * Returns false if [optionName] is not a [ToggleOption]
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   */
  fun isSet(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName): Boolean

  /**
   * Checks if the option's value set to default.
   *
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   */
  fun isDefault(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName): Boolean

  /**
   * Resets option's value to default.
   *
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   */
  fun resetDefault(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName)

  /**
   * Resets all options back to default values.
   */
  fun resetAllOptions()

  /**
   * Checks if the option with given optionName is a toggleOption.
   * @param optionName option name or alias
   */
  fun isToggleOption(optionName: String): Boolean

  /**
   * Sets the option on (true).
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the option is not a [ToggleOption]
   */
  fun setOption(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName)

  /**
   * COMPATIBILITY-LAYER: New method added
   * Please see: https://jb.gg/zo8n0r
   */
  fun setOption(scope: Scope, optionName: String, token: String = optionName)

  /**
   * Unsets the option (false).
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the option is not a [ToggleOption]
   */
  fun unsetOption(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName)

  /**
   * Inverts boolean option value true -> false / false -> true.
   * @param scope global/local option scope
   * @param optionName option name or alias
   * @param token used in exception messages
   * @throws ExException("E518: Unknown option: $token") in case the option is not found
   * @throws ExException("E474: Invalid argument: $token") in case the option is not a [ToggleOption]
   */
  fun toggleOption(scope: com.flop.idea.fim.options.OptionScope, optionName: String, token: String = optionName)

  /**
   * @return list of all option names
   */
  fun getOptions(): Set<String>

  /**
   * @return list of all option abbreviations
   */
  fun getAbbrevs(): Set<String>

  /**
   * Adds the option.
   * @param option option
   */
  fun addOption(option: Option<out FimDataType>)

  /**
   * Removes the option.
   * @param optionName option name or alias
   */
  fun removeOption(optionName: String)

  /**
   * Adds a listener to the option.
   * @param optionName option name or alias
   * @param listener option listener
   * @param executeOnAdd whether execute listener after the method call or not
   */
  fun addListener(optionName: String, listener: OptionChangeListener<FimDataType>, executeOnAdd: Boolean = false)

  /**
   * Remove the listener from the option.
   * @param optionName option name or alias
   * @param listener option listener
   */
  fun removeListener(optionName: String, listener: OptionChangeListener<FimDataType>)

  /**
   * Get the [Option] by its name or abbreviation
   */
  fun getOptionByNameOrAbbr(key: String): Option<out FimDataType>?

  /**
   * COMPATIBILITY-LAYER: Added this class
   * Please see: https://jb.gg/zo8n0r
   */
  sealed class Scope {
    object GLOBAL : Scope()
    class LOCAL(val editor: FimEditor) : Scope()
  }
}
