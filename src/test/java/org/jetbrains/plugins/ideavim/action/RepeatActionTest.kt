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

package org.jetbrains.plugins.ideafim.action

import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.junit.Test

class RepeatActionTest : FimTestCase() {

  @Test
  fun testSimpleRepeatLastCommand() {
    configureByText("foo foo")
    typeText(injector.parser.parseKeys("cw" + "bar" + "<Esc>" + "w" + "."))
    assertState("bar bar")
  }

  @Test
  fun testRepeatChangeToCharInNextLine() {
    configureByText(
      "The first line.\n" +
        "This is the second line.\n" +
        "Third line here, with a comma.\n" +
        "Last line."
    )
    typeText(injector.parser.parseKeys("j" + "ct." + "Change the line to point" + "<Esc>" + "j0" + "."))
    assertState(
      "The first line.\n" +
        "Change the line to point.\n" +
        "Change the line to point.\n" +
        "Last line."
    )
  }

  // VIM-1644
  @Test
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testRepeatChangeInVisualMode() {
    configureByText("foobar foobar")
    typeText(injector.parser.parseKeys("<C-V>llc" + "fu" + "<Esc>" + "w" + "."))
    assertState("fubar fubar")
  }

  // VIM-1644
  @Test
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testRepeatChangeInVisualModeMultiline() {
    configureByText(
      "There is a red house.\n" +
        "Another red house there.\n" +
        "They have red windows.\n" +
        "Good."
    )
    typeText(injector.parser.parseKeys("www" + "<C-V>ec" + "blue" + "<Esc>" + "j0w." + "j0ww."))
    assertState(
      "There is a blue house.\n" +
        "Another blue house there.\n" +
        "They have blue windows.\n" +
        "Good."
    )
  }
}
