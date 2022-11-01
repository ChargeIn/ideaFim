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

package com.flop.idea.fim.fimscript.model.functions.handlers

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.fimLine
import com.flop.idea.fim.helper.fimSelectionStart
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.FimLContext
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.model.datatypes.asFimInt
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.model.functions.FunctionHandler

// TODO: 03.08.2021 Support second parameter
object LineFunctionHandler : FunctionHandler() {

  override val name = "line"
  override val minimumNumberOfArguments = 1
  override val maximumNumberOfArguments = 2

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: FimEditor,
    context: ExecutionContext,
    fimContext: FimLContext,
  ): FimInt {
    val argument = argumentValues[0].evaluate(editor, context, fimContext)
    return variableToPosition(editor, argument, true)?.first ?: FimInt.ZERO
  }
}

object ColFunctionHandler : FunctionHandler() {

  override val name = "col"
  override val minimumNumberOfArguments = 1
  override val maximumNumberOfArguments = 1

  override fun doFunction(
    argumentValues: List<Expression>,
    editor: FimEditor,
    context: ExecutionContext,
    fimContext: FimLContext,
  ): FimDataType {
    val argument = argumentValues[0].evaluate(editor, context, fimContext)
    return variableToPosition(editor, argument, false)?.second ?: FimInt.ZERO
  }
}

private fun currentCol(editor: FimEditor): FimInt {
  val logicalPosition = editor.currentCaret().getLogicalPosition()
  var lineLength = editor.lineLength(logicalPosition.line)

  // If virtualedit is set, the col is one more
  // XXX Should we also check the current mode?
  if ((com.flop.idea.fim.FimPlugin.getOptionService().getOptionValue(OptionScope.LOCAL(editor), OptionConstants.virtualeditName) as FimString).value.isNotEmpty()) {
    lineLength += 1
  }

  return (logicalPosition.column + 1).coerceAtMost(lineLength).asFimInt()
}

// Analog of var2fpos function
// Translate variable to position
private fun variableToPosition(editor: FimEditor, variable: FimDataType, dollarForLine: Boolean): Pair<FimInt, FimInt>? {
  if (variable is FimList) {
    if (variable.values.size < 2) return null

    val line = indexAsNumber(variable, 0) ?: return null
    if (line <= 0 || line > editor.lineCount()) {
      return null
    }

    var column = indexAsNumber(variable, 1) ?: return null
    val lineLength = editor.lineLength(line.value - 1)

    if (variable[1].asString() == "$") {
      column = (lineLength + 1).asFimInt()
    }

    if (column.value == 0 || column > lineLength + 1) {
      return null
    }

    return line to column
  }

  val name = variable.asString()
  if (name.isEmpty()) return null

  // Current caret line
  if (name[0] == '.') return editor.ij.fimLine.asFimInt() to currentCol(editor)

  // Visual start
  if (name == "v") {
    if (editor.inVisualMode) {
      return editor.ij.fimLine.asFimInt() to currentCol(editor)
    }

    val fimStart = editor.currentCaret().fimSelectionStart
    val visualLine = (editor.offsetToLogicalPosition(fimStart).line + 1).asFimInt()
    val visualCol = (editor.offsetToLogicalPosition(fimStart).column + 1).asFimInt()

    return visualLine to visualCol
  }

  // Mark
  if (name.length >= 2 && name[0] == '\'') {
    val mark = com.flop.idea.fim.FimPlugin.getMark().getMark(editor, name[1]) ?: return null
    val markLogicalLine = (mark.logicalLine + 1).asFimInt()
    val markLogicalCol = (mark.col + 1).asFimInt()
    return markLogicalLine to markLogicalCol
  }

  // First visual line
  if (name.length >= 2 && name[0] == 'w' && name[1] == '0') {
    if (!dollarForLine) return null
    val actualVisualTop = com.flop.idea.fim.helper.EditorHelper.getVisualLineAtTopOfScreen(editor.ij)
    val actualLogicalTop = com.flop.idea.fim.helper.EditorHelper.visualLineToLogicalLine(editor.ij, actualVisualTop)
    return (actualLogicalTop + 1).asFimInt() to currentCol(editor)
  }

  // Last visual line
  if (name.length >= 2 && name[0] == 'w' && name[1] == '$') {
    if (!dollarForLine) return null
    val actualVisualBottom = com.flop.idea.fim.helper.EditorHelper.getVisualLineAtBottomOfScreen(editor.ij)
    val actualLogicalBottom = com.flop.idea.fim.helper.EditorHelper.visualLineToLogicalLine(editor.ij, actualVisualBottom)
    return (actualLogicalBottom + 1).asFimInt() to currentCol(editor)
  }

  // Last column or line
  if (name[0] == '$') {
    return if (dollarForLine) {
      editor.lineCount().asFimInt() to FimInt.ZERO
    } else {
      val line = editor.currentCaret().getLogicalPosition().line
      val lineLength = editor.lineLength(line)
      (line + 1).asFimInt() to lineLength.asFimInt()
    }
  }

  return null
}

// Analog of tv_list_find_nr
// Get value as number by index
private fun indexAsNumber(list: FimList, index: Int): FimInt? {
  val value = list.values.getOrNull(index) ?: return null
  return value.toFimNumber()
}
