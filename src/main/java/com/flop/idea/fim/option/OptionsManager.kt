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

package com.flop.idea.fim.option

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionConstants.Companion.ignorecaseName
import com.flop.idea.fim.options.OptionConstants.Companion.smartcaseName
import com.flop.idea.fim.options.OptionConstants.Companion.timeoutName
import com.flop.idea.fim.options.OptionConstants.Companion.timeoutlenName
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.options.helpers.KeywordOptionHelper
import com.flop.idea.fim.fimscript.services.IjFimOptionService

/**
 * COMPATIBILITY-LAYER: Added a class and package
 * Please see: https://jb.gg/zo8n0r
 */
object OptionsManager {
  val ignorecase: ToggleOption
    get() = (injector.optionService as IjFimOptionService).getOptionByNameOrAbbr(ignorecaseName) as ToggleOption
  val smartcase: ToggleOption
    get() = (injector.optionService as IjFimOptionService).getOptionByNameOrAbbr(smartcaseName) as ToggleOption
  val timeout: ToggleOption
    get() = (injector.optionService as IjFimOptionService).getOptionByNameOrAbbr(timeoutName) as ToggleOption
  val timeoutlen: NumberOption
    get() = (injector.optionService as IjFimOptionService).getOptionByNameOrAbbr(timeoutlenName) as NumberOption
  val iskeyword: KeywordOption
    get() = KeywordOption(KeywordOptionHelper)
}

class KeywordOption(val helper: KeywordOptionHelper) {
  fun toRegex(): List<String> {
    return helper.toRegex()
  }
}

object StrictMode {
  fun fail(message: String) {
    if (injector.optionService.isSet(OptionScope.GLOBAL, OptionConstants.ideastrictmodeName)) {
      error(message)
    }
  }
}
