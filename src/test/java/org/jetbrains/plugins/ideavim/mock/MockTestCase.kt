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

package org.jetbrains.plugins.ideafim.mock

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.textarea.TextComponentEditorImpl
import com.intellij.testFramework.replaceService
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.newapi.fim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.mockito.Mockito
import javax.swing.JTextArea

open class MockTestCase : FimTestCase() {

  val editorStub = TextComponentEditorImpl(null, JTextArea()).fim
  val contextStub: ExecutionContext = DataContext.EMPTY_CONTEXT.fim

  fun <T : Any> mockService(service: Class<T>): T {
    val mock = Mockito.mock(service)
    val applicationManager = ApplicationManager.getApplication()
    applicationManager.replaceService(service, mock, this.testRootDisposable)
    return mock
  }
}
