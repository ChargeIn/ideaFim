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
package com.flop.idea.fim.extension.surround

import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.extension.FimExtension
import com.flop.idea.fim.extension.FimExtensionFacade.executeNormalWithoutMapping
import com.flop.idea.fim.extension.FimExtensionFacade.getRegisterForCaret
import com.flop.idea.fim.extension.FimExtensionFacade.inputKeyStroke
import com.flop.idea.fim.extension.FimExtensionFacade.inputString
import com.flop.idea.fim.extension.FimExtensionFacade.putExtensionHandlerMapping
import com.flop.idea.fim.extension.FimExtensionFacade.putKeyMappingIfMissing
import com.flop.idea.fim.extension.FimExtensionFacade.setOperatorFunction
import com.flop.idea.fim.extension.FimExtensionFacade.setRegisterForCaret
import com.flop.idea.fim.helper.editorMode
import com.flop.idea.fim.key.OperatorFunction
import com.flop.idea.fim.newapi.IjFimCaret
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.helpers.ClipboardOptionHelper
import com.flop.idea.fim.put.PutData
import org.jetbrains.annotations.NonNls
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

/**
 * Port of fim-surround.
 *
 * See https://github.com/tpope/fim-surround
 *
 * @author dhleong
 * @author vlan
 */
class FimSurroundExtension : com.flop.idea.fim.extension.FimExtension {

  override fun getName() = "surround"

  @NonNls
  private val NO_MAPPINGS = "surround_no_mappings"

