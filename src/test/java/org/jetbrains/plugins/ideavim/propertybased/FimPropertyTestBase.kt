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

package org.jetbrains.plugins.ideafim.propertybased

import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim
import org.jetbrains.jetCheck.Generator
import org.jetbrains.jetCheck.ImperativeCommand
import org.jetbrains.plugins.ideafim.FimTestCase

abstract class FimPropertyTestBase : FimTestCase() {
  protected fun moveCaretToRandomPlace(env: ImperativeCommand.Environment, editor: Editor) {
    val pos = env.generateValue(Generator.integers(0, editor.document.textLength - 1), "Put caret at position %s")
    com.flop.idea.fim.group.MotionGroup.moveCaret(editor, editor.caretModel.currentCaret, pos)
  }

  protected fun reset(editor: Editor) {
    editor.fim.fimStateMachine.mappingState.resetMappingSequence()
    com.flop.idea.fim.FimPlugin.getKey().resetKeyMappings()

    KeyHandler.getInstance().fullReset(editor.fim)
    com.flop.idea.fim.FimPlugin.getRegister().resetRegisters()
    editor.caretModel.runForEachCaret { it.moveToOffset(0) }

    editor.fim.fimStateMachine.resetDigraph()
    com.flop.idea.fim.FimPlugin.getSearch().resetState()
    com.flop.idea.fim.FimPlugin.getChange().reset()
  }
}
