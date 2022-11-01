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

package org.jetbrains.plugins.ideafim.option

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.options.StringOption
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class StringListOptionTest : FimTestCase() {
  private val optionName = "myOpt"

  init {
    injector.optionService.addOption(StringOption(optionName, optionName, "", true, null))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test append existing value`() {
    injector.optionService.appendValue(OptionScope.GLOBAL, optionName, "123")
    injector.optionService.appendValue(OptionScope.GLOBAL, optionName, "456")
    injector.optionService.appendValue(OptionScope.GLOBAL, optionName, "123")

    assertEquals("123,456", (injector.optionService.getOptionValue(OptionScope.GLOBAL, optionName) as FimString).value)
    injector.optionService.resetDefault(OptionScope.GLOBAL, optionName)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test prepend existing value`() {
    injector.optionService.appendValue(OptionScope.GLOBAL, optionName, "456")
    injector.optionService.appendValue(OptionScope.GLOBAL, optionName, "123")
    injector.optionService.prependValue(OptionScope.GLOBAL, optionName, "123")

    assertEquals("456,123", (injector.optionService.getOptionValue(OptionScope.GLOBAL, optionName) as FimString).value)
    injector.optionService.resetDefault(OptionScope.GLOBAL, optionName)
  }
}
