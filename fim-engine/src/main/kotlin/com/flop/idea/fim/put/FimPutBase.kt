package com.flop.idea.fim.put

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimLogicalPosition
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.command.isBlock
import com.flop.idea.fim.command.isChar
import com.flop.idea.fim.command.isLine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.firstOrNull
import com.flop.idea.fim.helper.mode
import com.flop.idea.fim.mark.FimMarkConstants.MARK_CHANGE_POS
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class FimPutBase : FimPut {
  override fun putText(
    editor: FimEditor,
    context: ExecutionContext,
    data: PutData,
    operatorArguments: OperatorArguments,
    updateVisualMarks: Boolean,
  ): Boolean {
    val additionalData = collectPreModificationData(editor, data)
    deleteSelectedText(editor, data, operatorArguments)
    val processedText = processText(editor, data) ?: return false
    putTextAndSetCaretPosition(editor, context, processedText, data, additionalData)

    if (updateVisualMarks) {
      wrapInsertedTextWithVisualMarks(editor, data, processedText)
    }

    return true
  }

  protected fun collectPreModificationData(editor: FimEditor, data: PutData): Map<String, Any> {
    return if (data.visualSelection != null && data.visualSelection.typeInEditor.isBlock) {
      val fimSelection = data.visualSelection.caretsAndSelections.getValue(editor.primaryCaret())
      val selStart = editor.offsetToLogicalPosition(fimSelection.fimStart)
      val selEnd = editor.offsetToLogicalPosition(fimSelection.fimEnd)
      mapOf(
        "startColumnOfSelection" to min(selStart.column, selEnd.column),
        "selectedLines" to abs(selStart.line - selEnd.line),
        "firstSelectedLine" to min(selStart.line, selEnd.line)
      )
    } else mutableMapOf()
  }

  protected fun wasTextInsertedLineWise(text: ProcessedTextData): Boolean {
    return text.typeInRegister == SelectionType.LINE_WISE
  }

  /**
   * see ":h gv":
   * After using "p" or "P" in Visual mode the text that was put will be selected
   */
  protected fun wrapInsertedTextWithVisualMarks(editor: FimEditor, data: PutData, text: ProcessedTextData) {
    val textLength: Int = data.textData?.rawText?.length ?: return
    val currentCaret = editor.currentCaret()
    val caretsAndSelections = data.visualSelection?.caretsAndSelections ?: return
    val selection = caretsAndSelections[currentCaret] ?: caretsAndSelections.firstOrNull()?.value ?: return

    val leftIndex = min(selection.fimStart, selection.fimEnd)
    val rightIndex = leftIndex + textLength - 1

    val rangeForMarks = if (wasTextInsertedLineWise(text)) {
      // here we skip the \n char after the inserted text
      TextRange(leftIndex, rightIndex - 1)
    } else {
      TextRange(leftIndex, rightIndex)
    }

    editor.fimLastSelectionType = SelectionType.CHARACTER_WISE
    injector.markGroup.setVisualSelectionMarks(editor, rangeForMarks)
  }

  protected fun deleteSelectedText(editor: FimEditor, data: PutData, operatorArguments: OperatorArguments) {
    if (data.visualSelection == null) return

    data.visualSelection.caretsAndSelections.entries.sortedByDescending { it.key.getLogicalPosition() }
      .forEach { (caret, selection) ->
        if (!caret.isValid) return@forEach
        val range = selection.toFimTextRange(false).normalize()

        injector.application.runWriteAction {
          injector.changeGroup.deleteRange(editor, caret, range, selection.type, false, operatorArguments)
        }
        caret.moveToInlayAwareOffset(range.startOffset)
      }
  }

  protected fun processText(editor: FimEditor, data: PutData): ProcessedTextData? {
    var text = data.textData?.rawText ?: run {
      if (data.visualSelection != null) {
        val offset = editor.primaryCaret().offset.point
        injector.markGroup.setMark(editor, MARK_CHANGE_POS, offset)
        injector.markGroup.setChangeMarks(editor, TextRange(offset, offset + 1))
      }
      return null
    }

    if (data.visualSelection?.typeInEditor?.isLine == true && data.textData.typeInRegister.isChar) text += "\n"

    if (data.textData.typeInRegister.isLine && text.isNotEmpty() && text.last() != '\n') text += '\n'

    if (data.textData.typeInRegister.isChar && text.lastOrNull() == '\n' && data.visualSelection?.typeInEditor?.isLine == false) text =
      text.dropLast(1)

    return ProcessedTextData(text, data.textData.typeInRegister, data.textData.transferableData)
  }

  protected fun moveCaretToEndPosition(
    editor: FimEditor,
    caret: FimCaret,
    startOffset: Int,
    endOffset: Int,
    typeInRegister: SelectionType,
    modeInEditor: FimStateMachine.SubMode,
    caretAfterInsertedText: Boolean,
  ) {
    val cursorMode = when (typeInRegister) {
      SelectionType.BLOCK_WISE -> when (modeInEditor) {
        FimStateMachine.SubMode.VISUAL_LINE -> if (caretAfterInsertedText) "postEndOffset" else "startOffset"
        else -> if (caretAfterInsertedText) "preLineEndOfEndOffset" else "startOffset"
      }
      SelectionType.LINE_WISE -> if (caretAfterInsertedText) "postEndOffset" else "startOffsetSkipLeading"
      SelectionType.CHARACTER_WISE -> when (modeInEditor) {
        FimStateMachine.SubMode.VISUAL_LINE -> if (caretAfterInsertedText) "postEndOffset" else "startOffset"
        else -> if (caretAfterInsertedText) "preLineEndOfEndOffset" else "preEndOffset"
      }
    }

    when (cursorMode) {
      "startOffset" -> caret.moveToOffset(startOffset)
      "preEndOffset" -> caret.moveToOffset(endOffset - 1)
      "startOffsetSkipLeading" -> {
        caret.moveToOffset(startOffset)
        caret.moveToOffset(injector.motion.moveCaretToLineStartSkipLeading(editor, caret))
      }
      "postEndOffset" -> caret.moveToOffset(endOffset + 1)
      "preLineEndOfEndOffset" -> {
        var rightestPosition = editor.getLineEndForOffset(endOffset - 1)
        if (editor.mode != FimStateMachine.Mode.INSERT) --rightestPosition // it's not possible to place a caret at the end of the line in any mode except insert
        val pos = min(endOffset, rightestPosition)
        caret.moveToOffset(pos)
      }
    }
  }

  override fun doIndent(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    startOffset: Int,
    endOffset: Int,
  ): Int {
    TODO("Not yet implemented")
  }

  protected fun putTextCharacterwise(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    text: String,
    type: SelectionType,
    mode: FimStateMachine.SubMode,
    startOffset: Int,
    count: Int,
    indent: Boolean,
    cursorAfter: Boolean,
  ): Int {
    caret.moveToOffset(startOffset)
    val insertedText = text.repeat(count)
    injector.changeGroup.insertText(editor, caret, insertedText)

    val endOffset = if (indent)
      doIndent(editor, caret, context, startOffset, startOffset + insertedText.length)
    else
      startOffset + insertedText.length
    moveCaretToEndPosition(editor, caret, startOffset, endOffset, type, mode, cursorAfter)

    return endOffset
  }

  protected fun putTextLinewise(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    text: String,
    type: SelectionType,
    mode: FimStateMachine.SubMode,
    startOffset: Int,
    count: Int,
    indent: Boolean,
    cursorAfter: Boolean,
  ): Int {
    val overlappedCarets = ArrayList<FimCaret>(editor.carets().size)
    for (possiblyOverlappedCaret in editor.carets()) {
      if (possiblyOverlappedCaret.offset.point != startOffset || possiblyOverlappedCaret == caret) continue

      possiblyOverlappedCaret.moveToOffset(
        injector.motion.getOffsetOfHorizontalMotion(editor, possiblyOverlappedCaret, 1, true)
      )
      overlappedCarets.add(possiblyOverlappedCaret)
    }

    val endOffset = putTextCharacterwise(
      editor, caret, context, text, type, mode, startOffset, count, indent,
      cursorAfter
    )

    for (overlappedCaret in overlappedCarets) {
      overlappedCaret.moveToOffset(
        injector.motion.getOffsetOfHorizontalMotion(editor, overlappedCaret, -1, true)
      )
    }

    return endOffset
  }

  protected fun getMaxSegmentLength(text: String): Int {
    val tokenizer = StringTokenizer(text, "\n")
    var maxLen = 0
    while (tokenizer.hasMoreTokens()) {
      val s = tokenizer.nextToken()
      maxLen = max(s.length, maxLen)
    }
    return maxLen
  }

  protected fun putTextBlockwise(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    text: String,
    type: SelectionType,
    mode: FimStateMachine.SubMode,
    startOffset: Int,
    count: Int,
    indent: Boolean,
    cursorAfter: Boolean,
  ): Int {
    val startPosition = editor.offsetToLogicalPosition(startOffset)
    val currentColumn = if (mode == FimStateMachine.SubMode.VISUAL_LINE) 0 else startPosition.column
    var currentLine = startPosition.line

    val lineCount = injector.engineEditorHelper.getLineBreakCount(text) + 1
    if (currentLine + lineCount >= editor.nativeLineCount()) {
      val limit = currentLine + lineCount - editor.nativeLineCount()
      for (i in 0 until limit) {
        caret.moveToOffset(editor.fileSize().toInt())
        injector.changeGroup.insertText(editor, caret, "\n")
      }
    }

    val maxLen = getMaxSegmentLength(text)
    val tokenizer = StringTokenizer(text, "\n")
    var endOffset = startOffset
    while (tokenizer.hasMoreTokens()) {
      var segment = tokenizer.nextToken()
      var origSegment = segment

      if (segment.length < maxLen) {
        segment += " ".repeat(maxLen - segment.length)

        if (currentColumn != 0 && currentColumn < injector.engineEditorHelper.getLineLength(editor, currentLine)) {
          origSegment = segment
        }
      }

      val pad = injector.engineEditorHelper.pad(editor, context, currentLine, currentColumn)

      val insertOffset = editor.logicalPositionToOffset(FimLogicalPosition(currentLine, currentColumn))
      caret.moveToOffset(insertOffset)
      val insertedText = origSegment + segment.repeat(count - 1)
      injector.changeGroup.insertText(editor, caret, insertedText)
      endOffset += insertedText.length

      if (mode == FimStateMachine.SubMode.VISUAL_LINE) {
        caret.moveToOffset(endOffset)
        injector.changeGroup.insertText(editor, caret, "\n")
        ++endOffset
      } else {
        if (pad.isNotEmpty()) {
          caret.moveToOffset(insertOffset)
          injector.changeGroup.insertText(editor, caret, pad)
          endOffset += pad.length
        }
      }

      ++currentLine
    }

    if (indent) endOffset = doIndent(editor, caret, context, startOffset, endOffset)
    moveCaretToEndPosition(editor, caret, startOffset, endOffset, type, mode, cursorAfter)

    return endOffset
  }

  protected fun putTextInternal(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    text: String,
    type: SelectionType,
    mode: FimStateMachine.SubMode,
    startOffset: Int,
    count: Int,
    indent: Boolean,
    cursorAfter: Boolean,
  ): Int {
    return when (type) {
      SelectionType.CHARACTER_WISE -> putTextCharacterwise(
        editor,
        caret,
        context,
        text,
        type,
        mode,
        startOffset,
        count,
        indent,
        cursorAfter
      )
      SelectionType.LINE_WISE -> putTextLinewise(
        editor,
        caret,
        context,
        text,
        type,
        mode,
        startOffset,
        count,
        indent,
        cursorAfter
      )
      else -> putTextBlockwise(editor, caret, context, text, type, mode, startOffset, count, indent, cursorAfter)
    }
  }
}
