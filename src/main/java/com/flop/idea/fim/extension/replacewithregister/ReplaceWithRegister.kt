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

package com.flop.idea.fim.extension.replacewithregister

import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.command.isLine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.extension.FimExtension
import com.flop.idea.fim.extension.FimExtensionFacade
import com.flop.idea.fim.extension.FimExtensionFacade.executeNormalWithoutMapping
import com.flop.idea.fim.extension.FimExtensionFacade.putKeyMappingIfMissing
import com.flop.idea.fim.extension.FimExtensionFacade.setOperatorFunction
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.helper.EditorDataContext
import com.flop.idea.fim.helper.editorMode
import com.flop.idea.fim.helper.mode
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.key.OperatorFunction
import com.flop.idea.fim.newapi.IjExecutionContext
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.helpers.ClipboardOptionHelper
import com.flop.idea.fim.put.PutData
import org.jetbrains.annotations.NonNls

class ReplaceWithRegister : com.flop.idea.fim.extension.FimExtension {

  override fun getName(): String = "ReplaceWithRegister"

  override fun init() {
    FimExtensionFacade.putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys(RWR_OPERATOR), owner, RwrMotion(), false)
    FimExtensionFacade.putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys(RWR_LINE), owner, RwrLine(), false)
    FimExtensionFacade.putExtensionHandlerMapping(MappingMode.X, injector.parser.parseKeys(RWR_VISUAL), owner, RwrVisual(), false)

    putKeyMappingIfMissing(MappingMode.N, injector.parser.parseKeys("gr"), owner, injector.parser.parseKeys(RWR_OPERATOR), true)
    putKeyMappingIfMissing(MappingMode.N, injector.parser.parseKeys("grr"), owner, injector.parser.parseKeys(RWR_LINE), true)
    putKeyMappingIfMissing(MappingMode.X, injector.parser.parseKeys("gr"), owner, injector.parser.parseKeys(RWR_VISUAL), true)
  }

  private class RwrVisual : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      val typeInEditor = SelectionType.fromSubMode(editor.subMode)
      editor.forEachCaret { caret ->
        val selectionStart = caret.selectionStart
        val selectionEnd = caret.selectionEnd

        val visualSelection = caret to FimSelection.create(selectionStart, selectionEnd - 1, typeInEditor, editor)
        doReplace(editor.ij, caret, PutData.VisualSelection(mapOf(visualSelection), typeInEditor))
      }
      editor.exitVisualModeNative()
    }
  }

  private class RwrMotion : ExtensionHandler {
    override val isRepeatable: Boolean = true

    override fun execute(editor: FimEditor, context: ExecutionContext) {
      setOperatorFunction(Operator())
      executeNormalWithoutMapping(injector.parser.parseKeys("g@"), editor.ij)
    }
  }

  private class RwrLine : ExtensionHandler {
    override val isRepeatable: Boolean = true

    override fun execute(editor: FimEditor, context: ExecutionContext) {
      val caretsAndSelections = mutableMapOf<FimCaret, FimSelection>()
      editor.forEachCaret { caret ->
        val logicalLine = caret.getLogicalPosition().line
        val lineStart = editor.getLineStartOffset(logicalLine)
        val lineEnd = editor.getLineEndOffset(logicalLine, true)

        val visualSelection = caret to FimSelection.create(lineStart, lineEnd, SelectionType.LINE_WISE, editor)
        caretsAndSelections += visualSelection

        doReplace(editor.ij, caret, PutData.VisualSelection(mapOf(visualSelection), SelectionType.LINE_WISE))
      }

      editor.forEachCaret { caret ->
        val fimStart = caretsAndSelections[caret]?.fimStart
        if (fimStart != null) {
          caret.moveToOffset(fimStart)
        }
      }
    }
  }

  private class Operator : OperatorFunction {
    override fun apply(fimEditor: FimEditor, context: ExecutionContext, selectionType: SelectionType): Boolean {
      val editor = (fimEditor as IjFimEditor).editor
      val range = getRange(editor) ?: return false
      val visualSelection = PutData.VisualSelection(
        mapOf(
          fimEditor.primaryCaret() to FimSelection.create(
            range.startOffset,
            range.endOffset - 1,
            selectionType,
            fimEditor
          )
        ),
        selectionType
      )
      // todo multicaret
      doReplace(editor, fimEditor.primaryCaret(), visualSelection)
      return true
    }

    private fun getRange(editor: Editor): TextRange? = when (editor.fim.mode) {
      FimStateMachine.Mode.COMMAND -> com.flop.idea.fim.FimPlugin.getMark().getChangeMarks(editor.fim)
      FimStateMachine.Mode.VISUAL -> editor.caretModel.primaryCaret.run { TextRange(selectionStart, selectionEnd) }
      else -> null
    }
  }

  companion object {
    @NonNls
    private const val RWR_OPERATOR = "<Plug>ReplaceWithRegisterOperator"

    @NonNls
    private const val RWR_LINE = "<Plug>ReplaceWithRegisterLine"

    @NonNls
    private const val RWR_VISUAL = "<Plug>ReplaceWithRegisterVisual"

    private fun doReplace(editor: Editor, caret: FimCaret, visualSelection: PutData.VisualSelection) {
      val lastRegisterChar = injector.registerGroup.lastRegisterChar
      val savedRegister = caret.registerStorage.getRegister(caret, lastRegisterChar) ?: return

      var usedType = savedRegister.type
      var usedText = savedRegister.text
      if (usedType.isLine && usedText?.endsWith('\n') == true) {
        // Code from original plugin implementation. Correct text for linewise selected text
        usedText = usedText.dropLast(1)
        usedType = SelectionType.CHARACTER_WISE
      }

      val textData = PutData.TextData(usedText, usedType, savedRegister.transferableData)

      val putData = PutData(
        textData,
        visualSelection,
        1,
        insertTextBeforeCaret = true,
        rawIndent = true,
        caretAfterInsertedText = false,
        putToLine = -1
      )
      ClipboardOptionHelper.IdeaputDisabler().use {
        com.flop.idea.fim.FimPlugin.getPut().putText(
          IjFimEditor(editor),
          IjExecutionContext(EditorDataContext.init(editor)),
          putData,
          operatorArguments = OperatorArguments(
            editor.fimStateMachine?.isOperatorPending ?: false,
            0, editor.editorMode, editor.subMode
          )
        )
      }

      caret.registerStorage.saveRegister(caret, savedRegister.name, savedRegister)
      caret.registerStorage.saveRegister(caret, com.flop.idea.fim.FimPlugin.getRegister().defaultRegister, savedRegister)
    }
  }
}
