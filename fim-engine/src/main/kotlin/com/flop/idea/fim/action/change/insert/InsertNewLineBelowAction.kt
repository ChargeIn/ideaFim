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
package com.flop.idea.fim.action.change.insert

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.Offset
import com.flop.idea.fim.handler.ChangeEditorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import java.util.*

class InsertNewLineBelowAction : ChangeEditorActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.INSERT

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_MULTIKEY_UNDO)

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (editor.isOneLineMode()) return false
//    if (experimentalApi()) {
    @Suppress("ConstantConditionIf")
    if (false) {
      injector.changeGroup.insertLineAround(editor, context, 1)
    } else {
      insertNewLineBelow(editor, context)
    }
    return true
  }
}

class InsertNewLineAboveAction : ChangeEditorActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.INSERT

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_MULTIKEY_UNDO)

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    argument: Argument?,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (editor.isOneLineMode()) return false
//    if (experimentalApi()) {
    @Suppress("ConstantConditionIf")
    if (false) {
      injector.changeGroup.insertLineAround(editor, context, 0)
    } else {
      insertNewLineAbove(editor, context)
    }
    return true
  }
}

private fun insertNewLineAbove(editor: FimEditor, context: ExecutionContext) {
  if (editor.isOneLineMode()) return

  // See also EditorStartNewLineBefore. That will move the caret to line start, call EditorEnter to create a new line,
  //   and then move up and call EditorLineEnd. We get better indent positioning by going to the line end of the
  //   previous line and hitting enter, especially with plain text files.
  // However, we'll use EditorStartNewLineBefore in PyCharm notebooks where the last character of the previous line
  //   may be locked with a guard

  // Note that we're deliberately bypassing MotionGroup.moveCaret to avoid side effects, most notably unncessary
  // scrolling
  val firstLiners: MutableSet<FimCaret> = HashSet()
  val moves: MutableSet<Pair<FimCaret, Int>> = HashSet()
  for (caret in editor.nativeCarets()) {
    val offset: Int
    if (caret.getVisualPosition().line == 0) {
      // Fake indenting for the first line. Works well for plain text to match the existing indent
      offset = injector.motion.moveCaretToLineStartSkipLeading(editor, caret)
      firstLiners.add(caret)
    } else {
      offset = injector.motion.moveCaretToLineEnd(editor, caret.getLogicalPosition().line - 1, true)
    }
    moves.add(Pair(caret, offset))
  }

  // Check if the "last character on previous line" has a guard
  // This is actively used in pycharm notebooks https://youtrack.jetbrains.com/issue/VIM-2495
  val hasGuards = moves.stream().anyMatch { (_, second): Pair<FimCaret?, Int?> ->
    editor.document.getOffsetGuard(
      Offset(
        second!!
      )
    ) != null
  }
  if (!hasGuards) {
    for ((first, second) in moves) {
      first.moveToOffsetNative(second)
    }
    injector.changeGroup.initInsert(editor, context, FimStateMachine.Mode.INSERT)
    injector.changeGroup.runEnterAction(editor, context)
    for (caret in editor.nativeCarets()) {
      if (firstLiners.contains(caret)) {
        val offset = injector.motion.moveCaretToLineEnd(editor, 0, true)
        injector.motion.moveCaret(editor, caret, offset)
      }
    }
  } else {
    injector.changeGroup.initInsert(editor, context, FimStateMachine.Mode.INSERT)
    injector.changeGroup.runEnterAboveAction(editor, context)
  }
  injector.motion.scrollCaretIntoView(editor)
}

/**
 * Begin insert after the current line by creating a new blank line below the current line
 * for all carets
 * @param editor  The editor to insert into
 * @param context The data context
 */
private fun insertNewLineBelow(editor: FimEditor, context: ExecutionContext) {
  if (editor.isOneLineMode()) return
  for (caret in editor.nativeCarets()) {
    caret.moveToOffset(injector.motion.moveCaretToLineEnd(editor, caret))
  }

  injector.changeGroup.initInsert(editor, context, FimStateMachine.Mode.INSERT)
  injector.changeGroup.runEnterAction(editor, context)
  injector.motion.scrollCaretIntoView(editor)
}
