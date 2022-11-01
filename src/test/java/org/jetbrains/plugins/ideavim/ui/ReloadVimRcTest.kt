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

package org.jetbrains.plugins.ideafim.ui

import com.intellij.mock.MockEditorFactory
import com.flop.idea.fim.ui.FimRcFileState
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class ReloadFimRcTest : FimTestCase() {
  private val editorFactory = MockEditorFactory()

  override fun setUp() {
    super.setUp()
    FimRcFileState.clear()
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test equalTo`() {
    val file = """
      map x y
    """.trimIndent()

    FimRcFileState.saveFileState("", file)

    val document = editorFactory.createDocument(file)

    assertTrue(FimRcFileState.equalTo(document))
  }

  // TODO
//  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
//  fun `test equalTo with whitespaces`() {
//    val s = " " // Just to see whitespaces in the following code
//    val origFile = """
//      map x y
//      set myPlugin
//      map z t
//    """.trimIndent()
//    val changedFile = """
//      map x y
//      set myPlugin$s$s$s$s$s$s
//
//
//            map z t
//    """.trimIndent()
//
//    val lines = convertFileToLines(origFile)
//    FimRcFileState.saveFileState("", lines)
//
//    val document = editorFactory.createDocument(changedFile)
//
//    assertTrue(FimRcFileState.equalTo(document))
//  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test equalTo with whitespaces and comments`() {
    val s = " " // Just to see whitespaces in the following code
    val origFile = """
      map x y|"comment
      set nu
      set relativenumber" another comment
    """.trimIndent()
    val changedFile = """
      " comment
      map x y
      set ${s}${s}${s}nu
      set relativenumber
    """.trimIndent()

    FimRcFileState.saveFileState("", origFile)

    val document = editorFactory.createDocument(changedFile)

    assertTrue(FimRcFileState.equalTo(document))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test equalTo add line`() {
    val origFile = """
      map x y
      set myPlugin
    """.trimIndent()
    val changedFile = """
      map x y
      set myPlugin
      map z t
    """.trimIndent()

    FimRcFileState.saveFileState("", origFile)

    val document = editorFactory.createDocument(changedFile)

    assertFalse(FimRcFileState.equalTo(document))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test equalTo remove line`() {
    val origFile = """
      map x y
      set myPlugin
    """.trimIndent()
    val changedFile = """
      map x y
    """.trimIndent()

    FimRcFileState.saveFileState("", origFile)

    val document = editorFactory.createDocument(changedFile)

    assertFalse(FimRcFileState.equalTo(document))
  }
}
