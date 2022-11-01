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
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.handler.FimActionHandler
import com.flop.idea.fim.put.PutData
import com.flop.idea.fim.register.Register
import com.flop.idea.fim.fimscript.model.Script

class InsertRegisterAction : FimActionHandler.SingleExecution() {
  override val type: Command.Type = Command.Type.INSERT

  override val argumentType: Argument.Type = Argument.Type.CHARACTER

  override fun execute(
    editor: FimEditor,
    context: ExecutionContext,
    cmd: Command,
    operatorArguments: OperatorArguments,
  ): Boolean {
    val argument = cmd.argument

    if (argument?.character == '=') {
      injector.application.invokeLater {
        try {
          val expression = readExpression(editor)
          if (expression != null) {
            if (expression.isNotEmpty()) {
              val expressionValue =
                injector.fimscriptParser.parseExpression(expression)?.evaluate(editor, context, Script(listOf()))
                  ?: throw ExException("E15: Invalid expression: $expression")
              val textToStore = expressionValue.toInsertableString()
              injector.registerGroup.storeTextSpecial('=', textToStore)
            }
            insertRegister(editor, context, argument.character, operatorArguments)
          }
        } catch (e: ExException) {
          injector.messages.indicateError()
          injector.messages.showStatusBarMessage(e.message)
        }
      }
      return true
    } else {
      return argument != null && insertRegister(editor, context, argument.character, operatorArguments)
    }
  }

  private fun readExpression(editor: FimEditor): String? {
    return injector.commandLineHelper.inputString(editor, "=", null)
  }
}

/**
 * Inserts the contents of the specified register
 *
 * @param editor  The editor to insert the text into
 * @param context The data context
 * @param key     The register name
 * @return true if able to insert the register contents, false if not
 */
private fun insertRegister(
  editor: FimEditor,
  context: ExecutionContext,
  key: Char,
  operatorArguments: OperatorArguments
): Boolean {
  val register: Register? = injector.registerGroup.getRegister(key)
  if (register != null) {
    val text = register.rawText ?: injector.parser.toPrintableString(register.keys)
    val textData = PutData.TextData(text, SelectionType.CHARACTER_WISE, emptyList())
    val putData = PutData(textData, null, 1, insertTextBeforeCaret = true, rawIndent = true, caretAfterInsertedText = true)
    injector.put.putText(editor, context, putData, operatorArguments = operatorArguments)
    return true
  }
  return false
}
