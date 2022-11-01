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

package com.flop.idea.fim.group.copy

import com.intellij.codeInsight.editorActions.TextBlockTransferable
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.ide.DataManager
import com.intellij.ide.PasteProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.ide.CopyPasteManager
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.command.isBlock
import com.flop.idea.fim.command.isChar
import com.flop.idea.fim.command.isLine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.TestClipboardModel
import com.flop.idea.fim.helper.fileSize
import com.flop.idea.fim.helper.mode
import com.flop.idea.fim.helper.moveToInlayAwareOffset
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.mark.FimMarkConstants.MARK_CHANGE_POS
import com.flop.idea.fim.newapi.IjFimCaret
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.options.helpers.ClipboardOptionHelper
import com.flop.idea.fim.put.ProcessedTextData
import com.flop.idea.fim.put.PutData
import com.flop.idea.fim.put.FimPutBase
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import java.awt.datatransfer.DataFlavor
import kotlin.math.min

class PutGroup : FimPutBase() {
  override fun putTextForCaret(editor: FimEditor, caret: FimCaret, context: ExecutionContext, data: PutData, updateVisualMarks: Boolean): Boolean {
    val additionalData = collectPreModificationData(editor, data)
    data.visualSelection?.let {
      deleteSelectedText(
        editor,
        data,
        OperatorArguments(false, 0, editor.mode, editor.subMode)
      )
    }
    val processedText = processText(editor, data) ?: return false
    putForCaret(editor, caret, data, additionalData, context, processedText)
    if (editor.primaryCaret() == caret && updateVisualMarks) {
      wrapInsertedTextWithVisualMarks(editor, data, processedText)
    }
    return true
  }

  override fun putTextAndSetCaretPosition(
    editor: FimEditor,
    context: ExecutionContext,
    text: ProcessedTextData,
    data: PutData,
    additionalData: Map<String, Any>,
  ) {
    val visualSelection = data.visualSelection
    val subMode = visualSelection?.typeInEditor?.toSubMode() ?: FimStateMachine.SubMode.NONE
    if (OptionConstants.clipboard_ideaput in (
      injector.optionService
        .getOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName) as FimString
      ).value
    ) {
      val idePasteProvider = getProviderForPasteViaIde(editor, text.typeInRegister, data)
      if (idePasteProvider != null) {
        logger.debug("Perform put via idea paste")
        putTextViaIde(idePasteProvider, editor, context, text, subMode, data, additionalData)
        return
      }
    }

