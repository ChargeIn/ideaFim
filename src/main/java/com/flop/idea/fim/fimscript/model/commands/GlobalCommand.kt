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

import com.intellij.openapi.editor.RangeMarker
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.LineRange
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.group.SearchGroup.RE_BOTH
import com.flop.idea.fim.group.SearchGroup.RE_LAST
import com.flop.idea.fim.group.SearchGroup.RE_SEARCH
import com.flop.idea.fim.group.SearchGroup.RE_SUBST
import com.flop.idea.fim.helper.MessageHelper.message
import com.flop.idea.fim.helper.Msg
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.regexp.CharPointer
import com.flop.idea.fim.regexp.RegExp
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * see "h :global" / "h :vglobal"
 */
data class GlobalCommand(val ranges: Ranges, val argument: String, val invert: Boolean) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.SELF_SYNCHRONIZED)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    var result: ExecutionResult = ExecutionResult.Success
    editor.removeSecondaryCarets()
    val caret = editor.currentCaret()

    // For :g command the default range is %
    val lineRange: LineRange = if (ranges.size() == 0) {
      LineRange(0, editor.lineCount() - 1)
    } else {
      getLineRange(editor, caret)
    }
    if (!processGlobalCommand(editor, context, lineRange)) {
      result = ExecutionResult.Error
    }
    return result
  }

  private fun processGlobalCommand(
    editor: FimEditor,
    context: ExecutionContext,
    range: LineRange,
  ): Boolean {
    // When nesting the command works on one line.  This allows for
    // ":g/found/v/notfound/command".
    if (globalBusy && (range.startLine != 0 || range.endLine != editor.lineCount() - 1)) {
      com.flop.idea.fim.FimPlugin.showMessage(message("E147"))
      com.flop.idea.fim.FimPlugin.indicateError()
      return false
    }
    var cmd = CharPointer(StringBuffer(argument))

    val pat: CharPointer
    val delimiter: Char
    var whichPat = RE_LAST

    /*
     * undocumented vi feature:
     * "\/" and "\?": use previous search pattern.
     *   "\&": use previous substitute pattern.
     */
    if (argument.isEmpty()) {
      com.flop.idea.fim.FimPlugin.showMessage(message("E148"))
      com.flop.idea.fim.FimPlugin.indicateError()
      return false
    } else if (cmd.charAt() == '\\') {
      cmd.inc()
      if ("/?&".indexOf(cmd.charAt()) == -1) {
        com.flop.idea.fim.FimPlugin.showMessage(message(Msg.e_backslash))
        return false
      }
      whichPat = if (cmd.charAt() == '&') RE_SUBST else RE_SEARCH
      cmd.inc()
      pat = CharPointer("") /* empty search pattern */
    } else {
      delimiter = cmd.charAt() /* get the delimiter */
      cmd.inc()
      pat = cmd.ref(0) /* remember start of pattern */
      cmd = RegExp.skip_regexp(cmd, delimiter, true)
      if (cmd.charAt() == delimiter) { /* end delimiter found */
        cmd.set('\u0000').inc() /* replace it with a NUL */
      }
    }

    val (first, second) = injector.searchGroup.search_regcomp(pat, whichPat, RE_BOTH)
    if (!first) {
      com.flop.idea.fim.FimPlugin.showMessage(message(Msg.e_invcmd))
      com.flop.idea.fim.FimPlugin.indicateError()
      return false
    }
    val regmatch = second.first as RegExp.regmmatch_T
    val sp = second.third as RegExp

    var match: Int
    val lcount = editor.lineCount()
    val searchcol = 0
    if (globalBusy) {
      val offset = editor.currentCaret().offset
      val lineStartOffset = editor.lineStartForOffset(offset.point)
      match = sp.fim_regexec_multi(regmatch, editor, lcount, lineStartOffset, searchcol)
      if ((!invert && match > 0) || (invert && match <= 0)) {
        globalExecuteOne(editor, context, lineStartOffset, cmd.toString())
      }
    } else {
      // pass 1: set marks for each (not) matching line
      val line1 = range.startLine
      val line2 = range.endLine
      //region search_regcomp implementation
      // We don't need to worry about lastIgnoreSmartCase, it's always false. Fim resets after checking, and it only sets
      // it to true when searching for a word with `*`, `#`, `g*`, etc.

      if (line1 < 0 || line2 < 0) {
        return false
      }

      var ndone = 0
      val marks = mutableListOf<RangeMarker>()
      for (lnum in line1..line2) {
        if (gotInt) break

        // a match on this line?
        match = sp.fim_regexec_multi(regmatch, editor, lcount, lnum, searchcol)
        if ((!invert && match > 0) || (invert && match <= 0)) {
          val lineStartOffset = editor.getLineStartOffset(lnum)
          marks += editor.ij.document.createRangeMarker(lineStartOffset, lineStartOffset)
          ndone += 1
        }
        // TODO: 25.05.2021 Check break
      }

      // pass 2: execute the command for each line that has been marked
      if (gotInt) {
        com.flop.idea.fim.FimPlugin.showMessage(message("e_interr"))
      } else if (ndone == 0) {
        if (invert) {
          com.flop.idea.fim.FimPlugin.showMessage(message("global.command.not.found.v", pat.toString()))
        } else {
          com.flop.idea.fim.FimPlugin.showMessage(message("global.command.not.found.g", pat.toString()))
        }
      } else {
        globalExe(editor, context, marks, cmd.toString())
      }
    }
    return true
  }

  private fun globalExe(editor: FimEditor, context: ExecutionContext, marks: List<RangeMarker>, cmd: String) {
    globalBusy = true
    try {
      for (mark in marks) {
        if (gotInt) break
        if (!globalBusy) break
        val startOffset = mark.startOffset
        mark.dispose()
        globalExecuteOne(editor, context, startOffset, cmd)
        // TODO: 26.05.2021 break check
      }
    } catch (e: Exception) {
      throw e
    } finally {
      globalBusy = false
    }
    // TODO: 26.05.2021 Add other staff
  }

  private fun globalExecuteOne(editor: FimEditor, context: ExecutionContext, lineStartOffset: Int, cmd: String?) {
    // TODO: 26.05.2021 What about folds?
    editor.currentCaret().moveToOffset(lineStartOffset)
    if (cmd == null || cmd.isEmpty() || (cmd.length == 1 && cmd[0] == '\n')) {
      injector.fimscriptExecutor.execute("p", editor, context, skipHistory = true, indicateErrors = true, this.fimContext)
    } else {
      injector.fimscriptExecutor.execute(cmd, editor, context, skipHistory = true, indicateErrors = true, this.fimContext)
    }
  }

  companion object {
    private var globalBusy = false

    // Interrupted. Not used at the moment
    var gotInt: Boolean = false
  }
}
