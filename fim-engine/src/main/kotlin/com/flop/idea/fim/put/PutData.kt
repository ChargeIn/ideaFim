package com.flop.idea.fim.put

import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.group.visual.FimSelection

/**
 * [putToLine] has affect only of [insertTextBeforeCaret] is false and [visualSelection] is null
 */
data class PutData(
  val textData: TextData?,
  val visualSelection: VisualSelection?,
  val count: Int,
  val insertTextBeforeCaret: Boolean,
  private val rawIndent: Boolean,
  val caretAfterInsertedText: Boolean,
  val putToLine: Int = -1,
) {
  val indent: Boolean =
    if (rawIndent && textData?.typeInRegister != SelectionType.LINE_WISE && visualSelection?.typeInEditor != SelectionType.LINE_WISE) false else rawIndent

  data class VisualSelection(
    val caretsAndSelections: Map<FimCaret, FimSelection>,
    val typeInEditor: SelectionType,
  )

  data class TextData(
    val rawText: String?,
    val typeInRegister: SelectionType,
    val transferableData: List<Any>,
  )
}
