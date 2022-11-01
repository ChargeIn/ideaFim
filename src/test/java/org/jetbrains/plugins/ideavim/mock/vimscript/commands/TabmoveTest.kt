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

package org.jetbrains.plugins.ideafim.mock.fimscript.commands

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.group.TabService
import org.jetbrains.plugins.ideafim.mock.MockTestCase
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class TabmoveTest : MockTestCase() {

  fun `test move to the first index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove 0", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(0, contextStub)
  }

  fun `test move to the last index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove $", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(4, contextStub)
  }

  fun `test move to index that is greater than current`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(4)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove 2", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(2, contextStub)
  }

  fun `test move to index that is less than current`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(1)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove 3", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(2, contextStub)
  }

  fun `test move to nonexistent index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove 7", editorStub, contextStub, skipHistory = false)

    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument")
    verify(tabService, never()).moveCurrentTabToIndex(any(), any())
  }

  fun `test move to positive relative index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove +2", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(4, contextStub)
  }

  fun `test move to negative relative index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(4)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove -2", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(2, contextStub)
  }

  fun `test move to nonexistent positive relative index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove +10", editorStub, contextStub, skipHistory = false)

    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument")
    verify(tabService, never()).moveCurrentTabToIndex(any(), any())
  }

  fun `test move to nonexistent negative relative index`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove -10", editorStub, contextStub, skipHistory = false)

    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument")
    verify(tabService, never()).moveCurrentTabToIndex(any(), any())
  }

  fun `test move to plus zero`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove +0", editorStub, contextStub, skipHistory = false)

    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument")
    verify(tabService, never()).moveCurrentTabToIndex(any(), any())
  }

  fun `test move to minus zero`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove +0", editorStub, contextStub, skipHistory = false)

    assertPluginError(true)
    assertPluginErrorMessageContains("E474: Invalid argument")
    verify(tabService, never()).moveCurrentTabToIndex(any(), any())
  }

  fun `test move left with omitted number`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove +", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(3, contextStub)
  }

  fun `test move right with omitted number`() {
    val tabService = mockService(TabService::class.java)
    Mockito.`when`(tabService.getCurrentTabIndex(contextStub)).thenReturn(2)
    Mockito.`when`(tabService.getTabCount(contextStub)).thenReturn(5)
    injector.fimscriptExecutor.execute("tabmove -", editorStub, contextStub, skipHistory = false)

    verify(tabService).moveCurrentTabToIndex(1, contextStub)
  }
}
