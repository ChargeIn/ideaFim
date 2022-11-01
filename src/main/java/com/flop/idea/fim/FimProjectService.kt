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

package com.flop.idea.fim

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.flop.idea.fim.group.EditorHolderService

@Service
class FimProjectService(val project: Project) : Disposable {
  override fun dispose() {

    // Not sure if this is a best solution
    EditorHolderService.getInstance().editor = null
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project): FimProjectService = project.service()
  }
}

@Suppress("unused")
val Project.fimDisposable
  get() = FimProjectService.getInstance(this)