    logger.debug("Perform put via plugin")
    val myCarets = if (visualSelection != null) {
      visualSelection.caretsAndSelections.keys.sortedByDescending { it.getLogicalPosition() }
    } else {
      com.flop.idea.fim.helper.EditorHelper.getOrderedCaretsList(editor.ij).map { IjFimCaret(it) }
    }
    injector.application.runWriteAction {
      myCarets.forEach { caret -> putForCaret(editor, caret, data, additionalData, context, text) }
    }
  }

  private fun putForCaret(
    editor: FimEditor,
    caret: FimCaret,
    data: PutData,
    additionalData: Map<String, Any>,
    context: ExecutionContext,
    text: ProcessedTextData,
  ) {
    notifyAboutIdeaPut(editor)
    if (data.visualSelection?.typeInEditor?.isLine == true && editor.isOneLineMode()) return
    val startOffsets = prepareDocumentAndGetStartOffsets(editor, caret, text.typeInRegister, data, additionalData)

    startOffsets.forEach { startOffset ->
      val subMode = data.visualSelection?.typeInEditor?.toSubMode() ?: FimStateMachine.SubMode.NONE
      val endOffset = putTextInternal(
        editor, caret, context, text.text, text.typeInRegister, subMode,
        startOffset, data.count, data.indent, data.caretAfterInsertedText
      )
      if (caret == editor.primaryCaret()) {
        com.flop.idea.fim.FimPlugin.getMark().setChangeMarks(editor, TextRange(startOffset, endOffset))
      }
      moveCaretToEndPosition(
        editor,
        caret,
        startOffset,
        endOffset,
        text.typeInRegister,
        subMode,
        data.caretAfterInsertedText
      )
    }
  }

  private fun prepareDocumentAndGetStartOffsets(
    fimEditor: FimEditor,
    fimCaret: FimCaret,
    typeInRegister: SelectionType,
    data: PutData,
    additionalData: Map<String, Any>,
  ): List<Int> {
    val editor = (fimEditor as IjFimEditor).editor
    val caret = (fimCaret as IjFimCaret).caret
    val application = injector.application
    val visualSelection = data.visualSelection
    if (visualSelection != null) {
      return when {
        visualSelection.typeInEditor.isChar && typeInRegister.isLine -> {
          application.runWriteAction { editor.document.insertString(caret.offset, "\n") }
          listOf(caret.offset + 1)
        }
        visualSelection.typeInEditor.isBlock -> {
          val firstSelectedLine = additionalData["firstSelectedLine"] as Int
          val selectedLines = additionalData["selectedLines"] as Int
          val startColumnOfSelection = additionalData["startColumnOfSelection"] as Int
          val line = if (data.insertTextBeforeCaret) firstSelectedLine else firstSelectedLine + selectedLines
          when (typeInRegister) {
            SelectionType.LINE_WISE -> when {
              data.insertTextBeforeCaret -> listOf(com.flop.idea.fim.helper.EditorHelper.getLineStartOffset(editor, line))
              else -> {
                val pos = com.flop.idea.fim.helper.EditorHelper.getLineEndOffset(editor, line, true)
                application.runWriteAction { editor.document.insertString(pos, "\n") }
                listOf(pos + 1)
              }
            }
            SelectionType.CHARACTER_WISE -> (firstSelectedLine + selectedLines downTo firstSelectedLine)
              .map { editor.logicalPositionToOffset(LogicalPosition(it, startColumnOfSelection)) }
            SelectionType.BLOCK_WISE -> listOf(
              editor.logicalPositionToOffset(
                LogicalPosition(
                  firstSelectedLine,
                  startColumnOfSelection
                )
              )
            )
          }
        }
        visualSelection.typeInEditor.isLine -> {
          val lastChar = if (editor.fileSize > 0) {
            editor.document.getText(com.intellij.openapi.util.TextRange(editor.fileSize - 1, editor.fileSize))[0]
          } else {
            null
          }
          if (caret.offset == editor.fileSize && editor.fileSize != 0 && lastChar != '\n') {
            application.runWriteAction { editor.document.insertString(caret.offset, "\n") }
            listOf(caret.offset + 1)
          } else listOf(caret.offset)
        }
        else -> listOf(caret.offset)
      }
    } else {
      if (data.insertTextBeforeCaret) {
        return when (typeInRegister) {
          SelectionType.LINE_WISE -> listOf(com.flop.idea.fim.FimPlugin.getMotion().moveCaretToLineStart(editor.fim, caret.fim))
          else -> listOf(caret.offset)
        }
      }

      var startOffset: Int
      val line = if (data.putToLine < 0) caret.logicalPosition.line else data.putToLine
      when (typeInRegister) {
        SelectionType.LINE_WISE -> {
          startOffset =
            min(editor.document.textLength, com.flop.idea.fim.FimPlugin.getMotion().moveCaretToLineEnd(editor.fim, line, true) + 1)
          if (startOffset > 0 && startOffset == editor.document.textLength && editor.document.charsSequence[startOffset - 1] != '\n') {
            application.runWriteAction { editor.document.insertString(startOffset, "\n") }
            startOffset++
          }
        }
        else -> {
          startOffset = caret.offset
          if (!com.flop.idea.fim.helper.EditorHelper.isLineEmpty(editor, line, false)) {
            startOffset++
          }
        }
      }

      return if (startOffset > editor.document.textLength) listOf(editor.document.textLength) else listOf(startOffset)
    }
  }

  private fun getProviderForPasteViaIde(
    editor: FimEditor,
    typeInRegister: SelectionType,
    data: PutData,
  ): PasteProvider? {
    val visualSelection = data.visualSelection
    if (visualSelection != null && visualSelection.typeInEditor.isBlock) return null
    if ((typeInRegister.isLine || typeInRegister.isChar) && data.count == 1) {
      val context = DataManager.getInstance().getDataContext(editor.ij.contentComponent)
      val provider = PlatformDataKeys.PASTE_PROVIDER.getData(context)
      if (provider != null && provider.isPasteEnabled(context)) return provider
    }
    return null
  }

  private fun putTextViaIde(
    pasteProvider: PasteProvider,
    fimEditor: FimEditor,
    fimContext: ExecutionContext,
    text: ProcessedTextData,
    subMode: FimStateMachine.SubMode,
    data: PutData,
    additionalData: Map<String, Any>,
  ) {
    val editor = (fimEditor as IjFimEditor).editor
    val context = fimContext.context as DataContext
    val carets: MutableMap<Caret, RangeMarker> = mutableMapOf()
    com.flop.idea.fim.helper.EditorHelper.getOrderedCaretsList(editor).forEach { caret ->
      val startOffset =
        prepareDocumentAndGetStartOffsets(
          fimEditor,
          IjFimCaret(caret),
          text.typeInRegister,
          data,
          additionalData
        ).first()
      val pointMarker = editor.document.createRangeMarker(startOffset, startOffset)
      caret.moveToInlayAwareOffset(startOffset)
      carets[caret] = pointMarker
    }

    val allContentsBefore = CopyPasteManager.getInstance().allContents
    val sizeBeforeInsert = allContentsBefore.size
    val firstItemBefore = allContentsBefore.firstOrNull()
    val origTestContents = TestClipboardModel.contents
    val origContent: TextBlockTransferable = injector.clipboardManager.setClipboardText(
      text.text,
      transferableData = text.transferableData
    ) as TextBlockTransferable
    val allContentsAfter = CopyPasteManager.getInstance().allContents
    val sizeAfterInsert = allContentsAfter.size
    try {
      pasteProvider.performPaste(context)
    } finally {
      val textOnTop =
        ((firstItemBefore as? TextBlockTransferable)?.getTransferData(DataFlavor.stringFlavor) as? String) != text.text
      TestClipboardModel.contents = origTestContents
      if (sizeBeforeInsert != sizeAfterInsert || textOnTop) {
        // Sometimes inserted text replaces existing one. E.g. on insert with + or * register
        (CopyPasteManager.getInstance() as? CopyPasteManagerEx)?.run { removeContent(origContent) }
      }
    }

    carets.forEach { (caret, point) ->
      val startOffset = point.startOffset
      point.dispose()
      if (!caret.isValid) return@forEach
      val endOffset = if (data.indent) doIndent(
        fimEditor,
        IjFimCaret(caret),
        fimContext,
        startOffset,
        startOffset + text.text.length
      ) else startOffset + text.text.length
      com.flop.idea.fim.FimPlugin.getMark().setChangeMarks(editor.fim, TextRange(startOffset, endOffset))
      com.flop.idea.fim.FimPlugin.getMark().setMark(editor.fim, MARK_CHANGE_POS, startOffset)
      moveCaretToEndPosition(
        fimEditor,
        IjFimCaret(caret),
        startOffset,
        endOffset,
        text.typeInRegister,
        subMode,
        data.caretAfterInsertedText
      )
    }
  }

  override fun doIndent(
    editor: FimEditor,
    caret: FimCaret,
    context: ExecutionContext,
    startOffset: Int,
    endOffset: Int,
  ): Int {
    val startLine = editor.offsetToLogicalPosition(startOffset).line
    val endLine = editor.offsetToLogicalPosition(endOffset - 1).line
    val startLineOffset = (editor as IjFimEditor).editor.document.getLineStartOffset(startLine)
    val endLineOffset = editor.editor.document.getLineEndOffset(endLine)

    com.flop.idea.fim.FimPlugin.getChange().autoIndentRange(
      editor,
      caret,
      context,
      TextRange(startLineOffset, endLineOffset)
    )
    return editor.getLineEndOffset(endLine, true)
  }

  override fun notifyAboutIdeaPut(editor: FimEditor?) {
    val project = editor?.ij?.project
    if (com.flop.idea.fim.FimPlugin.getFimState().isIdeaPutNotified ||
      OptionConstants.clipboard_ideaput in (
        com.flop.idea.fim.FimPlugin.getOptionService()
          .getOptionValue(OptionScope.GLOBAL, OptionConstants.clipboardName) as FimString
        ).value ||
      ClipboardOptionHelper.ideaputDisabled
    ) return

    com.flop.idea.fim.FimPlugin.getFimState().isIdeaPutNotified = true

    com.flop.idea.fim.FimPlugin.getNotifications(project).notifyAboutIdeaPut()
  }

  companion object {
    private val logger = Logger.getInstance(PutGroup::class.java.name)
  }
}
