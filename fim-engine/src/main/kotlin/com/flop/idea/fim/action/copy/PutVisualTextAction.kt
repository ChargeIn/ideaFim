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

package com.flop.idea.fim.action.copy

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.CommandFlags
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.handler.VisualOperatorActionHandler
import com.flop.idea.fim.helper.enumSetOf
import com.flop.idea.fim.put.PutData
import java.util.*

/**
 * @author vlan
 */
sealed class PutVisualTextBaseAction(
  private val insertTextBeforeCaret: Boolean,
  private val indent: Boolean,
  private val caretAfterInsertedText: Boolean,
) : VisualOperatorActionHandler.SingleExecution() {

  override val type: Command.Type = Command.Type.OTHER_SELF_SYNCHRONIZED

  override val flags: EnumSet<CommandFlags> = enumSetOf(CommandFlags.FLAG_EXIT_VISUAL)

  override fun executeForAllCarets(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    caretsAndSelections: Map<FimCaret, FimSelection>,
    operatorArguments: OperatorArguments,
  ): Boolean {
    if (caretsAndSelections.isEmpty()) return false
    val count = cmd.count
    val caretToPutData = editor.sortedCarets().associateWith { getPutDataForCaret(it, caretsAndSelections[it], count) }
    injector.registerGroup.resetRegister()
    var result = true
    injector.application.runWriteAction {
      caretToPutData.forEach {
        result = injector.put.putTextForCaret(editor, it.key, context, it.value, true) && result
      }
    }
    return result
  }

  private fun getPutDataForCaret(caret: FimCaret, selection: FimSelection?, count: Int): PutData {
    val lastRegisterChar = injector.registerGroup.lastRegisterChar
    val register = caret.registerStorage.getRegister(caret, lastRegisterChar)
    val textData = register?.let {
      PutData.TextData(
        register.text ?: injector.parser.toPrintableString(register.keys),
        register.type,
        register.transferableData
      )
    }
    val visualSelection = selection?.let { PutData.VisualSelection(mapOf(caret to it), it.type) }
    return PutData(textData, visualSelection, count, insertTextBeforeCaret, indent, caretAfterInsertedText)
  }
}

class PutVisualTextBeforeCursorAction : PutVisualTextBaseAction(insertTextBeforeCaret = true, indent = true, caretAfterInsertedText = false)
class PutVisualTextAfterCursorAction : PutVisualTextBaseAction(insertTextBeforeCaret = false, indent = true, caretAfterInsertedText = false)

class PutVisualTextBeforeCursorNoIndentAction : PutVisualTextBaseAction(insertTextBeforeCaret = true, indent = false, caretAfterInsertedText = false)
class PutVisualTextAfterCursorNoIndentAction : PutVisualTextBaseAction(insertTextBeforeCaret = false, indent = false, caretAfterInsertedText = false)

class PutVisualTextBeforeCursorMoveCursorAction : PutVisualTextBaseAction(insertTextBeforeCaret = true, indent = true, caretAfterInsertedText = true)
class PutVisualTextAfterCursorMoveCursorAction : PutVisualTextBaseAction(insertTextBeforeCaret = false, indent = true, caretAfterInsertedText = true)
