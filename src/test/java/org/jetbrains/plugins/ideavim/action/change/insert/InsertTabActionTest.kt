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

package org.jetbrains.plugins.ideafim.action.change.insert

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class InsertTabActionTest : FimTestCase() {
  fun `test insert tab`() {
    setupChecks {
      keyHandler = Checks.KeyHandlerMethod.DIRECT_TO_VIM
    }
    val before = "I fo${c}und it in a legendary land"
    val after = "I fo    ${c}und it in a legendary land"
    configureByText(before)

    typeText(injector.parser.parseKeys("i" + "<Tab>"))

    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test insert tab scrolls at end of line`() {
    setupChecks {
      keyHandler = Checks.KeyHandlerMethod.DIRECT_TO_VIM
    }
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)

    // TODO: This works for tests, but not in real life. See FimShortcutKeyAction.isEnabled
    typeText(injector.parser.parseKeys("70|" + "i" + "<Tab>"))
    assertVisibleLineBounds(0, 32, 111)
  }

  @TestWithoutNeofim(SkipNeofimReason.OPTION)
  fun `test insert tab scrolls at end of line 2`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.sidescrolloffName, FimInt(10))
    configureByColumns(200)
    typeText(injector.parser.parseKeys("70|" + "i" + "<C-I>"))
    assertVisibleLineBounds(0, 32, 111)
  }
}
