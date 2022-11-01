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

package com.flop.idea.fim.extension.paragraphmotion

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.extension.FimExtension
import com.flop.idea.fim.extension.FimExtensionFacade
import com.flop.idea.fim.extension.FimExtensionFacade.putKeyMappingIfMissing
import com.flop.idea.fim.group.MotionGroup
import com.flop.idea.fim.helper.EditorHelper
import com.flop.idea.fim.helper.SearchHelper
import com.flop.idea.fim.helper.fimForEachCaret
import com.flop.idea.fim.newapi.ij

class ParagraphMotion : com.flop.idea.fim.extension.FimExtension {
  override fun getName(): String = "fim-paragraph-motion"

  override fun init() {
    FimExtensionFacade.putExtensionHandlerMapping(MappingMode.NXO, injector.parser.parseKeys("<Plug>(ParagraphNextMotion)"), owner, ParagraphMotionHandler(1), false)
    FimExtensionFacade.putExtensionHandlerMapping(MappingMode.NXO, injector.parser.parseKeys("<Plug>(ParagraphPrevMotion)"), owner, ParagraphMotionHandler(-1), false)

    putKeyMappingIfMissing(MappingMode.NXO, injector.parser.parseKeys("}"), owner, injector.parser.parseKeys("<Plug>(ParagraphNextMotion)"), true)
    putKeyMappingIfMissing(MappingMode.NXO, injector.parser.parseKeys("{"), owner, injector.parser.parseKeys("<Plug>(ParagraphPrevMotion)"), true)
  }

  private class ParagraphMotionHandler(private val count: Int) : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      editor.ij.fimForEachCaret { caret ->
        val motion = moveCaretToNextParagraph(editor.ij, caret, count)
        if (motion >= 0) {
          com.flop.idea.fim.group.MotionGroup.moveCaret(editor.ij, caret, motion)
        }
      }
    }

    fun moveCaretToNextParagraph(editor: Editor, caret: Caret, count: Int): Int {
      var res = com.flop.idea.fim.helper.SearchHelper.findNextParagraph(editor, caret, count, true)
      res = if (res >= 0) {
        com.flop.idea.fim.helper.EditorHelper.normalizeOffset(editor, res, true)
      } else {
        -1
      }
      return res
    }
  }
}
