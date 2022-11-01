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

package com.flop.idea.fim.newapi

import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor
import com.intellij.codeInsight.editorActions.TextBlockTransferable
import com.intellij.codeInsight.editorActions.TextBlockTransferableData
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.CaretStateTransferableData
import com.intellij.openapi.editor.RawText
import com.intellij.openapi.editor.richcopy.view.HtmlTransferableData
import com.intellij.openapi.editor.richcopy.view.RtfTransferableData
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.psi.PsiDocumentManager
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.FimClipboardManager
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.helper.TestClipboardModel
import com.flop.idea.fim.helper.TestClipboardModel.contents
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import java.awt.HeadlessException
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

@Service
class IjClipboardManager : FimClipboardManager {
  override fun getClipboardTextAndTransferableData(): Pair<String, List<Any>?>? {
    var res: String? = null
    var transferableData: List<TextBlockTransferableData> = ArrayList()
    try {
      val trans = getContents() ?: return null
      val data = trans.getTransferData(DataFlavor.stringFlavor)
      res = data.toString()
      transferableData = collectTransferableData(trans)
    } catch (ignored: HeadlessException) {
    } catch (ignored: UnsupportedFlavorException) {
    } catch (ignored: IOException) {
    }
    if (res == null) return null

    return Pair(res, transferableData)
  }

  @Suppress("UNCHECKED_CAST")
  override fun setClipboardText(text: String, rawText: String, transferableData: List<Any>): Any? {
    val transferableData1 = (transferableData as List<TextBlockTransferableData>).toMutableList()
    try {
      val s = TextBlockTransferable.convertLineSeparators(text, "\n", transferableData1)
      if (transferableData1.none { it is CaretStateTransferableData }) {
        // Manually add CaretStateTransferableData to avoid adjustment of copied text to multicaret
        transferableData1 += CaretStateTransferableData(intArrayOf(0), intArrayOf(s.length))
      }
      logger.debug { "Paste text with transferable data: ${transferableData1.joinToString { it.javaClass.name }}" }
      val content = TextBlockTransferable(s, transferableData1, RawText(rawText))
      setContents(content)
      return content
    } catch (ignored: HeadlessException) {
    }
    return null
  }

  override fun getTransferableData(fimEditor: FimEditor, textRange: TextRange, text: String): List<Any> {
    val editor = (fimEditor as IjFimEditor).editor
    val transferableData: MutableList<TextBlockTransferableData> = ArrayList()
    val project = editor.project ?: return ArrayList()

    val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return ArrayList()
    DumbService.getInstance(project).withAlternativeResolveEnabled {
      for (processor in CopyPastePostProcessor.EP_NAME.extensionList) {
        try {
          transferableData.addAll(
            processor.collectTransferableData(
              file,
              editor,
              textRange.startOffsets,
              textRange.endOffsets
            )
          )
        } catch (ignore: IndexNotReadyException) {
        }
      }
    }
    transferableData.add(CaretStateTransferableData(intArrayOf(0), intArrayOf(text.length)))

    // These data provided by {@link com.intellij.openapi.editor.richcopy.TextWithMarkupProcessor} doesn't work with
    //   IdeaFim and I don't see a way to fix it
    // See https://youtrack.jetbrains.com/issue/VIM-1785
    // See https://youtrack.jetbrains.com/issue/VIM-1731
    transferableData.removeIf { it: TextBlockTransferableData? -> it is RtfTransferableData || it is HtmlTransferableData }
    return transferableData
  }

  @Suppress("UNCHECKED_CAST")
  override fun preprocessText(
    fimEditor: FimEditor,
    textRange: TextRange,
    text: String,
    transferableData: List<*>,
  ): String {
    val editor = (fimEditor as IjFimEditor).editor
    val project = editor.project ?: return text
    val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.document) ?: return text
    val rawText = TextBlockTransferable.convertLineSeparators(
      text, "\n",
      transferableData as Collection<TextBlockTransferableData?>
    )
    if (com.flop.idea.fim.FimPlugin.getOptionService()
      .isSet(OptionScope.GLOBAL, OptionConstants.ideacopypreprocessName, OptionConstants.ideacopypreprocessName)
    ) {
      for (processor in CopyPastePreProcessor.EP_NAME.extensionList) {
        val escapedText = processor.preprocessOnCopy(file, textRange.startOffsets, textRange.endOffsets, rawText)
        if (escapedText != null) {
          return escapedText
        }
      }
    }
    return text
  }

  private fun setContents(contents: Transferable) {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      TestClipboardModel.contents = contents
      CopyPasteManagerEx.getInstanceEx().setContents(contents)
    } else {
      CopyPasteManagerEx.getInstanceEx().setContents(contents)
    }
  }

  private fun collectTransferableData(transferable: Transferable): List<TextBlockTransferableData> {
    val allValues: MutableList<TextBlockTransferableData> = ArrayList()
    for (processor in CopyPastePostProcessor.EP_NAME.extensionList) {
      val data = processor.extractTransferableData(transferable)
      if (data.isNotEmpty()) {
        allValues.addAll(data)
      }
    }
    return allValues
  }

  private fun getContents(): Transferable? {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      return contents
    }
    val manager = CopyPasteManagerEx.getInstanceEx()
    return manager.contents
  }

  companion object {
    val logger = fimLogger<IjClipboardManager>()
  }
}
