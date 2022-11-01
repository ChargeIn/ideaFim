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

package com.flop.idea.fim.group

import com.intellij.openapi.util.Key
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimStorageServiceBase
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.newapi.ij

class IjFimStorageService : FimStorageServiceBase() {
  val bufferToKey = mutableMapOf<String, MutableMap<String, Any?>>()

  override fun <T> getDataFromEditor(editor: FimEditor, key: com.flop.idea.fim.api.Key<T>): T? {
    return editor.ij.getUserData(getOrCreateIjKey(key))
  }

  override fun <T> putDataToEditor(editor: FimEditor, key: com.flop.idea.fim.api.Key<T>, data: T) {
    editor.ij.putUserData(getOrCreateIjKey(key), data)
  }

  @Suppress("UNCHECKED_CAST")
  override fun <T> getDataFromBuffer(editor: FimEditor, key: com.flop.idea.fim.api.Key<T>): T? {
    val buffer = com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor.ij)?.path ?: "empty path"
    return bufferToKey[buffer]?.get(key.name) as T?
  }

  override fun <T> putDataToBuffer(editor: FimEditor, key: com.flop.idea.fim.api.Key<T>, data: T) {
    val buffer = com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor.ij)?.path ?: "empty path"
    var bufferStorage = bufferToKey[buffer]
    if (bufferStorage == null) {
      bufferStorage = mutableMapOf()
      bufferToKey[buffer] = bufferStorage
    }
    bufferStorage[key.name] = data
  }

  override fun <T> getDataFromTab(editor: FimEditor, key: com.flop.idea.fim.api.Key<T>): T? {
    throw ExException("Tab scope is not yet supported by IdeaFim :(")
  }

  override fun <T> putDataToTab(editor: FimEditor, key: com.flop.idea.fim.api.Key<T>, data: T) {
    throw ExException("Tab scope is not yet supported by IdeaFim :(")
  }

  private val ijKeys = mutableMapOf<String, Key<out Any?>>()
  @Suppress("UNCHECKED_CAST")
  private fun <T> getOrCreateIjKey(key: com.flop.idea.fim.api.Key<T>): Key<T> {
    val storedIjKey = ijKeys[key.name]
    if (storedIjKey != null) {
      return storedIjKey as Key<T>
    }
    val newKey = Key<T>(key.name)
    ijKeys[key.name] = newKey
    return newKey
  }
}
