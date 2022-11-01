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
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */

class ActionCommandTest : FimTestCase() {
  // VIM-652 |:action|
  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testEditorRightAction() {
    configureByText("<caret>foo\n" + "bar\n")
    typeText(commandToKeys("action EditorRight"))
    assertMode(FimStateMachine.Mode.COMMAND)
    assertState("f<caret>oo\n" + "bar\n")
  }

  // VIM-862 |:action| in visual character mode
  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualCharacterMode() {
    configureByJavaText(
      "-----\n" +
        "1<caret>2345\n" +
        "abcde\n" +
        "-----"
    )
    typeText(injector.parser.parseKeys("vjl"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState(
      "-----\n" +
        "1/*2345\n" +
        "abc*/de\n" +
        "-----"
    )
  }

  // https://github.com/JetBrains/ideafim/commit/fe714a90032d0cb5ef0a0e0d8783980b6f1c7d20#r35647600
  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualCharacterModeWithIncSearch() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
    configureByJavaText(
      "-----\n" +
        "1<caret>2345\n" +
        "abcde\n" +
        "-----"
    )
    typeText(injector.parser.parseKeys("vjl"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState(
      "-----\n" +
        "1/*2345\n" +
        "abc*/de\n" +
        "-----"
    )
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
  }

  // VIM-862 |:action|
  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualCharacterModeSameLine() {
    configureByJavaText("1<caret>2345\n" + "abcde\n")
    typeText(injector.parser.parseKeys("vl"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState("1/*23*/45\n" + "abcde\n")
  }

  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualCharacterModeSameLineWithIncsearch() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
    configureByJavaText("1<caret>2345\n" + "abcde\n")
    typeText(injector.parser.parseKeys("vl"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState("1/*23*/45\n" + "abcde\n")
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
  }

  // VIM-862 |:action| in visual line mode
  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualLineMode() {
    configureByJavaText(
      "-----\n" +
        "1<caret>2345\n" +
        "abcde\n" +
        "-----"
    )
    typeText(injector.parser.parseKeys("Vj"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState(
      "-----\n" +
        "/*\n" +
        "12345\n" +
        "abcde\n" +
        "*/\n" +
        "-----"
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualLineModeWithIncsearch() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
    configureByJavaText(
      "-----\n" +
        "1<caret>2345\n" +
        "abcde\n" +
        "-----"
    )
    typeText(injector.parser.parseKeys("Vj"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState(
      "-----\n" +
        "/*\n" +
        "12345\n" +
        "abcde\n" +
        "*/\n" +
        "-----"
    )
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
  }

  // VIM-862 |:action| in visual block mode
  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualBlockMode() {
    configureByJavaText(
      "-----\n" +
        "1<caret>2345\n" +
        "abcde\n" +
        "-----"
    )
    typeText(injector.parser.parseKeys("<C-V>lj"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState(
      "-----\n" +
        "1/*23*/45\n" +
        "a/*bc*/de\n" +
        "-----"
    )
  }

  @TestWithoutNeofim(SkipNeofimReason.ACTION_COMMAND)
  fun testExCommandInVisualBlockModeWithIncsearch() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
    configureByJavaText(
      "-----\n" +
        "1<caret>2345\n" +
        "abcde\n" +
        "-----"
    )
    typeText(injector.parser.parseKeys("<C-V>lj"))
    typeText(commandToKeys("'<,'>action CommentByBlockComment"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertState(
      "-----\n" +
        "1/*23*/45\n" +
        "a/*bc*/de\n" +
        "-----"
    )
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.incsearchName)
  }
}
