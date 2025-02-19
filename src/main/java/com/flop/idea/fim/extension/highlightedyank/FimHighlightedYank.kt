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

package com.flop.idea.fim.extension.highlightedyank

import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Disposer
import com.flop.idea.fim.FimProjectService
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.helper.MessageHelper
import com.flop.idea.fim.helper.FimNlsSafe
import com.flop.idea.fim.listener.FimInsertListener
import com.flop.idea.fim.listener.FimYankListener
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import org.jetbrains.annotations.NonNls
import java.awt.Color
import java.awt.Font
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

const val DEFAULT_HIGHLIGHT_DURATION: Long = 300

@NonNls
private val HIGHLIGHT_DURATION_VARIABLE_NAME = "highlightedyank_highlight_duration"

@NonNls
private val HIGHLIGHT_COLOR_VARIABLE_NAME = "highlightedyank_highlight_color"
private var defaultHighlightTextColor: Color? = null

private fun getDefaultHighlightTextColor(): Color {
  return defaultHighlightTextColor
    ?: return EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES.defaultAttributes.backgroundColor
      .also { defaultHighlightTextColor = it }
}

class HighlightColorResetter : LafManagerListener {
  override fun lookAndFeelChanged(source: LafManager) {
    defaultHighlightTextColor = null
  }
}

/**
 * @author KostkaBrukowa (@kostkabrukowa)
 *
 * Port of fim-highlightedyank
 * See https://github.com/machakann/fim-highlightedyank
 *
 * if you want to optimize highlight duration, use g:highlightedyank_highlight_duration. Assign a time in milliseconds.
 *
 * let g:highlightedyank_highlight_duration = "1000"
 *
 * A negative number makes the highlight persistent.
 * let g:highlightedyank_highlight_duration = "-1"
 *
 * if you want to change background color of highlight you can provide the rgba of the color you want e.g.
 * let g:highlightedyank_highlight_color = "rgba(160, 160, 160, 155)"
 *
 * When a new text is yanked or user starts editing, the old highlighting would be deleted.
 */
class FimHighlightedYank : com.flop.idea.fim.extension.FimExtension, FimYankListener, FimInsertListener {
  private val highlightHandler = HighlightHandler()

  override fun getName() = "highlightedyank"

  override fun init() {
    com.flop.idea.fim.FimPlugin.getYank().addListener(this)
    com.flop.idea.fim.FimPlugin.getChange().addInsertListener(this)
  }

  override fun dispose() {
    com.flop.idea.fim.FimPlugin.getYank().removeListener(this)
    com.flop.idea.fim.FimPlugin.getChange().removeInsertListener(this)
  }

  override fun yankPerformed(editor: Editor, range: TextRange) {
    highlightHandler.highlightYankRange(editor, range)
  }

  override fun insertModeStarted(editor: Editor) {
    highlightHandler.clearAllYankHighlighters()
  }

  private class HighlightHandler {
    private var editor: Editor? = null
    private val yankHighlighters: MutableSet<RangeHighlighter> = mutableSetOf()

    fun highlightYankRange(editor: Editor, range: TextRange) {
      // from fim-highlightedyank docs: When a new text is yanked or user starts editing, the old highlighting would be deleted
      clearAllYankHighlighters()

      this.editor = editor
      val project = editor.project
      if (project != null) {
        Disposer.register(
          FimProjectService.getInstance(project)
        ) {
          this.editor = null
          yankHighlighters.clear()
        }
      }

      if (range.isMultiple) {
        for (i in 0 until range.size()) {
          highlightSingleRange(editor, range.startOffsets[i]..range.endOffsets[i])
        }
      } else {
        highlightSingleRange(editor, range.startOffset..range.endOffset)
      }
    }

    fun clearAllYankHighlighters() {
      yankHighlighters.forEach { highlighter ->
        editor?.markupModel?.removeHighlighter(highlighter) ?: failIfStrictMode("Highlighters without an editor")
      }

      yankHighlighters.clear()
    }

    private fun failIfStrictMode(value: String) {
      if (injector.optionService.isSet(OptionScope.GLOBAL, OptionConstants.ideastrictmodeName)) {
        error(value)
      }
    }

    private fun highlightSingleRange(editor: Editor, range: ClosedRange<Int>) {
      val highlighter = editor.markupModel.addRangeHighlighter(
        range.start,
        range.endInclusive,
        HighlighterLayer.SELECTION,
        getHighlightTextAttributes(),
        HighlighterTargetArea.EXACT_RANGE
      )

      yankHighlighters.add(highlighter)

      setClearHighlightRangeTimer(highlighter)
    }

    private fun setClearHighlightRangeTimer(highlighter: RangeHighlighter) {
      val timeout = extractUsersHighlightDuration()

      // from fim-highlightedyank docs: A negative number makes the highlight persistent.
      if (timeout >= 0) {
        Executors.newSingleThreadScheduledExecutor().schedule(
          {
            ApplicationManager.getApplication().invokeLater {
              editor?.markupModel?.removeHighlighter(highlighter) ?: failIfStrictMode("Highlighters without an editor")
            }
          },
          timeout, TimeUnit.MILLISECONDS
        )
      }
    }

    private fun getHighlightTextAttributes() = TextAttributes(
      null,
      extractUsersHighlightColor(),
      editor?.colorsScheme?.getColor(EditorColors.CARET_COLOR),
      EffectType.SEARCH_MATCH,
      Font.PLAIN
    )

    private fun extractUsersHighlightDuration(): Long {
      return extractVariable(HIGHLIGHT_DURATION_VARIABLE_NAME, DEFAULT_HIGHLIGHT_DURATION) {
        it.toLong()
      }
    }

    private fun extractUsersHighlightColor(): Color {
      return extractVariable(HIGHLIGHT_COLOR_VARIABLE_NAME, getDefaultHighlightTextColor()) { value ->
        val rgba = value
          .substring(4)
          .filter { it != '(' && it != ')' && !it.isWhitespace() }
          .split(',')
          .map { it.toInt() }

        Color(rgba[0], rgba[1], rgba[2], rgba[3])
      }
    }

    private fun <T> extractVariable(variable: String, default: T, extractFun: (value: String) -> T): T {
      val value = com.flop.idea.fim.FimPlugin.getVariableService().getGlobalVariableValue(variable)

      if (value is FimString) {
        return try {
          extractFun(value.value)
        } catch (e: Exception) {
          @FimNlsSafe val message = MessageHelper.message(
            "highlightedyank.invalid.value.of.0.1",
            "g:$variable",
            e.message ?: ""
          )
          com.flop.idea.fim.FimPlugin.showMessage(message)

          default
        }
      }

      return default
    }
  }
}
