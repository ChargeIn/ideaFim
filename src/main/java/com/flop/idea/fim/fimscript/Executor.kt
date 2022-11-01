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

package com.flop.idea.fim.fimscript

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.textarea.TextComponentEditorImpl
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimScriptExecutorBase
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.FinishException
import com.flop.idea.fim.history.HistoryConstants
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.register.RegisterConstants.LAST_COMMAND_REGISTER
import com.flop.idea.fim.fimscript.model.CommandLineFimLContext
import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.commands.Command
import com.flop.idea.fim.fimscript.model.commands.RepeatCommand
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import java.io.File
import java.io.IOException
import javax.swing.JTextArea

@Service
class Executor : FimScriptExecutorBase() {
  private val logger = logger<Executor>()
  override var executingFimscript = false

  @Throws(ExException::class)
  override fun execute(script: String, editor: FimEditor, context: ExecutionContext, skipHistory: Boolean, indicateErrors: Boolean, fimContext: FimLContext?): ExecutionResult {
    var finalResult: ExecutionResult = ExecutionResult.Success

    val myScript = FimscriptParser.parse(script)
    myScript.units.forEach { it.fimContext = fimContext ?: myScript }

    for (unit in myScript.units) {
      try {
        val result = unit.execute(editor, context)
        if (result is ExecutionResult.Error) {
          finalResult = ExecutionResult.Error
          if (indicateErrors) {
            com.flop.idea.fim.FimPlugin.indicateError()
          }
        }
      } catch (e: ExException) {
        if (e is FinishException) {
          break
        }
        finalResult = ExecutionResult.Error
        if (indicateErrors) {
          com.flop.idea.fim.FimPlugin.showMessage(e.message)
          com.flop.idea.fim.FimPlugin.indicateError()
        } else {
          logger.warn("Failed while executing $unit. " + e.message)
        }
      } catch (e: NotImplementedError) {
        if (indicateErrors) {
          com.flop.idea.fim.FimPlugin.showMessage("Not implemented yet :(")
          com.flop.idea.fim.FimPlugin.indicateError()
        }
      } catch (e: Exception) {
        logger.warn("Caught: ${e.message}")
        logger.warn(e.stackTrace.toString())
        if (injector.application.isUnitTest()) {
          throw e
        }
      }
    }

    if (!skipHistory) {
      com.flop.idea.fim.FimPlugin.getHistory().addEntry(HistoryConstants.COMMAND, script)
      if (myScript.units.size == 1 && myScript.units[0] is Command && myScript.units[0] !is RepeatCommand) {
        com.flop.idea.fim.FimPlugin.getRegister().storeTextSpecial(LAST_COMMAND_REGISTER, script)
      }
    }
    return finalResult
  }

  override fun execute(script: String, skipHistory: Boolean) {
    val editor = TextComponentEditorImpl(null, JTextArea()).fim
    val context = DataContext.EMPTY_CONTEXT.fim
    execute(script, editor, context, skipHistory, indicateErrors = true, CommandLineFimLContext)
  }

  override fun executeFile(file: File, indicateErrors: Boolean) {
    val editor = TextComponentEditorImpl(null, JTextArea()).fim
    val context = DataContext.EMPTY_CONTEXT.fim
    try {
      execute(file.readText(), editor, context, skipHistory = true, indicateErrors)
    } catch (ignored: IOException) { }
  }

  @Throws(ExException::class)
  override fun executeLastCommand(editor: FimEditor, context: ExecutionContext): Boolean {
    val reg = com.flop.idea.fim.FimPlugin.getRegister().getRegister(':') ?: return false
    val text = reg.text ?: return false
    execute(text, editor, context, skipHistory = false, indicateErrors = true, CommandLineFimLContext)
    return true
  }
}
