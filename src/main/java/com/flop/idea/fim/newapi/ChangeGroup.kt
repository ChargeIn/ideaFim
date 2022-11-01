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

package com.flop.idea.fim.newapi

import com.intellij.codeInsight.editorActions.EnterHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.psi.util.PsiUtilBase
import com.intellij.util.text.CharArrayUtil
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.LineDeleteShift
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimChangeGroupBase
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimMotionGroupBase
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.EditorLine
import com.flop.idea.fim.common.IndentConfig
import com.flop.idea.fim.common.OperatedRange
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.common.FimRange
import com.flop.idea.fim.common.including
import com.flop.idea.fim.common.offset
import com.flop.idea.fim.group.MotionGroup
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.inlayAwareVisualColumn
import com.flop.idea.fim.helper.fimChangeActionSwitchMode
import com.flop.idea.fim.helper.fimLastColumn
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope

fun changeRange(
  editor: Editor,
  caret: Caret,
  range: TextRange,
  type: SelectionType,
  context: DataContext,
) {
  val fimEditor = IjFimEditor(editor)
  val fimRange = toFimRange(range, type)

  var col = 0
  var lines = 0
  if (type === SelectionType.BLOCK_WISE) {
    lines = FimChangeGroupBase.getLinesCountInVisualBlock(IjFimEditor(editor), range)
    col = editor.offsetToLogicalPosition(range.startOffset).column
    if (caret.fimLastColumn == FimMotionGroupBase.LAST_COLUMN) {
      col = FimMotionGroupBase.LAST_COLUMN
    }
  }

  // Remove the range
  val fimCaret = IjFimCaret(caret)
  val indent = editor.offsetToLogicalPosition(fimEditor.indentForLine(fimCaret.getLine().line)).column
  val deletedInfo = injector.fimMachine.delete(fimRange, fimEditor, fimCaret)
  if (deletedInfo != null) {
    if (deletedInfo is OperatedRange.Lines) {
      // Add new line in case of linewise motion
      val existingLine = if (fimEditor.fileSize() != 0L) {
        if (deletedInfo.shiftType != LineDeleteShift.NO_NL) {
          fimEditor.addLine(deletedInfo.lineAbove)
        } else {
          EditorLine.Pointer.init(deletedInfo.lineAbove.line, fimEditor)
        }
      } else {
        EditorLine.Pointer.init(0, fimEditor)
      }

      val offset = fimCaret.offsetForLineWithStartOfLineOption(existingLine)
      // TODO: 29.12.2021 IndentConfig is not abstract
      val indentText = IndentConfig.create(editor).createIndentBySize(indent)
      fimEditor.insertText(offset.offset, indentText)
      val caretOffset = offset + indentText.length
      fimCaret.moveToOffset(caretOffset)
      com.flop.idea.fim.FimPlugin.getChange().insertBeforeCursor(editor.fim, context.fim)
    } else {
      when (deletedInfo) {
        is OperatedRange.Characters -> {
          fimCaret.moveToOffset(deletedInfo.leftOffset.point)
        }
        is OperatedRange.Block -> TODO()
        else -> TODO()
      }
      if (type == SelectionType.BLOCK_WISE) {
        com.flop.idea.fim.FimPlugin.getChange().setInsertRepeat(lines, col, false)
      }
      editor.fimChangeActionSwitchMode = FimStateMachine.Mode.INSERT
    }
  } else {
    com.flop.idea.fim.FimPlugin.getChange().insertBeforeCursor(editor.fim, context.fim)
  }
}

fun deleteRange(
  editor: FimEditor,
  caret: FimCaret,
  range: TextRange,
  type: SelectionType,
): Boolean {
  val fimRange = toFimRange(range, type)

  (caret as IjFimCaret).caret.fimLastColumn = caret.caret.inlayAwareVisualColumn
  val deletedInfo = injector.fimMachine.delete(fimRange, editor, caret)
  if (deletedInfo != null) {
    when (deletedInfo) {
      is OperatedRange.Characters -> {
        val newOffset = injector.engineEditorHelper.normalizeOffset(editor, deletedInfo.leftOffset.point, false)
        caret.moveToOffset(newOffset)
      }
      is OperatedRange.Block -> TODO()
      is OperatedRange.Lines -> {
        if (deletedInfo.shiftType != LineDeleteShift.NL_ON_START) {
          val line = deletedInfo.lineAbove.toPointer(editor)
          val offset = caret.offsetForLineWithStartOfLineOption(line)
          caret.moveToOffset(offset)
        } else {
          val logicalLine = EditorLine.Pointer.init((deletedInfo.lineAbove.line - 1).coerceAtLeast(0), editor)
          val offset = caret.offsetForLineWithStartOfLineOption(logicalLine)
          caret.moveToOffset(offset)
        }
      }
    }
  }
  return deletedInfo != null
}

