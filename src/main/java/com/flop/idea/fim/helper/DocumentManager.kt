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

package com.flop.idea.fim.helper

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.util.Key
import com.flop.idea.fim.EventFacade
import com.flop.idea.fim.group.MarkGroup
import com.flop.idea.fim.group.SearchGroup

object DocumentManager {
  private val docListeners = mutableSetOf<DocumentListener>()
  private val LISTENER_MARKER = Key<String>("FimlistenerMarker")

  init {
    docListeners += com.flop.idea.fim.group.MarkGroup.MarkUpdater.INSTANCE
    docListeners += com.flop.idea.fim.group.SearchGroup.DocumentSearchListener.INSTANCE
  }

  fun addListeners(doc: Document) {
    val marker = doc.getUserData(LISTENER_MARKER)
    if (marker != null) return

    doc.putUserData(LISTENER_MARKER, "foo")
    for (docListener in docListeners) {
      com.flop.idea.fim.EventFacade.getInstance().addDocumentListener(doc, docListener)
    }
  }

  fun removeListeners(doc: Document) {
    doc.getUserData(LISTENER_MARKER) ?: return

    doc.putUserData(LISTENER_MARKER, null)
    for (docListener in docListeners) {
      com.flop.idea.fim.EventFacade.getInstance().removeDocumentListener(doc, docListener)
    }
  }
}
