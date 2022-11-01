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

import com.intellij.openapi.util.Key
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType

class IjFimLocalOptions : FimLocalOptions {

  private val localValuesKey = Key<MutableMap<String, FimDataType>>("localOptions")

  override fun getOptions(editor: FimEditor): Map<String, FimDataType> {
    val ijEditor = (editor as IjFimEditor).editor

    return ijEditor.getUserData(localValuesKey) ?: emptyMap()
  }

  override fun setOption(editor: FimEditor, optionName: String, value: FimDataType) {
    val ijEditor = (editor as IjFimEditor).editor

    if (ijEditor.getUserData(localValuesKey) == null) {
      ijEditor.putUserData(localValuesKey, mutableMapOf(optionName to value))
    } else {
      ijEditor.getUserData(localValuesKey)!![optionName] = value
    }
  }

  override fun reset(editor: FimEditor) {
    val ijEditor = (editor as IjFimEditor).editor

    ijEditor.getUserData(localValuesKey)?.clear()
  }
}