  override fun init() {
    putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys("<Plug>YSurround"), owner, YSurroundHandler(), false)
    putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys("<Plug>CSurround"), owner, CSurroundHandler(), false)
    putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys("<Plug>DSurround"), owner, DSurroundHandler(), false)
    putExtensionHandlerMapping(MappingMode.XO, injector.parser.parseKeys("<Plug>VSurround"), owner, VSurroundHandler(), false)

    val noMappings = com.flop.idea.fim.FimPlugin.getVariableService().getGlobalVariableValue(NO_MAPPINGS)?.asBoolean() ?: false
    if (!noMappings) {
      putKeyMappingIfMissing(MappingMode.N, injector.parser.parseKeys("ys"), owner, injector.parser.parseKeys("<Plug>YSurround"), true)
      putKeyMappingIfMissing(MappingMode.N, injector.parser.parseKeys("cs"), owner, injector.parser.parseKeys("<Plug>CSurround"), true)
      putKeyMappingIfMissing(MappingMode.N, injector.parser.parseKeys("ds"), owner, injector.parser.parseKeys("<Plug>DSurround"), true)
      putKeyMappingIfMissing(MappingMode.XO, injector.parser.parseKeys("S"), owner, injector.parser.parseKeys("<Plug>VSurround"), true)
    }
  }

  private class YSurroundHandler : ExtensionHandler {
    override val isRepeatable = true

    override fun execute(editor: FimEditor, context: ExecutionContext) {
      setOperatorFunction(Operator())
      executeNormalWithoutMapping(injector.parser.parseKeys("g@"), editor.ij)
    }
  }

  private class VSurroundHandler : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      val selectionStart = editor.ij.caretModel.primaryCaret.selectionStart
      // NB: Operator ignores SelectionType anyway
      if (!Operator().apply(editor, context, SelectionType.CHARACTER_WISE)) {
        return
      }
      runWriteAction {
        // Leave visual mode
        executeNormalWithoutMapping(injector.parser.parseKeys("<Esc>"), editor.ij)
        editor.ij.caretModel.moveToOffset(selectionStart)
      }
    }
  }

  private class CSurroundHandler : ExtensionHandler {
    override val isRepeatable = true

    override fun execute(editor: FimEditor, context: ExecutionContext) {
      val charFrom = getChar(editor.ij)
      if (charFrom.code == 0) return

      val charTo = getChar(editor.ij)
      if (charTo.code == 0) return

      val newSurround = getOrInputPair(charTo, editor.ij) ?: return
      runWriteAction { change(editor, context, charFrom, newSurround) }
    }

    companion object {
      fun change(editor: FimEditor, context: ExecutionContext, charFrom: Char, newSurround: Pair<String, String>?) {
        // Save old register values for carets
        val surroundings = editor.sortedCarets()
          .map {
            val oldValue: List<KeyStroke>? = getRegisterForCaret(REGISTER, it)
            setRegisterForCaret(REGISTER, it, null)
            SurroundingInfo(it, null, oldValue, null)
          }

        // Delete surrounding's content
        perform("di" + pick(charFrom), editor.ij)

        // Add info about surrounding's inner text and location
        surroundings.forEach {
          val registerValue = getRegisterForCaret(REGISTER, it.caret)
          val innerValue = if (registerValue.isNullOrEmpty()) null else registerValue
          it.innerText = innerValue

          val lineEndOffset = injector.engineEditorHelper.getLineEndOffset(editor, it.caret.getLine().line, false)
          if (lineEndOffset == it.caret.offset.point) {
            it.isLineEnd = true
          }
        }

        // Remove surrounding
        perform("da" + pick(charFrom), editor.ij)

        surroundings.forEach {
          if (it.innerText == null && getRegisterForCaret(REGISTER, it.caret)?.isNotEmpty() == true) {
            it.innerText = emptyList()
          }

          // caret should be placed at the first char of inserted text
          // the best solution would be using [ mark after the paste, but marks are not supported by multicaret
          // todo
          if (it.innerText != null) {
            it.offset = it.caret.offset.point
          }
        }

        surroundings
          .filter { it.innerText != null } // we do nothing with carets that are not inside the surrounding
          .map { surrounding ->
            val innerValue = injector.parser.toPrintableString(surrounding.innerText!!)
            val text = newSurround?.let { it.first + innerValue + it.second } ?: innerValue
            val textData = PutData.TextData(text, SelectionType.CHARACTER_WISE, emptyList())
            val putData = PutData(textData, null, 1, insertTextBeforeCaret = !surrounding.isLineEnd, rawIndent = true, caretAfterInsertedText = false)

            surrounding.caret to putData
          }.forEach {
            injector.put.putTextForCaret(editor, it.first, context, it.second)
          }

        surroundings.forEach {
          it.restoreRegister()
        }

        if (surroundings.size == 1) {
          surroundings.first().moveCaret()
        }
      }

      private fun perform(sequence: String, editor: Editor) {
        ClipboardOptionHelper.IdeaputDisabler()
          .use { executeNormalWithoutMapping(injector.parser.parseKeys("\"" + REGISTER + sequence), editor) }
      }

      private fun pick(charFrom: Char) = when (charFrom) {
        'a' -> '>'
        'r' -> ']'
        else -> charFrom
      }
    }
  }

  private data class SurroundingInfo(val caret: FimCaret, var innerText: List<KeyStroke>?, val oldRegisterContent: List<KeyStroke>?, var offset: Int?, var isLineEnd: Boolean = false) {
    fun restoreRegister() {
      setRegisterForCaret(REGISTER, caret, oldRegisterContent)
    }

    fun moveCaret() {
      if (innerText != null && offset != null) {
        caret.moveToOffset(offset!! + if (isLineEnd) 1 else 0)
      }
    }
  }

  private class DSurroundHandler : ExtensionHandler {
    override val isRepeatable = true

    override fun execute(editor: FimEditor, context: ExecutionContext) {
      // Deleting surround is just changing the surrounding to "nothing"
      val charFrom = getChar(editor.ij)
      if (charFrom.code == 0) return

      runWriteAction { CSurroundHandler.change(editor, context, charFrom, null) }
    }
  }

  private class Operator : OperatorFunction {
    override fun apply(fimEditor: FimEditor, context: ExecutionContext, selectionType: SelectionType): Boolean {
      val editor = fimEditor.ij
      val c = getChar(editor)
      if (c.code == 0) return true

      val pair = getOrInputPair(c, editor) ?: return false
      // XXX: Will it work with line-wise or block-wise selections?
      val range = getSurroundRange(editor) ?: return false
      runWriteAction {
        val change = com.flop.idea.fim.FimPlugin.getChange()
        val leftSurround = pair.first
        val primaryCaret = editor.caretModel.primaryCaret
        change.insertText(IjFimEditor(editor), IjFimCaret(primaryCaret), range.startOffset, leftSurround)
        change.insertText(IjFimEditor(editor), IjFimCaret(primaryCaret), range.endOffset + leftSurround.length, pair.second)
        // Jump back to start
        executeNormalWithoutMapping(injector.parser.parseKeys("`["), editor)
      }
      return true
    }

    private fun getSurroundRange(editor: Editor): TextRange? = when (editor.editorMode) {
      FimStateMachine.Mode.COMMAND -> com.flop.idea.fim.FimPlugin.getMark().getChangeMarks(editor.fim)
      FimStateMachine.Mode.VISUAL -> editor.caretModel.primaryCaret.run { TextRange(selectionStart, selectionEnd) }
      else -> null
    }
  }

  companion object {
    private const val REGISTER = '"'

    private val tagNameAndAttributesCapturePattern = "(\\S+)([^>]*)>".toPattern()

    private val SURROUND_PAIRS = mapOf(
      'b' to ("(" to ")"),
      '(' to ("( " to " )"),
      ')' to ("(" to ")"),
      'B' to ("{" to "}"),
      '{' to ("{ " to " }"),
      '}' to ("{" to "}"),
      'r' to ("[" to "]"),
      '[' to ("[ " to " ]"),
      ']' to ("[" to "]"),
      'a' to ("<" to ">"),
      '>' to ("<" to ">"),
      's' to (" " to "")
    )

    private fun getSurroundPair(c: Char): Pair<String, String>? = if (c in SURROUND_PAIRS) {
      SURROUND_PAIRS[c]
    } else if (!c.isLetter()) {
      val s = c.toString()
      s to s
    } else null

    private fun inputTagPair(editor: Editor): Pair<String, String>? {
      val tagInput = inputString(editor, "<", '>')
      val matcher = tagNameAndAttributesCapturePattern.matcher(tagInput)
      return if (matcher.find()) {
        val tagName = matcher.group(1)
        val tagAttributes = matcher.group(2)
        "<$tagName$tagAttributes>" to "</$tagName>"
      } else null
    }

    private fun inputFunctionName(
      editor: Editor,
      withInternalSpaces: Boolean,
    ): Pair<String, String>? {
      val functionNameInput = inputString(editor, "function: ", null)
      if (functionNameInput.isEmpty()) return null
      return if (withInternalSpaces) "$functionNameInput( " to " )" else "$functionNameInput(" to ")"
    }

    private fun getOrInputPair(c: Char, editor: Editor): Pair<String, String>? = when (c) {
      '<', 't' -> inputTagPair(editor)
      'f' -> inputFunctionName(editor, false)
      'F' -> inputFunctionName(editor, true)
      else -> getSurroundPair(c)
    }

    private fun getChar(editor: Editor): Char {
      val key = inputKeyStroke(editor)
      val keyChar = key.keyChar
      return if (keyChar == KeyEvent.CHAR_UNDEFINED || keyChar.code == KeyEvent.VK_ESCAPE) {
        0.toChar()
      } else keyChar
    }
  }
}
