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

package com.flop.idea.fim.fimscript.model.commands

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.ex.ExOutputModel
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * see "h :!"
 */
data class CmdFilterCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.SELF_SYNCHRONIZED)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    logger.debug("execute")
    val command = buildString {
      var inBackslash = false
      argument.forEach { c ->
        when {
          !inBackslash && c == '!' -> {
            val last = com.flop.idea.fim.FimPlugin.getProcess().lastCommand
            if (last.isNullOrEmpty()) {
              com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("e_noprev"))
              return ExecutionResult.Error
            }
            append(last)
          }
          !inBackslash && c == '%' -> {
            val virtualFile = com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor.ij)
            if (virtualFile == null) {
              // Note that we use a slightly different error message to Fim, because we don't support alternate files or file
              // name modifiers. (I also don't know what the :p:h means)
              // (Fim) E499: Empty file name for '%' or '#', only works with ":p:h"
              // (IdeaFim) E499: Empty file name for '%'
              com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("E499"))
              return ExecutionResult.Error
            }
            append(virtualFile.path)
          }
          else -> append(c)
        }

        inBackslash = c == '\\'
      }
    }

    if (command.isEmpty()) {
      return ExecutionResult.Error
    }

    val workingDirectory = editor.ij.project?.basePath
    return try {
      if (ranges.size() == 0) {
        // Show command output in a window
        com.flop.idea.fim.FimPlugin.getProcess().executeCommand(editor, command, null, workingDirectory)?.let {
          ExOutputModel.getInstance(editor.ij).output(it)
        }
        ExecutionResult.Success
      } else {
        // Filter
        val range = this.getTextRange(editor, false)
        val input = editor.ij.document.charsSequence.subSequence(range.startOffset, range.endOffset)
        com.flop.idea.fim.FimPlugin.getProcess().executeCommand(editor, command, input, workingDirectory)?.let {
          ApplicationManager.getApplication().runWriteAction {
            val start = editor.offsetToLogicalPosition(range.startOffset)
            val end = editor.offsetToLogicalPosition(range.endOffset)
            editor.ij.document.replaceString(range.startOffset, range.endOffset, it)
            val linesFiltered = end.line - start.line
            if (linesFiltered > 2) {
              com.flop.idea.fim.FimPlugin.showMessage("$linesFiltered lines filtered")
            }
          }
        }
        ExecutionResult.Success
      }
    } catch (e: ProcessCanceledException) {
      throw ExException("Command terminated")
    } catch (e: Exception) {
      throw ExException(e.message)
    }
  }

  companion object {
    private val logger = Logger.getInstance(CmdFilterCommand::class.java.name)
  }
}
