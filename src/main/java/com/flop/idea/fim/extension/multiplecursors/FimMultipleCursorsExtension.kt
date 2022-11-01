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

package com.flop.idea.fim.extension.multiplecursors

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.util.NlsSafe
import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.extension.FimExtensionFacade.putExtensionHandlerMapping
import com.flop.idea.fim.extension.FimExtensionFacade.putKeyMappingIfMissing
import com.flop.idea.fim.group.visual.fimSetSelection
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.helper.SearchOptions
import com.flop.idea.fim.helper.endOffsetInclusive
import com.flop.idea.fim.helper.enumSetOf
import com.flop.idea.fim.helper.exitVisualMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.updateCaretsVisualAttributes
import com.flop.idea.fim.helper.userData
import com.flop.idea.fim.newapi.IjFimEditor
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import java.lang.Integer.min

@NlsSafe
private const val NEXT_WHOLE_OCCURRENCE = "<Plug>NextWholeOccurrence"

@NlsSafe
private const val NEXT_OCCURRENCE = "<Plug>NextOccurrence"

@NlsSafe
private const val SKIP_OCCURRENCE = "<Plug>SkipOccurrence"

@NlsSafe
private const val REMOVE_OCCURRENCE = "<Plug>RemoveOccurrence"

@NlsSafe
private const val ALL_WHOLE_OCCURRENCES = "<Plug>AllWholeOccurrences"

@NlsSafe
private const val ALL_OCCURRENCES = "<Plug>AllOccurrences"

private var Editor.fimMultipleCursorsWholeWord: Boolean? by userData()
private var Editor.fimMultipleCursorsLastSelection: TextRange? by userData()

/**
 * Port of fim-multiple-cursors.
 *
 * See https://github.com/terryma/fim-multiple-cursors
 * */
class FimMultipleCursorsExtension : com.flop.idea.fim.extension.FimExtension {

  override fun getName() = "multiple-cursors"

  override fun init() {
    putExtensionHandlerMapping(MappingMode.NXO, injector.parser.parseKeys(NEXT_WHOLE_OCCURRENCE), owner, NextOccurrenceHandler(), false)
    putExtensionHandlerMapping(
      MappingMode.NXO,
      injector.parser.parseKeys(NEXT_OCCURRENCE),
      owner,
      NextOccurrenceHandler(whole = false),
      false
    )
    putExtensionHandlerMapping(MappingMode.NXO, injector.parser.parseKeys(ALL_WHOLE_OCCURRENCES), owner, AllOccurrencesHandler(), false)
    putExtensionHandlerMapping(
      MappingMode.NXO,
      injector.parser.parseKeys(ALL_OCCURRENCES),
      owner,
      AllOccurrencesHandler(whole = false),
      false
    )
    putExtensionHandlerMapping(MappingMode.X, injector.parser.parseKeys(SKIP_OCCURRENCE), owner, SkipOccurrenceHandler(), false)
    putExtensionHandlerMapping(MappingMode.X, injector.parser.parseKeys(REMOVE_OCCURRENCE), owner, RemoveOccurrenceHandler(), false)

    putKeyMappingIfMissing(MappingMode.NXO, injector.parser.parseKeys("<A-n>"), owner, injector.parser.parseKeys(NEXT_WHOLE_OCCURRENCE), true)
    putKeyMappingIfMissing(MappingMode.NXO, injector.parser.parseKeys("g<A-n>"), owner, injector.parser.parseKeys(NEXT_OCCURRENCE), true)
    putKeyMappingIfMissing(MappingMode.X, injector.parser.parseKeys("<A-x>"), owner, injector.parser.parseKeys(SKIP_OCCURRENCE), true)
    putKeyMappingIfMissing(MappingMode.X, injector.parser.parseKeys("<A-p>"), owner, injector.parser.parseKeys(REMOVE_OCCURRENCE), true)
  }

