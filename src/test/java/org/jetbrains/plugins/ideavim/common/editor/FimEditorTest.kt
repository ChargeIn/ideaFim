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

package org.jetbrains.plugins.ideafim.common.editor

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.command.WriteCommandAction
import com.flop.idea.fim.common.offset
import com.flop.idea.fim.newapi.IjFimEditor
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class FimEditorTest : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test delete string`() {
    configureByText("01234567890")
    val fimEditor = IjFimEditor(myFixture.editor)
    WriteCommandAction.runWriteCommandAction(myFixture.project) {
      runWriteAction {
        fimEditor.deleteRange(0.offset, 5.offset)
      }
    }
    assertState("567890")
  }
}
