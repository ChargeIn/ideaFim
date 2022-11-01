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

import com.intellij.testFramework.PlatformTestUtil
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.FimTestCase
import org.jetbrains.plugins.ideafim.rangeOf
import org.jetbrains.plugins.ideafim.waitAndAssert

/**
 * @author vlan
 */
class MacroActionTest : FimTestCase() {
  // |q|
  fun testRecordMacro() {
    val editor = typeTextInFile(injector.parser.parseKeys("qa" + "3l" + "q"), "on<caret>e two three\n")
    val commandState = editor.fim.fimStateMachine
    assertFalse(commandState.isRecording)
    val registerGroup = com.flop.idea.fim.FimPlugin.getRegister()
    val register = registerGroup.getRegister('a')
    assertNotNull(register)
    assertEquals("3l", register!!.text)
  }

  fun testRecordMacroDoesNotExpandMap() {
    configureByText("")
    enterCommand("imap pp hello")
    typeText(injector.parser.parseKeys("qa" + "i" + "pp<Esc>" + "q"))
    val register = com.flop.idea.fim.FimPlugin.getRegister().getRegister('a')
    assertNotNull(register)
    assertEquals("ipp<Esc>", injector.parser.toKeyNotation(register!!.keys))
  }

  fun testRecordMacroWithDigraph() {
    typeTextInFile(injector.parser.parseKeys("qa" + "i" + "<C-K>OK<Esc>" + "q"), "")
    val register = com.flop.idea.fim.FimPlugin.getRegister().getRegister('a')
    assertNotNull(register)
    assertEquals("i<C-K>OK<Esc>", injector.parser.toKeyNotation(register!!.keys))
  }

  fun `test macro with search`() {
    val content = """
            A Discovery

            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(content)
    typeText(injector.parser.parseKeys("qa" + "/rocks<CR>" + "q" + "gg" + "@a"))

    val startOffset = content.rangeOf("rocks").startOffset

    waitAndAssert {
      startOffset == myFixture.editor.caretModel.offset
    }
  }

  fun `test macro with command`() {
    val content = """
            A Discovery

            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(content)
    typeText(injector.parser.parseKeys("qa" + ":map x y<CR>" + "q"))

    val register = com.flop.idea.fim.FimPlugin.getRegister().getRegister('a')
    val registerSize = register!!.keys.size
    TestCase.assertEquals(9, registerSize)
  }

  fun `test last command`() {
    val content = "${c}0\n1\n2\n3\n"
    configureByText(content)
    typeText(injector.parser.parseKeys(":d<CR>" + "@:"))
    assertState("2\n3\n")
  }

  fun `test last command with count`() {
    val content = "${c}0\n1\n2\n3\n4\n5\n"
    configureByText(content)
    typeText(injector.parser.parseKeys(":d<CR>" + "4@:"))
    assertState("5\n")
  }

  fun `test last command as last macro with count`() {
    val content = "${c}0\n1\n2\n3\n4\n5\n"
    configureByText(content)
    typeText(injector.parser.parseKeys(":d<CR>" + "@:" + "3@@"))
    assertState("5\n")
  }

  fun `test last command as last macro multiple times`() {
    val content = "${c}0\n1\n2\n3\n4\n5\n"
    configureByText(content)
    typeText(injector.parser.parseKeys(":d<CR>" + "@:" + "@@" + "@@"))
    assertState("4\n5\n")
  }

  // Broken, see the resulting text
  fun `ignore test macro with macro`() {
    val content = """
            A Discovery

            ${c}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(content)
    typeText(injector.parser.parseKeys("qa" + "l" + "q" + "qb" + "10@a" + "q" + "2@b"))

    val startOffset = content.rangeOf("rocks").startOffset

    waitAndAssert {
      println(myFixture.editor.caretModel.offset)
      println(startOffset)
      println()
      startOffset == myFixture.editor.caretModel.offset
    }
  }

  fun `test macro with count`() {
    configureByText("${c}0\n1\n2\n3\n4\n5\n")
    typeText(injector.parser.parseKeys("qajq" + "4@a"))
    if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideadelaymacroName)) {
      PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    }
    assertState("0\n1\n2\n3\n4\n${c}5\n")
  }
}
