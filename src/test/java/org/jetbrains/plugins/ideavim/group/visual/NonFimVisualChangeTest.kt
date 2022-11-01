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

package org.jetbrains.plugins.ideafim.group.visual

import com.intellij.codeInsight.editorActions.BackspaceHandler
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.LogicalPosition
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.editorMode
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.listener.FimListenerManager
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.jetbrains.plugins.ideafim.assertDoesntChange
import org.jetbrains.plugins.ideafim.rangeOf
import org.jetbrains.plugins.ideafim.waitAndAssert
import org.jetbrains.plugins.ideafim.waitAndAssertMode

/**
 * @author Alex Plate
 */
class NonFimVisualChangeTest : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test save mode after removing text`() {
    // PyCharm uses BackspaceHandler.deleteToTargetPosition to remove indent
    // See https://github.com/JetBrains/ideafim/pull/186#issuecomment-486656093
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    FimListenerManager.EditorListeners.add(myFixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(FimStateMachine.Mode.INSERT)
    ApplicationManager.getApplication().runWriteAction {
      CommandProcessor.getInstance().runUndoTransparentAction {
        BackspaceHandler.deleteToTargetPosition(myFixture.editor, LogicalPosition(2, 0))
      }
    }
    assertState(
      """
            A Discovery

            found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.INSERT)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable and disable selection`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    FimListenerManager.EditorListeners.add(myFixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(FimStateMachine.Mode.INSERT)

    // Fast add and remove selection
    myFixture.editor.selectionModel.setSelection(0, 10)
    myFixture.editor.selectionModel.removeSelection()

    assertDoesntChange { myFixture.editor.editorMode == FimStateMachine.Mode.INSERT }
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable, disable, and enable selection again`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    FimListenerManager.EditorListeners.add(myFixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(FimStateMachine.Mode.INSERT)

    // Fast add and remove selection
    myFixture.editor.selectionModel.setSelection(0, 10)
    myFixture.editor.selectionModel.removeSelection()
    myFixture.editor.selectionModel.setSelection(0, 10)

    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test switch from char to line visual mode`() {
    val text = """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """.trimIndent()
    configureByText(text)
    FimListenerManager.EditorListeners.add(myFixture.editor)
    typeText(injector.parser.parseKeys("i"))
    assertMode(FimStateMachine.Mode.INSERT)

    val range = text.rangeOf("Discovery")
    myFixture.editor.selectionModel.setSelection(range.startOffset, range.endOffset)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertEquals(FimStateMachine.SubMode.VISUAL_CHARACTER, myFixture.editor.subMode)

    val rangeLine = text.rangeOf("A Discovery\n")
    myFixture.editor.selectionModel.setSelection(rangeLine.startOffset, rangeLine.endOffset)
    waitAndAssert { myFixture.editor.subMode == FimStateMachine.SubMode.VISUAL_LINE }
  }
}
