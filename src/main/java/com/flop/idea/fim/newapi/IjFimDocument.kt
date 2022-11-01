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

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.flop.idea.fim.api.FimDocument
import com.flop.idea.fim.common.ChangesListener
import com.flop.idea.fim.common.LiveRange
import com.flop.idea.fim.common.Offset

class IjFimDocument(private val document: Document) : FimDocument {

  private val changeListenersMap: MutableMap<ChangesListener, DocumentListener> = mutableMapOf()

  override fun addChangeListener(listener: ChangesListener) {
    val nativeListener = object : DocumentListener {
      override fun documentChanged(event: DocumentEvent) {
        listener.documentChanged(
          ChangesListener.Change(
            event.oldFragment.toString(),
            event.newFragment.toString(),
            event.offset,
          )
        )
      }
    }
    changeListenersMap[listener] = nativeListener
    document.addDocumentListener(nativeListener)
  }

  override fun removeChangeListener(listener: ChangesListener) {
    val nativeListener = changeListenersMap.remove(listener) ?: error("Existing listener expected")
    document.removeDocumentListener(nativeListener)
  }

  override fun getOffsetGuard(offset: Offset): LiveRange? {
    return document.getOffsetGuard(offset.point)?.fim
  }
}
