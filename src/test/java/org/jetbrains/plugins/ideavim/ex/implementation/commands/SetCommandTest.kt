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

package org.jetbrains.plugins.ideafim.ex.implementation.commands

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class SetCommandTest : FimTestCase() {

  fun `test unknown option`() {
    configureByText("\n")
    typeText(commandToKeys("set unknownOption"))
    assertPluginError(true)
    assertPluginErrorMessageContains("Unknown option: unknownOption")
  }

  fun `test toggle option`() {
    configureByText("\n")
    typeText(commandToKeys("set rnu"))
    assertTrue(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.relativenumberName))
    typeText(commandToKeys("set rnu!"))
    assertFalse(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.relativenumberName))
  }

  // todo we have spaces in assertExOutput because of pad(20) in the com.flop.idea.fim.fimscript.model.commands.SetCommandKt#showOptions method
  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test number option`() {
    configureByText("\n")
    typeText(commandToKeys("set scrolloff&"))
    assertEquals(FimInt(0), injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName))
    typeText(commandToKeys("set scrolloff?"))
    assertExOutput("scrolloff=0         \n")
    typeText(commandToKeys("set scrolloff=5"))
    assertEquals(FimInt(5), injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.scrolloffName))
    typeText(commandToKeys("set scrolloff?"))
    assertExOutput("scrolloff=5         \n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test toggle option as a number`() {
    configureByText("\n")
    typeText(commandToKeys("set number&"))
    assertEquals(FimInt(0), injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.numberName))
    typeText(commandToKeys("set number?"))
    assertExOutput("nonumber            \n")
    typeText(commandToKeys("let &nu=1000"))
    assertEquals(FimInt(1000), injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.numberName))
    typeText(commandToKeys("set number?"))
    assertExOutput("  number            \n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test toggle option exceptions`() {
    configureByText("\n")
    typeText(commandToKeys("set number+=10"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument: number+=10")
    typeText(commandToKeys("set number+=test"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument: number+=test")

    typeText(commandToKeys("set number^=10"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument: number^=10")
    typeText(commandToKeys("set number^=test"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument: number^=test")

    typeText(commandToKeys("set number-=10"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument: number-=10")
    typeText(commandToKeys("set number-=test"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument: number-=test")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test number option exceptions`() {
    configureByText("\n")
    typeText(commandToKeys("set scrolloff+=10"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E521: Number required after =: scrolloff+=10")
    typeText(commandToKeys("set scrolloff+=test"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E521: Number required after =: scrolloff+=test")

    typeText(commandToKeys("set scrolloff^=10"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E521: Number required after =: scrolloff^=10")
    typeText(commandToKeys("set scrolloff^=test"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E521: Number required after =: scrolloff^=test")

    typeText(commandToKeys("set scrolloff-=10"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E521: Number required after =: scrolloff-=10")
    typeText(commandToKeys("set scrolloff-=test"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E521: Number required after =: scrolloff-=test")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test string option`() {
    configureByText("\n")
    typeText(commandToKeys("set selection&"))
    assertEquals(FimString("inclusive"), injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.selectionName))
    typeText(commandToKeys("set selection?"))
    assertExOutput("selection=inclusive \n")
    typeText(commandToKeys("set selection=exclusive"))
    assertEquals(FimString("exclusive"), injector.optionService.getOptionValue(OptionScope.GLOBAL, OptionConstants.selectionName))
    typeText(commandToKeys("set selection?"))
    assertExOutput("selection=exclusive \n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test show numbered value`() {
    configureByText("\n")
    typeText(commandToKeys("set so"))
    assertExOutput("scrolloff=0         \n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.OPTION)
  fun `test show numbered value with questionmark`() {
    configureByText("\n")
    typeText(commandToKeys("set so?"))
    assertExOutput("scrolloff=0         \n")
  }
}
