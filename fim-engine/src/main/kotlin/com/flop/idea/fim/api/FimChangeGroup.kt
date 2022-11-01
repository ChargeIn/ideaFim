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
package com.flop.idea.fim.api

import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.ex.ranges.LineRange
import com.flop.idea.fim.group.visual.FimSelection
import org.jetbrains.annotations.TestOnly
import javax.swing.KeyStroke

interface FimChangeGroup {
  fun setInsertRepeat(lines: Int, column: Int, append: Boolean)

  fun insertBeforeCursor(editor: FimEditor, context: ExecutionContext)

  fun insertBeforeFirstNonBlank(editor: FimEditor, context: ExecutionContext)

  fun insertLineStart(editor: FimEditor, context: ExecutionContext)

  fun insertAfterCursor(editor: FimEditor, context: ExecutionContext)

  fun insertAfterLineEnd(editor: FimEditor, context: ExecutionContext)

  fun insertPreviousInsert(editor: FimEditor, context: ExecutionContext, exit: Boolean, operatorArguments: OperatorArguments)

  fun insertLineAround(editor: FimEditor, context: ExecutionContext, shift: Int)

  fun initInsert(editor: FimEditor, context: ExecutionContext, mode: FimStateMachine.Mode)

  fun processEscape(editor: FimEditor, context: ExecutionContext?, operatorArguments: OperatorArguments)

  fun processEnter(editor: FimEditor, context: ExecutionContext)

  fun processPostChangeModeSwitch(editor: FimEditor, context: ExecutionContext, toSwitch: FimStateMachine.Mode)

  fun processCommand(editor: FimEditor, cmd: Command)

  fun deleteCharacter(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    isChange: Boolean,
    operatorArguments: OperatorArguments
  ): Boolean

  fun processSingleCommand(editor: FimEditor)

  fun deleteEndOfLine(editor: FimEditor, caret: FimCaret, count: Int, operatorArguments: OperatorArguments): Boolean

  fun deleteJoinLines(
    editor: FimEditor,
    caret: FimCaret,
    count: Int,
    spaces: Boolean,
    operatorArguments: OperatorArguments
  ): Boolean

  fun processKey(editor: FimEditor, context: ExecutionContext, key: KeyStroke): Boolean

  fun processKeyInSelectMode(editor: FimEditor, context: ExecutionContext, key: KeyStroke): Boolean

  fun deleteLine(editor: FimEditor, caret: FimCaret, count: Int, operatorArguments: OperatorArguments): Boolean

  fun deleteJoinRange(
    editor: FimEditor,
    caret: FimCaret,
    range: TextRange,
    spaces: Boolean,
    operatorArguments: OperatorArguments
  ): Boolean

  fun joinViaIdeaByCount(editor: FimEditor, context: ExecutionContext, count: Int): Boolean

  fun joinViaIdeaBySelections(editor: FimEditor, context: ExecutionContext, caretsAndSelections: Map<FimCaret, FimSelection>)

  fun getDeleteRangeAndType(editor: FimEditor, caret: FimCaret, context: ExecutionContext, argument: Argument, isChange: Boolean, operatorArguments: OperatorArguments): Pair<TextRange, SelectionType>?

  fun getDeleteRangeAndType2(editor: FimEditor, caret: FimCaret, context: ExecutionContext, argument: Argument, isChange: Boolean, operatorArguments: OperatorArguments): Pair<TextRange, SelectionType>?

  fun deleteRange(
    editor: FimEditor,
    caret: FimCaret,
    range: TextRange,
    type: SelectionType?,
    isChange: Boolean,
    operatorArguments: OperatorArguments
  ): Boolean
  fun deleteRange2(editor: FimEditor, caret: FimCaret, range: TextRange, type: SelectionType): Boolean

  fun changeCharacters(editor: FimEditor, caret: FimCaret, operatorArguments: OperatorArguments): Boolean

  fun changeEndOfLine(editor: FimEditor, caret: FimCaret, count: Int, operatorArguments: OperatorArguments): Boolean

  fun changeMotion(editor: FimEditor, caret: FimCaret, context: ExecutionContext, argument: Argument, operatorArguments: OperatorArguments): Boolean

  fun changeCaseToggleCharacter(editor: FimEditor, caret: FimCaret, count: Int): Boolean

  fun blockInsert(editor: FimEditor, context: ExecutionContext, range: TextRange, append: Boolean, operatorArguments: OperatorArguments): Boolean

  fun changeCaseRange(editor: FimEditor, caret: FimCaret, range: TextRange, type: Char): Boolean

  fun changeRange(
    editor: FimEditor,
    caret: FimCaret,
    range: TextRange,
    type: SelectionType,
    context: ExecutionContext?,
    operatorArguments: OperatorArguments
  ): Boolean

  fun changeCaseMotion(editor: FimEditor, caret: FimCaret, context: ExecutionContext?, type: Char, argument: Argument, operatorArguments: OperatorArguments): Boolean

  fun reformatCodeMotion(editor: FimEditor, caret: FimCaret, context: ExecutionContext?, argument: Argument, operatorArguments: OperatorArguments): Boolean

  fun reformatCodeSelection(editor: FimEditor, caret: FimCaret, range: FimSelection)

  fun autoIndentMotion(editor: FimEditor, caret: FimCaret, context: ExecutionContext, argument: Argument, operatorArguments: OperatorArguments)

  fun autoIndentRange(editor: FimEditor, caret: FimCaret, context: ExecutionContext, range: TextRange)

  fun indentLines(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    lines: Int,
    dir: Int,
    operatorArguments: OperatorArguments
  )

  fun insertText(editor: FimEditor, caret: FimCaret, offset: Int, str: String)

  fun insertText(editor: FimEditor, caret: FimCaret, str: String)

  fun indentMotion(editor: FimEditor, caret: FimCaret, context: ExecutionContext, argument: Argument, dir: Int, operatorArguments: OperatorArguments)

  fun indentRange(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    range: TextRange,
    count: Int,
    dir: Int,
    operatorArguments: OperatorArguments
  )

  fun changeNumberVisualMode(editor: FimEditor, caret: FimCaret, selectedRange: TextRange, count: Int, avalanche: Boolean): Boolean

  fun changeNumber(editor: FimEditor, caret: FimCaret, count: Int): Boolean

  fun sortRange(editor: FimEditor, range: LineRange, lineComparator: Comparator<String>): Boolean

  fun reset()

  fun saveStrokes(newStrokes: String?)

  @TestOnly
  fun resetRepeat()
  fun notifyListeners(editor: FimEditor)
  fun runEnterAction(editor: FimEditor, context: ExecutionContext)
  fun runEnterAboveAction(editor: FimEditor, context: ExecutionContext)

  /**
   * This repeats the previous insert count times
   *
   * @param editor  The editor to insert into
   * @param context The data context
   * @param count   The number of times to repeat the previous insert
   */
  fun repeatInsert(
    editor: FimEditor,
    context: ExecutionContext,
    count: Int,
    started: Boolean,
    operatorArguments: OperatorArguments,
  )

  fun type(fimEditor: FimEditor, context: ExecutionContext, key: Char)
  fun replaceText(editor: FimEditor, start: Int, end: Int, str: String)
}
