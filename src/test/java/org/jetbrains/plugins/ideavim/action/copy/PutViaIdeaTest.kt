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

import com.intellij.codeInsight.editorActions.TextBlockTransferable
import com.intellij.openapi.ide.CopyPasteManager
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.jetbrains.plugins.ideafim.rangeOf
import java.util.*

/**
 * @author Alex Plate
 */
class PutViaIdeaTest : FimTestCase() {

  private var optionsBefore: String = ""

  override fun setUp() {
    super.setUp()
    optionsBefore = (com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName) as FimString).value
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName, FimString("ideaput"))
  }

  override fun tearDown() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName, FimString(optionsBefore))
    super.tearDown()
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test simple insert via idea`() {
    val before = "${c}I found it in a legendary land"
    configureByText(before)

    injector.registerGroup.storeText('"', "legendary", SelectionType.CHARACTER_WISE)

    typeText(injector.parser.parseKeys("ve" + "p"))
    val after = "legendar${c}y it in a legendary land"
    assertState(after)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test insert several times`() {
    val before = "${c}I found it in a legendary land"
    configureByText(before)

    com.flop.idea.fim.FimPlugin.getRegister()
      .storeText(myFixture.editor.fim, before rangeOf "legendary", SelectionType.CHARACTER_WISE, false)

    typeText(injector.parser.parseKeys("ppp"))
    val after = "Ilegendarylegendarylegendar${c}y found it in a legendary land"
    assertState(after)
  }

  fun `test insert doesn't clear existing elements`() {
    val randomUUID = UUID.randomUUID()
    val before = "${c}I found it in a legendary$randomUUID land"
    configureByText(before)

    CopyPasteManager.getInstance().setContents(TextBlockTransferable("Fill", emptyList(), null))
    CopyPasteManager.getInstance().setContents(TextBlockTransferable("Buffer", emptyList(), null))

    com.flop.idea.fim.FimPlugin.getRegister()
      .storeText(myFixture.editor.fim, before rangeOf "legendary$randomUUID", SelectionType.CHARACTER_WISE, false)

    val sizeBefore = CopyPasteManager.getInstance().allContents.size
    typeText(injector.parser.parseKeys("ve" + "p"))
    assertEquals(sizeBefore, CopyPasteManager.getInstance().allContents.size)
  }

  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun `test insert block with newline`() {
    val before = """
            A Discovery
            $c
            I found it in a legendary land
            
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(before)

    com.flop.idea.fim.FimPlugin.getRegister().storeText(
      myFixture.editor.fim,
      before rangeOf "\nI found it in a legendary land\n",
      SelectionType.CHARACTER_WISE,
      false
    )

    typeText(injector.parser.parseKeys("p"))
    val after = """
            A Discovery
            
            I found it in a legendary land
            
            I found it in a legendary land
            
            hard by the torrent of a mountain pass.
    """.trimIndent()
    assertState(after)
  }
}
