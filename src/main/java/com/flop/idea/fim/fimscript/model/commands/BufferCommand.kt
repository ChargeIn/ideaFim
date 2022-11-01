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

import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * Handles buffer, buf, bu, b.
 *
 * @author John Weigel
 */
data class BufferCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val arg = argument.trim()
    val overrideModified = arg.startsWith('!')
    val buffer = if (overrideModified) arg.replace(Regex("^!\\s*"), "") else arg
    var result = true

    if (buffer.isNotEmpty()) {
      if (buffer.matches(Regex("^\\d+$"))) {
        val bufNum = buffer.toInt() - 1

        if (!com.flop.idea.fim.FimPlugin.getFile().selectFile(bufNum, context)) {
          com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("buffer.0.does.not.exist", bufNum))
          result = false
        }
      } else if (buffer == "#") {
        com.flop.idea.fim.FimPlugin.getFile().selectPreviousTab(context)
      } else {
        val editors = findPartialMatch(context, buffer)

        when (editors.size) {
          0 -> {
            com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("no.matching.buffer.for.0", buffer))
            result = false
          }
          1 -> {
            if (com.flop.idea.fim.helper.EditorHelper.hasUnsavedChanges(editor.ij) && !overrideModified) {
              com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("no.write.since.last.change.add.to.override"))
              result = false
            } else {
              com.flop.idea.fim.FimPlugin.getFile().openFile(com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editors[0].ij)!!.name, context)
            }
          }
          else -> {
            com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("more.than.one.match.for.0", buffer))
            result = false
          }
        }
      }
    }

    return if (result) ExecutionResult.Success else ExecutionResult.Error
  }

  private fun findPartialMatch(context: ExecutionContext, fileName: String): List<FimEditor> {
    val matchedFiles = mutableListOf<FimEditor>()
    val project = PlatformDataKeys.PROJECT.getData(context.ij) ?: return matchedFiles

    for (file in FileEditorManager.getInstance(project).openFiles) {
      if (file.name.contains(fileName)) {
        val editor = com.flop.idea.fim.helper.EditorHelper.getEditor(file) ?: continue
        matchedFiles.add(editor.fim)
      }
    }

    return matchedFiles
  }
}
