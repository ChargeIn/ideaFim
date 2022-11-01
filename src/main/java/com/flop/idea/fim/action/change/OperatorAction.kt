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
package com.flop.idea.fim.action.change

import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.common.argumentCaptured
import com.flop.idea.fim.group.MotionGroup
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.handler.VisualOperatorActionHandler
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.helper.enumSetOf
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.ij
import java.util.*

private fun doOperatorAction(editor: FimEditor, context: ExecutionContext, textRange: TextRange, selectionType: SelectionType): Boolean {
  val operatorFunction = injector.keyGroup.operatorFunction
  if (operatorFunction == null) {
    com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("E774"))
    return false
  }

  val saveRepeatHandler = FimRepeater.repeatHandler
  com.flop.idea.fim.FimPlugin.getMark().setChangeMarks(editor, textRange)
  KeyHandler.getInstance().reset(editor)
  val result = operatorFunction.apply(editor, context, selectionType)
  FimRepeater.repeatHandler = saveRepeatHandler
  return result
}

class OperatorAction : FimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override val argumentType: Argument.Type = Argument.Type.MOTION

  override fun execute(editor: FimEditor, context: ExecutionContext, cmd: Command, operatorArguments: OperatorArguments): Boolean {
    val argument = cmd.argument ?: return false
    if (!editor.fimStateMachine.isDotRepeatInProgress) {
      argumentCaptured = argument
    }
    val range = getMotionRange(editor, context, argument, operatorArguments)

    if (range != null) {
      val selectionType = if (argument.motion.isLinewiseMotion()) {
        SelectionType.LINE_WISE
      } else {
        SelectionType.CHARACTER_WISE
      }
      return doOperatorAction(editor, context, range, selectionType)
    }
    return false
  }

  private fun getMotionRange(
    editor: FimEditor,
    context: ExecutionContext,
    argument: Argument,
    operatorArguments: OperatorArguments
  ): TextRange? {

    // Note that we're using getMotionRange2 in order to avoid normalising the linewise range into line start
    // offsets that will be used to set the change marks. This affects things like the location of the caret in the
    // Commentary extension
    val ijEditor = editor.ij
    return com.flop.idea.fim.group.MotionGroup.getMotionRange2(
      ijEditor,
      ijEditor.caretModel.primaryCaret,
      context.ij,
      argument,
      operatorArguments
    )?.normalize()?.let {

      // If we're linewise, make sure the end offset isn't just the EOL char
      if (argument.motion.isLinewiseMotion() && it.endOffset < editor.fileSize()) {
        TextRange(it.startOffset, it.endOffset + 1)
      } else {
        it
      }
    }
  }
}

class VisualOperatorAction : VisualOperatorActionHandler.ForEachCaret() {
  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_EXIT_VISUAL)

  override fun executeAction(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    cmd: Command,
    range: FimSelection,
    operatorArguments: OperatorArguments,
  ): Boolean {
    return doOperatorAction(editor, context, range.toFimTextRange(), range.type)
  }
}
