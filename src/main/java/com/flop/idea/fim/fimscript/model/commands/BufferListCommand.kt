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
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ExOutputModel
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.fimLine
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.fimscript.model.ExecutionResult
import org.jetbrains.annotations.NonNls

/**
 * Handles buffers, files, ls command. Supports +, =, a, %, # filters.
 *
 * @author John Weigel
 */
data class BufferListCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)

  companion object {
    const val FILE_NAME_PAD = 30
    val SUPPORTED_FILTERS = setOf('+', '=', 'a', '%', '#')
  }

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val arg = argument.trim()
    val filter = pruneUnsupportedFilters(arg)
    val bufferList = getBufferList(context, filter)

    ExOutputModel.getInstance(editor.ij).output(bufferList.joinToString(separator = "\n"))

    return ExecutionResult.Success
  }

  private fun pruneUnsupportedFilters(filter: String) = filter.filter { it in SUPPORTED_FILTERS }

  private fun getBufferList(context: ExecutionContext, filter: String): List<String> {
    val bufferList = mutableListOf<String>()
    val project = PlatformDataKeys.PROJECT.getData(context.ij) ?: return emptyList()

    val fem = FileEditorManager.getInstance(project)
    val openFiles = fem.openFiles
    val bufNumPad = openFiles.size.toString().length
    val currentFile = fem.selectedFiles[0]
    val previousFile = com.flop.idea.fim.FimPlugin.getFile().getPreviousTab(context.ij)
    val virtualFileDisplayMap = buildVirtualFileDisplayMap(project)

    var index = 1
    for ((file, displayFileName) in virtualFileDisplayMap) {
      val editor = com.flop.idea.fim.helper.EditorHelper.getEditor(file) ?: continue

      val bufStatus = getBufferStatus(editor.fim, file, currentFile, previousFile)

      if (bufStatusMatchesFilter(filter, bufStatus)) {
        val lineNum = editor.fimLine
        val lineNumPad =
          if (displayFileName.length < FILE_NAME_PAD) (FILE_NAME_PAD - displayFileName.length).toString() else ""

        bufferList.add(
          String.format(
            "   %${bufNumPad}s %s %s%${lineNumPad}s line: %d", index, bufStatus, displayFileName, "", lineNum
          )
        )
      }
      index++
    }

    return bufferList
  }

  private fun buildVirtualFileDisplayMap(project: Project): Map<VirtualFile, String> {
    val openFiles = FileEditorManager.getInstance(project).openFiles
    val basePath = if (project.basePath != null) project.basePath + "/" else ""
    val filePaths = mutableMapOf<VirtualFile, String>()

    for (file in openFiles) {
      val filePath = file.path

      // If the file is under the project path, then remove the project base path from the file.
      val displayFilePath = if (basePath.isNotEmpty() && filePath.startsWith(basePath)) {
        filePath.replace(basePath, "")
      } else {
        // File is not under the project base path so add the full path.
        filePath
      }

      filePaths[file] = '"' + displayFilePath + '"'
    }

    return filePaths
  }

  private fun bufStatusMatchesFilter(filter: String, bufStatus: String) = filter.all { it in bufStatus }
}

private fun getBufferStatus(
  editor: FimEditor,
  file: VirtualFile,
  currentFile: VirtualFile,
  previousFile: VirtualFile?,
): String {
  @NonNls val bufStatus = StringBuilder()

  when (file) {
    currentFile -> bufStatus.append("%a  ")
    previousFile -> bufStatus.append("#   ")
    else -> bufStatus.append("    ")
  }

  if (!file.isWritable) {
    bufStatus.setCharAt(2, '=')
  }

  if (isDocumentDirty(editor.ij.document)) {
    bufStatus.setCharAt(3, '+')
  }

  return bufStatus.toString()
}

private fun isDocumentDirty(document: Document): Boolean {
  var line = 0

  while (line < document.lineCount) {
    if (document.isLineModified(line)) {
      return true
    }
    line++
  }

  return false
}
