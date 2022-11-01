package com.flop.idea.fim.put

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.OperatorArguments

interface FimPut {
  fun doIndent(editor: FimEditor, caret: FimCaret, context: ExecutionContext, startOffset: Int, endOffset: Int): Int

  fun notifyAboutIdeaPut(editor: FimEditor?)
  fun putTextAndSetCaretPosition(
    editor: FimEditor,
    context: ExecutionContext,
    text: ProcessedTextData,
    data: PutData,
    additionalData: Map<String, Any>,
  )

  fun putText(
    editor: FimEditor,
    context: ExecutionContext,
    data: PutData,
    operatorArguments: OperatorArguments,
    updateVisualMarks: Boolean = false,
  ): Boolean

  fun putTextForCaret(editor: FimEditor, caret: FimCaret, context: ExecutionContext, data: PutData, updateVisualMarks: Boolean = false): Boolean
}