  abstract class WriteActionHandler : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      ApplicationManager.getApplication().runWriteAction {
        executeInWriteAction(editor.ij, context.ij)
      }
    }

    abstract fun executeInWriteAction(editor: Editor, context: DataContext)
  }

  inner class NextOccurrenceHandler(val whole: Boolean = true) : WriteActionHandler() {
    override fun executeInWriteAction(editor: Editor, context: DataContext) {
      val caretModel = editor.caretModel

      // fim-multiple-cursors provides a completely custom implementation of multiple cursors. We can rely on IntelliJ's
      // implementation.
      // fim-multiple-cursors will call "new" to add a new cursor. In normal mode, it sets "whole" to true, in visual,
      // "whole" is false. The "whole" flag is saved to a script wide variable, the cursor is added and then the plugin
      // enters a custom loop, applying appropriate commands. In this loop, there is only a key shortcut for "next"
      // (<C-N>) and no support for "next non-word". The loop will check the script wide word boundary flag and call
      // "new" again.
      // We might want to consider updating the mappings to handle the difference between normal mode and visual mode

      if (!editor.inVisualMode) {
        // TODO: Handle multiple cursors in normal mode
        // E.g. start a multiple cursor session, clear selection and add a new cursor
        // TODO: New cursor should be based on text at the last visual selection marks
        // (Marks are not set until we come out of visual mode, so might need to use a work around)
        // TODO: Make sure we can handle manually added cursors
        if (caretModel.caretCount > 1) return

        val selection = selectWordUnderCaret(editor, caretModel.primaryCaret)

        // The handler is specific to whole/not-whole word, but the next occurrence is based on the initial call
        editor.fimMultipleCursorsWholeWord = whole
        editor.fimMultipleCursorsLastSelection = selection
      } else {
        // fim-multiple-cursors is case sensitive, so it's ok to use a case sensitive set here
        val patterns = sortedSetOf<String>()
        val newPositions = arrayListOf<VisualPosition>()

        // If multiple lines are selected, we want to convert the selection to multiple carets, positioned at the start
        // of each line
        for (caret in caretModel.allCarets) {
          val selectedText = caret.selectedText ?: return

          // Keep a track of the selected text, we'll check it later
          patterns.add(selectedText)

          val lines = selectedText.count { it == '\n' }
          if (lines > 0) {
            val selectionStart = min(caret.selectionStart, caret.selectionEnd)
            val startPosition = editor.offsetToVisualPosition(selectionStart)
            for (line in startPosition.line + 1..startPosition.line + lines) {
              newPositions.add(VisualPosition(line, startPosition.column))
            }
            com.flop.idea.fim.group.MotionGroup.moveCaret(editor, caret, selectionStart)
          }
        }

        if (newPositions.size > 0) {
          editor.exitVisualMode()
          newPositions.forEach { editor.caretModel.addCaret(it, true) ?: return }
          editor.updateCaretsVisualAttributes()
          return
        }

        // All the carets should be selecting the same text. If they're not, then it's likely they have been added
        // by some other means, so we shouldn't continue with the VIM behaviour
        if (patterns.size > 1) return

        // If we are adding the first new cursor, based on the current selection, we do a non-whole word match (ignoring
        // the value passed to the handler during mapping. We should fix the mappings for visual mode). If we're adding
        // a second or subsequent cursor, we should use the boundary matching parameter used to start the session.
        // But all we know right now is that we're in visual mode, and we have a selection. We cannot tell if the
        // selection has been added by the user (we're trying to add the first cursor) or it was added when we added the
        // first/previous cursor (we're about to add a second/subsequent cursor).
        // So, we keep track of the selection used to add the previous cursor. If it matches the current select, we know
        // we're about to add a second cursor (so use the saved word boundary flag). If it does not match, something's
        // changed, so we're adding a first cursor based on the current selection (set a new non-whole word flag)
        val currentSelection = TextRange(caretModel.primaryCaret.selectionStart, caretModel.primaryCaret.selectionEnd)
        var lastSelection = editor.fimMultipleCursorsLastSelection
        val wholeWord = if (lastSelection != null && lastSelection.startOffset == currentSelection.startOffset &&
          lastSelection.endOffset == currentSelection.endOffset
        ) {
          editor.fimMultipleCursorsWholeWord ?: false
        } else {
          false
        }
        editor.fimMultipleCursorsWholeWord = wholeWord
        lastSelection = currentSelection

        // Always work on the text in the last visual selection range, so we work with any changed text, even if it's no
        // longer selected
        val pattern = com.flop.idea.fim.helper.EditorHelper.getText(editor, lastSelection)

        val primaryCaret = editor.caretModel.primaryCaret
        val nextOffset = findNextOccurrence(editor, primaryCaret.offset, pattern, wholeWord)
        if (nextOffset != -1) {
          caretModel.allCarets.forEach {
            if (it.selectionStart == nextOffset) {
              com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("message.no.more.matches"))
              return
            }
          }

          val caret = editor.caretModel.addCaret(editor.offsetToVisualPosition(nextOffset), true) ?: return
          editor.updateCaretsVisualAttributes()
          editor.fimMultipleCursorsLastSelection = selectText(caret, pattern, nextOffset)
        } else {
          com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("message.no.more.matches"))
        }
      }
    }
  }

  inner class AllOccurrencesHandler(val whole: Boolean = true) : WriteActionHandler() {
    override fun executeInWriteAction(editor: Editor, context: DataContext) {
      val caretModel = editor.caretModel
      if (caretModel.caretCount > 1) return

      val primaryCaret = caretModel.primaryCaret
      val text = if (editor.inVisualMode) {
        primaryCaret.selectedText ?: return
      } else {
        val range = com.flop.idea.fim.helper.SearchHelper.findWordUnderCursor(editor, primaryCaret) ?: return
        if (range.startOffset > primaryCaret.offset) return
        com.flop.idea.fim.helper.EditorHelper.getText(editor, range)
      }

      if (!editor.inVisualMode) {
        enterVisualMode(editor.fim)
      }

      // Note that ignoreCase is not overridden by the `\C` in the pattern
      val pattern = makePattern(text, whole)
      val matches = com.flop.idea.fim.helper.SearchHelper.findAll(editor, pattern, 0, -1, false)
      for (match in matches) {
        if (match.contains(primaryCaret.offset)) {
          com.flop.idea.fim.group.MotionGroup.moveCaret(editor, primaryCaret, match.startOffset)
          selectText(primaryCaret, text, match.startOffset)
        } else {
          val caret = editor.caretModel.addCaret(editor.offsetToVisualPosition(match.startOffset), true) ?: return
          selectText(caret, text, match.startOffset)
        }
      }
      editor.updateCaretsVisualAttributes()
    }
  }

  inner class SkipOccurrenceHandler : WriteActionHandler() {
    override fun executeInWriteAction(editor: Editor, context: DataContext) {
      val primaryCaret = editor.caretModel.primaryCaret
      val selectedText = primaryCaret.selectedText ?: return

      val nextOffset =
        findNextOccurrence(editor, primaryCaret.offset, selectedText, editor.fimMultipleCursorsWholeWord ?: false)
      if (nextOffset != -1) {
        editor.caretModel.allCarets.forEach {
          if (it.selectionStart == nextOffset) {
            com.flop.idea.fim.FimPlugin.showMessage(MessageHelper.message("message.no.more.matches"))
            return
          }
        }

        primaryCaret.moveToVisualPosition(editor.offsetToVisualPosition(nextOffset))
        selectText(primaryCaret, selectedText, nextOffset)
      }
    }
  }

  inner class RemoveOccurrenceHandler : WriteActionHandler() {
    override fun executeInWriteAction(editor: Editor, context: DataContext) {
      val caret = editor.caretModel.primaryCaret
      if (caret.selectedText == null) return
      if (!editor.caretModel.removeCaret(caret)) {
        editor.exitVisualMode()
      }
      com.flop.idea.fim.group.MotionGroup.scrollCaretIntoView(caret.editor)
    }
  }

  private fun selectText(caret: Caret, text: String, offset: Int): TextRange? {
    if (text.isEmpty()) return null
    caret.fimSetSelection(offset, offset + text.length - 1, true)
    com.flop.idea.fim.group.MotionGroup.scrollCaretIntoView(caret.editor)
    return TextRange(caret.selectionStart, caret.selectionEnd)
  }

  private fun selectWordUnderCaret(editor: Editor, caret: Caret): TextRange? {
    val range = com.flop.idea.fim.helper.SearchHelper.findWordUnderCursor(editor, caret) ?: return null
    if (range.startOffset > caret.offset) return null

    enterVisualMode(editor.fim)

    // Select the word under the caret, moving the caret to the end of the selection
    caret.fimSetSelection(range.startOffset, range.endOffsetInclusive, true)
    return TextRange(caret.selectionStart, caret.selectionEnd)
  }

  private fun enterVisualMode(editor: FimEditor) {
    // We need to reset the key handler to make sure we pick up the fact that we're in visual mode
    com.flop.idea.fim.FimPlugin.getVisualMotion().enterVisualMode(editor, FimStateMachine.SubMode.VISUAL_CHARACTER)
    KeyHandler.getInstance().reset(editor)
  }

  private fun findNextOccurrence(editor: Editor, startOffset: Int, text: String, whole: Boolean): Int {
    val searchOptions = enumSetOf(SearchOptions.WHOLE_FILE)
    if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.LOCAL(IjFimEditor(editor)), OptionConstants.wrapscanName)) {
      searchOptions.add(SearchOptions.WRAP)
    }

    return com.flop.idea.fim.helper.SearchHelper.findPattern(editor, makePattern(text, whole), startOffset, 1, searchOptions)?.startOffset ?: -1
  }

  private fun makePattern(text: String, whole: Boolean): String {
    // Pattern is "very nomagic" (ignore regex chars) and "force case sensitive". This is fim-multiple-cursors behaviour
    return "\\V\\C" + com.flop.idea.fim.helper.SearchHelper.makeSearchPattern(text, whole)
  }
}
