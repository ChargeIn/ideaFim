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

package org.jetbrains.plugins.ideafim.action.change

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.services.IjFimOptionService
import org.jetbrains.plugins.ideafim.FimTestCase

class UndoActionTest : FimTestCase() {
  fun `test simple undo`() {
    val keys = listOf("dw", "u")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = before
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    val editor = myFixture.editor
    assertFalse(editor.caretModel.primaryCaret.hasSelection())
  }

  // Not yet supported
  fun `undo after selection`() {
    val keys = listOf("v3eld", "u")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = before
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    assertFalse(hasSelection())
  }

  fun `test undo with count`() {
    val keys = listOf("dwdwdw", "2u")
    val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    val after = """
                A Discovery

                ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
    """.trimIndent()
    doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    assertFalse(hasSelection())
  }

  fun `test cursor movements do not require additional undo`() {
    if (!injector.optionService.isSet(OptionScope.GLOBAL, IjFimOptionService.oldUndo)) {
      val keys = listOf("a1<Esc>ea2<Esc>ea3<Esc>", "uu")
      val before = """
                A Discovery

                ${c}I found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent()
      val after = """
                A Discovery

                I1 found$c it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent()
      doTest(keys, before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
      assertFalse(hasSelection())
    }
  }

  private fun hasSelection(): Boolean {
    val editor = myFixture.editor
    return editor.caretModel.primaryCaret.hasSelection()
  }
}
