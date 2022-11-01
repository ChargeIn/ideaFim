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

package org.jetbrains.plugins.ideafim.action.copy

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.FimTestCase

class YankMotionActionTest : FimTestCase() {
  fun `test yank till new line`() {
    val file = """
            A Discovery

            I found it in a legendary l${c}and
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("yW"), file)
    val text = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()

    TestCase.assertEquals("and", text)
  }

  fun `test yank caret doesn't move`() {
    val file = """
            A Discovery

            I found it in a legendary l${c}and
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(file)

    val initialOffset = myFixture.editor.caretModel.offset
    typeText(injector.parser.parseKeys("yy"))

    TestCase.assertEquals(initialOffset, myFixture.editor.caretModel.offset)
  }

  @Suppress("DANGEROUS_CHARACTERS")
  fun `test unnamed saved to " register`() {
    val clipboardValue = (com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName) as FimString).value
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName, FimString("unnamed"))

    try {
      configureByText("I found it in a ${c}legendary land")
      typeText(injector.parser.parseKeys("yiw"))

      val starRegister = com.flop.idea.fim.FimPlugin.getRegister().getRegister('*') ?: kotlin.test.fail("Register * is empty")
      assertEquals("legendary", starRegister.text)

      val quoteRegister = com.flop.idea.fim.FimPlugin.getRegister().getRegister('"') ?: kotlin.test.fail("Register \" is empty")
      assertEquals("legendary", quoteRegister.text)
    } finally {
      com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName, FimString(clipboardValue))
    }
  }

  @Suppress("DANGEROUS_CHARACTERS")
  fun `test z saved to " register`() {
    configureByText("I found it in a ${c}legendary land")
    typeText(injector.parser.parseKeys("\"zyiw"))

    val starRegister = com.flop.idea.fim.FimPlugin.getRegister().getRegister('z') ?: kotlin.test.fail("Register z is empty")
    assertEquals("legendary", starRegister.text)

    val quoteRegister = com.flop.idea.fim.FimPlugin.getRegister().getRegister('"') ?: kotlin.test.fail("Register \" is empty")
    assertEquals("legendary", quoteRegister.text)
  }

  @Suppress("DANGEROUS_CHARACTERS")
  fun `test " saved to " register`() {
    configureByText("I found it in a ${c}legendary land")
    typeText(injector.parser.parseKeys("\"zyiw"))

    val quoteRegister = com.flop.idea.fim.FimPlugin.getRegister().getRegister('"') ?: kotlin.test.fail("Register \" is empty")
    assertEquals("legendary", quoteRegister.text)
  }

  fun `test yank up`() {
    val file = """
            A ${c}Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("yk"), file)

    assertTrue(com.flop.idea.fim.FimPlugin.isError())
  }

  fun `test yank dollar at last empty line`() {
    val file = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
            $c
    """.trimIndent()
    typeTextInFile(injector.parser.parseKeys("y$"), file)
    val text = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()

    TestCase.assertEquals("", text)
  }

  fun `test yank to star with mapping`() {
    val file = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(commandToKeys("map * *zz"), file)
    typeTextInFile(injector.parser.parseKeys("\"*yiw"), file)
    val text = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()

    TestCase.assertEquals("legendary", text)
  }

  fun `test yank to star with yank mapping`() {
    val file = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    typeTextInFile(commandToKeys("map * *yiw"), file)
    typeTextInFile(injector.parser.parseKeys("\"*"), file)
    assertNull(com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text)
  }

  fun `test yank last line`() {
    val file = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent$c of a mountain pass.
    """.trimIndent()

    doTest("yy", file, file, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    val text = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()

    TestCase.assertEquals("hard by the torrent of a mountain pass.\n", text)
  }
}
