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

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import com.flop.idea.fim.FimTypedActionHandler
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * It's needed to wait till JupyterCommandModeTypingBlocker is going to be registered using an extension point
 * After that, we would be able to register our typingHandler before (or after) the one from jupyter.
 */
class HandlerInjector {
  companion object {
    @JvmStatic
    fun inject(): TypedActionHandler? {
      try {
        val javaClass = TypedAction.getInstance().rawHandler::class.java
        val pythonHandler = javaClass.kotlin.objectInstance
        val field =
          javaClass.declaredFields.singleOrNull { it.name == "DEFAULT_RAW_HANDLER" || it.name == "editModeRawHandler" }
            ?: return null
        field.isAccessible = true

        val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

        val originalHandler = field.get(pythonHandler) as? TypedActionHandler ?: return null
        val newFimHandler = FimTypedActionHandler(originalHandler)
        field.set(pythonHandler, newFimHandler)
        return originalHandler
      } catch (ignored: Exception) {
        // Ignore
      }
      return null
    }

    @JvmStatic
    fun notebookCommandMode(editor: Editor?): Boolean {
      return if (editor != null) {
        val inEditor = com.flop.idea.fim.helper.EditorHelper.getVirtualFile(editor)?.extension == "ipynb"
        TypedAction.getInstance().rawHandler::class.java.simpleName.equals("JupyterCommandModeTypingBlocker") && inEditor
      } else {
        TypedAction.getInstance().rawHandler::class.java.simpleName.equals("JupyterCommandModeTypingBlocker")
      }
    }
  }
}