/**
 * XXX: This implementation is incorrect!
 *
 * Known issues of this code:
 * - Indent is incorrect when `o` for kotlin code like
 *   ```
 *   if (true) {
 *   }
 *   ```
 *   This is probably the kotlin issue, but still
 * - `*` character doesn't appear when `o` in javadoc section
 */
fun insertLineAround(editor: FimEditor, context: ExecutionContext, shift: Int) {
  val project = (editor as IjFimEditor).editor.project

  com.flop.idea.fim.FimPlugin.getChange().initInsert(editor, context, FimStateMachine.Mode.INSERT)

  if (!FimStateMachine.getInstance(editor).isDotRepeatInProgress) {
    for (fimCaret in editor.carets()) {
      val caret = (fimCaret as IjFimCaret).caret
      val line = fimCaret.getLine()

      // Current line indent
      val lineStartOffset = editor.getLineRange(line).first.point
      val text = editor.editor.document.charsSequence
      val lineStartWsEndOffset = CharArrayUtil.shiftForward(text, lineStartOffset, " \t")
      val indent = text.subSequence(lineStartOffset, lineStartWsEndOffset)

      // Calculating next line with minding folders
      val lineEndOffset = if (shift == 1) {
        com.flop.idea.fim.FimPlugin.getMotion().moveCaretToLineEnd(editor, IjFimCaret(caret))
      } else {
        com.flop.idea.fim.FimPlugin.getMotion().moveCaretToLineStart(editor, caret.fim)
      }
      val position = EditorLine.Offset.init(editor.offsetToLogicalPosition(lineEndOffset).line + shift, editor)

      val insertedLine = editor.addLine(position)
      com.flop.idea.fim.FimPlugin.getChange().saveStrokes("\n")

      var lineStart = editor.getLineRange(insertedLine).first
      val initialLineStart = lineStart

      // Set up indent
      // Firstly set up primitive indent
      editor.insertText(lineStart, indent)
      lineStart = (lineStart.point + indent.length).offset

      if (project != null) {
        // Secondly set up language smart indent
        val language = PsiUtilBase.getLanguageInEditor(caret, project)
        val newIndent = EnterHandler.adjustLineIndentNoCommit(language, editor.editor.document, editor.editor, lineStart.point)
        lineStart = if (newIndent >= 0) newIndent.offset else lineStart
      }
      com.flop.idea.fim.FimPlugin.getChange()
        .saveStrokes(
          editor.editor.document.getText(
            com.intellij.openapi.util.TextRange(
              initialLineStart.point,
              lineStart.point
            )
          )
        )

      fimCaret.moveToOffset(lineStart.point)
    }
  }

  com.flop.idea.fim.group.MotionGroup.scrollCaretIntoView(editor.editor)
}

fun FimCaret.offsetForLineWithStartOfLineOption(logicalLine: EditorLine.Pointer): Int {
  return if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.LOCAL(editor), OptionConstants.startoflineName)) {
    offsetForLineStartSkipLeading(logicalLine.line)
  } else {
    com.flop.idea.fim.FimPlugin.getMotion().moveCaretToLineWithSameColumn(editor, logicalLine.line, this)
  }
}

fun FimEditor.indentForLine(line: Int): Int {
  val editor = (this as IjFimEditor).editor
  return com.flop.idea.fim.helper.EditorHelper.getLeadingCharacterOffset(editor, line)
}

fun toFimRange(range: TextRange, type: SelectionType): FimRange {
  return when (type) {
    SelectionType.LINE_WISE -> {
      FimRange.Line.Offsets(range.startOffset.offset, range.endOffset.offset)
    }
    SelectionType.CHARACTER_WISE -> FimRange.Character.Range(range.startOffset including range.endOffset)
    SelectionType.BLOCK_WISE -> FimRange.Block(range.startOffset.offset, range.endOffset.offset)
  }
}
