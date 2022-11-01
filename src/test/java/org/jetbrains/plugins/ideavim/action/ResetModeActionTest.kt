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
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.key.MappingOwner
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class ResetModeActionTest : FimTestCase() {
  private val owner = MappingOwner.Plugin.get("ResetModeActionTest")

  fun `test reset from normal mode`() {
    val keys = "<C-\\><C-N>"
    val before = "A Discovery"
    val after = "A Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset from insert mode`() {
    val keys = listOf("i", "<C-\\><C-N>")
    val before = "A Discovery"
    val after = "A Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset from insert mode check position`() {
    val keys = listOf("i", "<C-\\><C-N>")
    val before = "A Disc${c}overy"
    val after = "A Dis${c}covery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset and execute command`() {
    val keys = listOf("i", "<C-\\><C-N>", "3l")
    val before = "${c}A Discovery"
    val after = "A D${c}iscovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset from visual mode`() {
    val keys = listOf("V", "<C-\\><C-N>")
    val before = "A Discovery"
    val after = "A Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset from select mode`() {
    val keys = listOf("gH", "<C-\\><C-N>")
    val before = "A Discovery"
    val after = "A Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset from operator-pending mode`() {
    val keys = listOf("d", "<C-\\><C-N>")
    val before = "A Discovery"
    val after = "A Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test reset from operator-pending mode with delete`() {
    val keys = "d<Esc>dw"
    val before = "A Discovery"
    val after = "Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test delete command after resetting operator-pending mode`() {
    val keys = listOf("d", "<C-\\><C-N>", "dw")
    val before = "A Discovery"
    val after = "Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test delete command after resetting operator-pending mode with esc`() {
    val keys = listOf("d", "<Esc>", "dw")
    val before = "A Discovery"
    val after = "Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  @TestWithoutNeofim(SkipNeofimReason.CTRL_CODES)
  fun `test delete command after resetting operator-pending mode with ctrl open bracket`() {
    val keys = listOf("d", "<C-[>", "dw")
    val before = "A Discovery"
    val after = "Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  @TestWithoutNeofim(SkipNeofimReason.MAPPING)
  fun `test delete command after resetting operator-pending mode with mapping`() {
    com.flop.idea.fim.FimPlugin.getKey()
      .putKeyMapping(MappingMode.NVO, injector.parser.parseKeys("<C-D>"), owner, injector.parser.parseKeys("<Esc>"), false)

    val keys = listOf("d", "<C-D>", "dw")
    val before = "A Discovery"
    val after = "Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test non-delete commands after resetting operator-pending mode`() {
    val keys = listOf("c", "<C-\\><C-N>", "another")
    val before = "A Discovery"
    val after = "Another Discovery"
    doTest(keys, before, after, FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }

  fun `test delete after escaping t`() {
    val keys = "dt<esc>D"
    val before = "A ${c}Discovery"
    val after = "A "
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    TestCase.assertFalse(myFixture.editor.selectionModel.hasSelection())
  }
}
