package com.flop.idea.fim.fimscript.model.commands

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.helper.Msg
import com.flop.idea.fim.mark.FimMarkConstants
import com.flop.idea.fim.fimscript.model.ExecutionResult

/**
 * @author Jørgen Granseth
 * see "h :delmarks"
 */

private val VIML_COMMENT = Regex("(?<!\\\\)\".*")
private val TRAILING_SPACES = Regex("\\s*$")
private val ARGUMENT_DELETE_ALL_FILE_MARKS = Regex("^!$")

private const val ESCAPED_QUOTE = "\\\""
private const val UNESCAPED_QUOTE = "\""

data class DeleteMarksCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_REQUIRED, Access.READ_ONLY)

  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    val processedArg = argument
      .replace(VIML_COMMENT, "")
      .replace(ESCAPED_QUOTE, UNESCAPED_QUOTE)
      .replace(TRAILING_SPACES, "")
      .replace(ARGUMENT_DELETE_ALL_FILE_MARKS, FimMarkConstants.DEL_FILE_MARKS)
      .replaceRanges(FimMarkConstants.WR_REGULAR_FILE_MARKS)
      .replaceRanges(FimMarkConstants.WR_GLOBAL_MARKS)
      .replaceRanges(FimMarkConstants.RO_GLOBAL_MARKS)

    processedArg.indexOfFirst { it !in " ${FimMarkConstants.DEL_MARKS}" }.let { index ->
      if (index != -1) {
        val invalidIndex = if (processedArg[index] == '-') (index - 1).coerceAtLeast(0) else index

        injector.messages.showStatusBarMessage(injector.messages.message(Msg.E475, processedArg.substring(invalidIndex)))
        return ExecutionResult.Error
      }
    }

    processedArg.forEach { character -> deleteMark(editor, character) }

    return ExecutionResult.Success
  }
}

private fun deleteMark(editor: FimEditor, character: Char) {
  if (character != ' ') {
    val markGroup = injector.markGroup
    val mark = markGroup.getMark(editor, character) ?: return
    markGroup.removeMark(character, mark)
  }
}

private fun String.replaceRanges(range: String): String {
  return Regex("[$range]-[$range]").replace(this) { match ->
    val startChar = match.value[0]
    val endChar = match.value[2]

    val startIndex = range.indexOf(startChar)
    val endIndex = range.indexOf(endChar)

    if (startIndex >= 0 && endIndex >= 0 && startIndex <= endIndex) {
      range.subSequence(startIndex, endIndex + 1)
    } else {
      match.value
    }
  }
}
