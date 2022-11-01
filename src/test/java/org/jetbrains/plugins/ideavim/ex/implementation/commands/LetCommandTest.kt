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

import com.intellij.testFramework.PlatformTestUtil
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.fimscript.FimScriptGlobalEnvironment
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class LetCommandTest : FimTestCase() {

  fun `test assignment to string`() {
    configureByText("\n")
    typeText(commandToKeys("let s = \"foo\""))
    typeText(commandToKeys("echo s"))
    assertExOutput("foo\n")
  }

  fun `test assignment to number`() {
    configureByText("\n")
    typeText(commandToKeys("let s = 100"))
    typeText(commandToKeys("echo s"))
    assertExOutput("100\n")
  }

  fun `test assignment to expression`() {
    configureByText("\n")
    typeText(commandToKeys("let s = 10 + 20 * 4"))
    typeText(commandToKeys("echo s"))
    assertExOutput("90\n")
  }

  fun `test adding new pair to dictionary`() {
    configureByText("\n")
    typeText(commandToKeys("let s = {'key1' : 1}"))
    typeText(commandToKeys("let s['key2'] = 2"))
    typeText(commandToKeys("echo s"))
    assertExOutput("{'key1': 1, 'key2': 2}\n")
  }

  fun `test editing existing pair in dictionary`() {
    configureByText("\n")
    typeText(commandToKeys("let s = {'key1' : 1}"))
    typeText(commandToKeys("let s['key1'] = 2"))
    typeText(commandToKeys("echo s"))
    assertExOutput("{'key1': 2}\n")
  }

  fun `test assignment plus operator`() {
    configureByText("\n")
    typeText(commandToKeys("let s = 10"))
    typeText(commandToKeys("let s += 5"))
    typeText(commandToKeys("echo s"))
    assertExOutput("15\n")
  }

  fun `test changing list item`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 1]"))
    typeText(commandToKeys("let s[1] = 2"))
    typeText(commandToKeys("echo s"))
    assertExOutput("[1, 2]\n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test changing list item with index out of range`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 1]"))
    typeText(commandToKeys("let s[2] = 2"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E684: list index out of range: 2")
  }

  fun `test changing list with sublist expression`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 2, 3]"))
    typeText(commandToKeys("let s[0:1] = [5, 4]"))
    typeText(commandToKeys("echo s"))
    assertExOutput("[5, 4, 3]\n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test changing list with sublist expression and larger list`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 2, 3]"))
    typeText(commandToKeys("let s[0:1] = [5, 4, 3, 2, 1]"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E710: List value has more items than targets")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test changing list with sublist expression and smaller list`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 2, 3]"))
    typeText(commandToKeys("let s[0:1] = [5]"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E711: List value does not have enough items")
  }

  fun `test changing list with sublist expression and undefined end`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 2, 3]"))
    typeText(commandToKeys("let s[1:] = [5, 5, 5, 5]"))
    typeText(commandToKeys("echo s"))
    assertExOutput("[1, 5, 5, 5, 5]\n")
  }

  fun `test let option`() {
    configureByText("\n")
    typeText(commandToKeys("set noincsearch"))
    assertFalse(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.incsearchName))
    typeText(commandToKeys("let &incsearch = 12"))
    assertTrue(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.incsearchName))
    typeText(commandToKeys("set noincsearch"))
    assertFalse(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.incsearchName))
  }

  fun `test let option2`() {
    configureByText("\n")
    typeText(commandToKeys("set incsearch"))
    assertTrue(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.incsearchName))
    typeText(commandToKeys("let &incsearch = 0"))
    assertFalse(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.incsearchName))
  }

  fun `test comment`() {
    configureByText("\n")
    typeText(commandToKeys("let s = [1, 2, 3] \" my list for storing numbers"))
    typeText(commandToKeys("echo s"))
    assertExOutput("[1, 2, 3]\n")
  }

  fun `test fimScriptGlobalEnvironment`() {
    configureByText("\n")
    typeText(commandToKeys("let g:WhichKey_ShowFimActions = \"true\""))
    typeText(commandToKeys("echo g:WhichKey_ShowFimActions"))
    assertExOutput("true\n")
    assertEquals("true", com.flop.idea.fim.ex.fimscript.FimScriptGlobalEnvironment.getInstance().variables["g:WhichKey_ShowFimActions"])
  }

  fun `test list is passed by reference`() {
    configureByText("\n")
    typeText(commandToKeys("let list = [1, 2, 3]"))
    typeText(commandToKeys("let l2 = list"))
    typeText(commandToKeys("let list += [4]"))
    typeText(commandToKeys("echo l2"))

    assertExOutput("[1, 2, 3, 4]\n")
  }

  fun `test list is passed by reference 2`() {
    configureByText("\n")
    typeText(commandToKeys("let list = [1, 2, 3, []]"))
    typeText(commandToKeys("let l2 = list"))
    typeText(commandToKeys("let list[3] += [4]"))
    typeText(commandToKeys("echo l2"))

    assertExOutput("[1, 2, 3, [4]]\n")
  }

  fun `test list is passed by reference 3`() {
    configureByText("\n")
    typeText(commandToKeys("let list = [1, 2, 3, []]"))
    typeText(commandToKeys("let dict = {}"))
    typeText(commandToKeys("let dict.l2 = list"))
    typeText(commandToKeys("let list[3] += [4]"))
    typeText(commandToKeys("echo dict.l2"))

    assertExOutput("[1, 2, 3, [4]]\n")
  }

  fun `test list is passed by reference 4`() {
    configureByText("\n")
    typeText(commandToKeys("let list = [1, 2, 3]"))
    typeText(commandToKeys("let dict = {}"))
    typeText(commandToKeys("let dict.l2 = list"))
    typeText(commandToKeys("let dict.l2 += [4]"))
    typeText(commandToKeys("echo dict.l2"))

    assertExOutput("[1, 2, 3, 4]\n")
  }

  fun `test number is passed by value`() {
    configureByText("\n")
    typeText(commandToKeys("let number = 10"))
    typeText(commandToKeys("let n2 = number"))
    typeText(commandToKeys("let number += 2"))
    typeText(commandToKeys("echo n2"))

    assertExOutput("10\n")
  }

  fun `test string is passed by value`() {
    configureByText("\n")
    typeText(commandToKeys("let string = 'abc'"))
    typeText(commandToKeys("let str2 = string"))
    typeText(commandToKeys("let string .= 'd'"))
    typeText(commandToKeys("echo str2"))

    assertExOutput("abc\n")
  }

  fun `test dict is passed by reference`() {
    configureByText("\n")
    typeText(commandToKeys("let dictionary = {}"))
    typeText(commandToKeys("let dict2 = dictionary"))
    typeText(commandToKeys("let dictionary.one = 1"))
    typeText(commandToKeys("let dictionary['two'] = 2"))
    typeText(commandToKeys("echo dict2"))

    assertExOutput("{'one': 1, 'two': 2}\n")
  }

  fun `test dict is passed by reference 2`() {
    configureByText("\n")
    typeText(commandToKeys("let list = [1, 2, 3, {'a': 'b'}]"))
    typeText(commandToKeys("let dict = list[3]"))
    typeText(commandToKeys("let list[3].key = 'value'"))
    typeText(commandToKeys("echo dict"))

    assertExOutput("{'a': 'b', 'key': 'value'}\n")
  }

  fun `test numbered register`() {
    configureByText("\n")
    typeText(commandToKeys("let @4 = 'inumber register works'"))
    typeText(commandToKeys("echo @4"))
    assertExOutput("inumber register works\n")

    typeText(injector.parser.parseKeys("@4"))
    if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideadelaymacroName)) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }
    assertState("number register works\n")
  }

  fun `test lowercase letter register`() {
    configureByText("\n")
    typeText(commandToKeys("let @o = 'ilowercase letter register works'"))
    typeText(commandToKeys("echo @o"))
    assertExOutput("ilowercase letter register works\n")

    typeText(injector.parser.parseKeys("@o"))
    if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideadelaymacroName)) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }
    assertState("lowercase letter register works\n")
  }

  fun `test uppercase letter register`() {
    configureByText("\n")
    typeText(commandToKeys("let @O = 'iuppercase letter register works'"))
    typeText(commandToKeys("echo @O"))
    assertExOutput("iuppercase letter register works\n")

    typeText(injector.parser.parseKeys("@O"))
    if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideadelaymacroName)) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }
    assertState("uppercase letter register works\n")
    typeText(injector.parser.parseKeys("<Esc>"))

    typeText(commandToKeys("let @O = '!'"))
    typeText(commandToKeys("echo @O"))
    assertExOutput("iuppercase letter register works!\n")
  }

  fun `test unnamed register`() {
    configureByText("\n")
    typeText(commandToKeys("let @\" = 'iunnamed register works'"))
    typeText(commandToKeys("echo @\""))
    assertExOutput("iunnamed register works\n")

    typeText(injector.parser.parseKeys("@\""))
    if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideadelaymacroName)) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }
    assertState("unnamed register works\n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test define script variable with command line context`() {
    configureByText("\n")
    typeText(commandToKeys("let s:my_var = 'oh, hi Mark'"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E461: Illegal variable name: s:my_var")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test define local variable with command line context`() {
    configureByText("\n")
    typeText(commandToKeys("let l:my_var = 'oh, hi Mark'"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E461: Illegal variable name: l:my_var")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test define function variable with command line context`() {
    configureByText("\n")
    typeText(commandToKeys("let a:my_var = 'oh, hi Mark'"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E461: Illegal variable name: a:my_var")
  }
}
