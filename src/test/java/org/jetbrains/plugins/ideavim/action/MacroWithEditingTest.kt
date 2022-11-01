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

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.jetbrains.plugins.ideafim.waitAndAssert

class MacroWithEditingTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test print macro`() {
    typeTextInFile(injector.parser.parseKeys("qa" + "iHello<Esc>" + "q"), "")
    setText("")
    typeText(injector.parser.parseKeys("\"ap"))
    assertState("iHello" + 27.toChar())
  }

  fun `test copy and perform macro`() {
    typeTextInFile(injector.parser.parseKeys("^v\$h\"wy"), "iHello<Esc>")
    assertEquals("iHello<Esc>", com.flop.idea.fim.FimPlugin.getRegister().getRegister('w')?.rawText)
    setText("")
    typeText(injector.parser.parseKeys("@w"))
    waitAndAssert {
      myFixture.editor.document.text == "Hello"
    }
  }

  fun `test copy and perform macro ctrl_a`() {
    typeTextInFile(injector.parser.parseKeys("^v\$h\"wy"), "<C-A>")
    assertEquals("<C-A>", com.flop.idea.fim.FimPlugin.getRegister().getRegister('w')?.rawText)
    setText("1")
    typeText(injector.parser.parseKeys("@w"))
    waitAndAssert {
      myFixture.editor.document.text == "2"
    }
  }
}
