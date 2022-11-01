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

@file:JvmName("ModeHelper")

package com.flop.idea.fim.helper

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.listener.SelectionFimListenerSuppressor
import com.flop.idea.fim.newapi.IjExecutionContext
import com.flop.idea.fim.newapi.IjFimCaret
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.fim

/**
 * Pop all modes, but leave editor state. E.g. editor selection is not removed.
 */
fun Editor.popAllModes() {
  val commandState = this.fim.fimStateMachine
  while (commandState.mode != FimStateMachine.Mode.COMMAND) {
    commandState.popModes()
  }
}

@RWLockLabel.NoLockRequired
fun Editor.exitVisualMode() {
  val selectionType = SelectionType.fromSubMode(this.subMode)
  SelectionFimListenerSuppressor.lock().use {
    if (inBlockSubMode) {
      this.caretModel.removeSecondaryCarets()
    }
    if (!this.fimKeepingVisualOperatorAction) {
      this.caretModel.allCarets.forEach(Caret::removeSelection)
    }
  }
  if (this.inVisualMode) {
    this.fimLastSelectionType = selectionType
    val primaryCaret = this.caretModel.primaryCaret
    val fimSelectionStart = primaryCaret.fimSelectionStart
    com.flop.idea.fim.FimPlugin.getMark().setVisualSelectionMarks(this.fim, TextRange(fimSelectionStart, primaryCaret.offset))
    this.caretModel.allCarets.forEach { it.fimSelectionStartClear() }

    this.fim.fimStateMachine.popModes()
  }
}

/** [adjustCaretPosition] - if true, caret will be moved one char left if it's on the line end */
fun Editor.exitSelectMode(adjustCaretPosition: Boolean) {
  if (!this.inSelectMode) return

  this.fim.fimStateMachine.popModes()
  SelectionFimListenerSuppressor.lock().use {
    this.caretModel.allCarets.forEach {
      it.removeSelection()
      it.fimSelectionStartClear()
      if (adjustCaretPosition) {
        val lineEnd = com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(this, it.offset)
        val lineStart = com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(this, it.offset)
        if (it.offset == lineEnd && it.offset != lineStart) {
          it.moveToInlayAwareOffset(it.offset - 1)
        }
      }
    }
  }
}

/** [adjustCaretPosition] - if true, caret will be moved one char left if it's on the line end */
fun FimEditor.exitSelectMode(adjustCaretPosition: Boolean) {
  if (!this.inSelectMode) return

  this.fimStateMachine.popModes()
  SelectionFimListenerSuppressor.lock().use {
    this.carets().forEach { fimCaret ->
      val caret = (fimCaret as IjFimCaret).caret
      caret.removeSelection()
      caret.fimSelectionStartClear()
      if (adjustCaretPosition) {
        val lineEnd = com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset((this as IjFimEditor).editor, caret.offset)
        val lineStart = com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(this.editor, caret.offset)
        if (caret.offset == lineEnd && caret.offset != lineStart) {
          caret.moveToInlayAwareOffset(caret.offset - 1)
        }
      }
    }
  }
}

fun Editor.exitInsertMode(context: DataContext, operatorArguments: OperatorArguments) {
  com.flop.idea.fim.FimPlugin.getChange().processEscape(IjFimEditor(this), IjExecutionContext(context), operatorArguments)
}
