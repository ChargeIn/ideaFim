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
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */
class YankVisualLinesActionTest : FimTestCase() {
  fun `test from visual mode`() {
    val text = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val yankedTest = """
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            
    """.trimIndent()
    configureByText(text)
    typeText(injector.parser.parseKeys("vjY"))
    val savedText = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()
    TestCase.assertEquals(yankedTest, savedText)
  }

  fun `test from visual mode till the end`() {
    val text = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was sett${c}led on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val textAfter = """
            A Discovery

            I found it in a legendary land
            all rocks and lavender and tufted grass,
            ${c}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest("vjY", text, textAfter, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    val yankedTest = """
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
            
    """.trimIndent()
    val savedText = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()
    TestCase.assertEquals(yankedTest, savedText)
  }

  fun `test from line visual mode`() {
    val text = """
            A Discovery

            I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    val yankedTest = """
            I found it in a legendary land
            all rocks and lavender and tufted grass,
            
    """.trimIndent()
    configureByText(text)
    typeText(injector.parser.parseKeys("VjY"))
    val savedText = com.flop.idea.fim.FimPlugin.getRegister().lastRegister?.text ?: kotlin.test.fail()
    TestCase.assertEquals(yankedTest, savedText)
  }
}
