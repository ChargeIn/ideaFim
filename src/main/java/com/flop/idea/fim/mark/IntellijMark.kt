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

package com.flop.idea.fim.mark

import com.intellij.ide.bookmark.BookmarkType
import com.intellij.ide.bookmark.BookmarksManager
import com.intellij.ide.bookmark.LineBookmark
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import java.lang.ref.WeakReference

class IntellijMark(bookmark: LineBookmark, override val col: Int, project: Project?) : Mark {

  private val project: WeakReference<Project?> = WeakReference(project)

  override val key = BookmarksManager.getInstance(project)?.getType(bookmark)?.mnemonic!!
  override val logicalLine: Int
    get() = getMark()?.line ?: 0
  override val filename: String
    get() = getMark()?.file?.path ?: ""
  override val protocol: String?
    get() = getMark()?.file?.let { VirtualFileManager.extractProtocol(it.url) } ?: ""

  override fun isClear(): Boolean = getMark() == null
  override fun clear() {
    val mark = getMark() ?: return
    getProject()?.let { project -> BookmarksManager.getInstance(project)?.remove(mark) }
  }

  private fun getMark(): LineBookmark? =
    getProject()?.let {
      project ->
      BookmarksManager.getInstance(project)?.getBookmark(BookmarkType.get(key)) as? LineBookmark
    }

  private fun getProject(): Project? {
    val proj = project.get() ?: return null
    if (proj.isDisposed) return null
    return proj
  }
}
