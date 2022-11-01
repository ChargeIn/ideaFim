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

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeWithMe.ClientId
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.ClientEditorManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import java.util.stream.Collectors

/**
 * This annotation is created for test functions (methods).
 * It means that the original fim behavior has small differences from behavior of IdeaFim.
 * [shouldBeFixed] flag indicates whether the given functionality should be fixed
 *   or the given behavior is normal for IdeaFim and should be leaved as is.
 *
 * E.g. after execution of some commands original fim has the following text:
 *    Hello1
 *    Hello2
 *    Hello3
 *
 * But IdeaFim gives you:
 *    Hello1
 *
 *    Hello2
 *    Hello3
 *
 * In this case you should still create the test function and mark this function with [FimBehaviorDiffers] annotation.
 *
 * Why does this annotation exist?
 * After creating some functionality you can understand that IdeaFim has a bit different behavior, but you
 *   cannot fix it right now because of any reason (bugs in IDE,
 *   the impossibility of this functionality in IDEA (*[shouldBeFixed] == false*), leak of time for fixing).
 *   In that case, you should NOT remove the corresponding test or leave it without any marks that this test
 *   not fully convenient with fim, but leave the test with IdeaFim's behavior and put this annotation
 *   with description of how original fim works.
 *
 * Note that using this annotation should be avoided as much as possible and behavior of IdeaFim should be as close
 *   to fim as possible.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class FimBehaviorDiffers(
  val originalFimAfter: String = "",
  val description: String = "",
  val shouldBeFixed: Boolean = true,
)

fun <T : Comparable<T>> sort(a: T, b: T) = if (a > b) b to a else a to b

// TODO Should be replaced with FimEditor.carets()
inline fun Editor.fimForEachCaret(action: (caret: Caret) -> Unit) {
  if (this.inBlockSubMode) {
    action(this.caretModel.primaryCaret)
  } else {
    this.caretModel.allCarets.forEach(action)
  }
}

fun Editor.getTopLevelEditor() = if (this is EditorWindow) this.delegate else this

/**
 * Return list of editors for local host (for code with me plugin)
 */
fun localEditors(): List<Editor> {
  return ClientEditorManager.getCurrentInstance().editors().collect(Collectors.toList())
}

fun localEditors(doc: Document): List<Editor> {
  return EditorFactory.getInstance().getEditors(doc)
    .filter { editor -> editor.editorClientId.let { it == null || it == ClientId.currentOrNull } }
}

fun localEditors(doc: Document, project: Project): List<Editor> {
  return EditorFactory.getInstance().getEditors(doc, project)
    .filter { editor -> editor.editorClientId.let { it == null || it == ClientId.currentOrNull } }
}

val Editor.editorClientId: ClientId?
  get() {
    if (editorClientKey == null) {
      @Suppress("DEPRECATION")
      editorClientKey = Key.findKeyByName("editorClientIdby userData()") ?: return null
    }
    return editorClientKey?.let { this.getUserData(it) as? ClientId }
  }

private var editorClientKey: Key<*>? = null

@Suppress("IncorrectParentDisposable")
fun Editor.isTemplateActive(): Boolean {
  val project = this.project ?: return false
  // XXX: I've disabled this check to find the stack trace where the project is disposed
//  if (project.isDisposed) return false
  return TemplateManager.getInstance(project).getActiveTemplate(this) != null
}

/**
 * This annotations marks if annotated function required read or write lock
 */
@Target
annotation class RWLockLabel {
  /**
   * [Readonly] annotation means that annotated function should be called from read action
   * This annotation is only a marker and doesn't enable r/w lock automatically
   */
  @Target(AnnotationTarget.FUNCTION)
  annotation class Readonly

  /**
   * [Writable] annotation means that annotated function should be called from write action
   * This annotation is only a marker and doesn't enable r/w lock automatically
   */
  @Suppress("unused")
  @Target(AnnotationTarget.FUNCTION)
  annotation class Writable

  /**
   * [SelfSynchronized] annotation means that annotated function handles read/write lock by itself
   * This annotation is only a marker and doesn't enable r/w lock automatically
   */
  @Target(AnnotationTarget.FUNCTION)
  annotation class SelfSynchronized

  /**
   * [NoLockRequired] annotation means that annotated function doesn't require any lock
   * This annotation is only a marker and doesn't enable r/w lock automatically
   */
  @Target(AnnotationTarget.FUNCTION)
  annotation class NoLockRequired
}

fun fimEnabled(editor: Editor?): Boolean {
  if (!com.flop.idea.fim.FimPlugin.isEnabled()) return false
  if (editor != null && editor.isIdeaFimDisabledHere) return false
  return true
}

fun fimDisabled(editor: Editor?): Boolean = !fimEnabled(editor)

fun experimentalApi(): Boolean {
  return com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.experimentalapiName)
}
