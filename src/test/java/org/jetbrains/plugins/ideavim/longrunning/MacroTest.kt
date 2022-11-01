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

package org.jetbrains.plugins.ideafim.longrunning

import com.intellij.testFramework.PlatformTestUtil
import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class MacroTest : FimTestCase() {

  // was a problem on revision affec9bb61ea5e1e635673a0041d61f7af3722b2
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test no StackOverflowException`() {
    configureByText("abc de${c}fg")
    typeText(injector.parser.parseKeys("qahlq"))
    typeText(injector.parser.parseKeys("1000000@a"))
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    assertState("abc de${c}fg")
  }
}
