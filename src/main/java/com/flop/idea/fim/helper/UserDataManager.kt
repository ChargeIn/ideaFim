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

@file:JvmName("UserDataManager")
@file:Suppress("ObjectPropertyName")

package com.flop.idea.fim.helper

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import com.flop.idea.fim.api.CaretRegisterStorageBase
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.ex.ExOutputModel
import com.flop.idea.fim.group.visual.VisualChange
import com.flop.idea.fim.group.visual.fimLeadSelectionOffset
import com.flop.idea.fim.ui.ExOutputPanel
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @author Alex Plate
 */

//region Fim selection start ----------------------------------------------------
/**
 * Caret's offset when entering visual mode
 */
var Caret.fimSelectionStart: Int
  get() {
    val selectionStart = _fimSelectionStart
    if (selectionStart == null) {
      fimSelectionStart = fimLeadSelectionOffset
      return fimLeadSelectionOffset
    }
    return selectionStart
  }
  set(value) {
    _fimSelectionStart = value
  }

fun Caret.fimSelectionStartClear() {
  this._fimSelectionStart = null
}

private var Caret._fimSelectionStart: Int? by userDataCaretToEditor()
//endregion ----------------------------------------------------

// Last column excluding inlays before the caret
var Caret.fimLastColumn: Int by userDataCaretToEditorOr { (this as Caret).inlayAwareVisualColumn }
var Caret.fimLastVisualOperatorRange: VisualChange? by userDataCaretToEditor()
var Caret.fimInsertStart: RangeMarker by userDataOr {
  (this as Caret).editor.document.createRangeMarker(
    this.offset,
    this.offset
  )
}
var Caret.registerStorage: CaretRegisterStorageBase? by userDataCaretToEditor()

// ------------------ Editor
fun unInitializeEditor(editor: Editor) {
  editor.fimLastSelectionType = null
  editor.fimStateMachine = null
  editor.fimMorePanel = null
  editor.fimExOutput = null
  editor.fimLastHighlighters = null
}

var Editor.fimLastSearch: String? by userData()
var Editor.fimLastHighlighters: MutableCollection<RangeHighlighter>? by userData()
var Editor.fimIncsearchCurrentMatchOffset: Int? by userData()

/***
 * @see :help visualmode()
 */
var Editor.fimLastSelectionType: SelectionType? by userData()
var Editor.fimStateMachine: FimStateMachine? by userData()
var Editor.fimEditorGroup: Boolean by userDataOr { false }
var Editor.fimLineNumbersInitialState: Boolean by userDataOr { false }
var Editor.fimHasRelativeLineNumbersInstalled: Boolean by userDataOr { false }
var Editor.fimMorePanel: com.flop.idea.fim.ui.ExOutputPanel? by userData()
var Editor.fimExOutput: ExOutputModel? by userData()
var Editor.fimTestInputModel: com.flop.idea.fim.helper.TestInputModel? by userData()

/**
 * Checks whether a keeping visual mode visual operator action is performed on editor.
 */
var Editor.fimKeepingVisualOperatorAction: Boolean by userDataOr { false }
var Editor.fimChangeActionSwitchMode: FimStateMachine.Mode? by userData()

/**
 * Function for delegated properties.
 * The property will be delegated to UserData and has nullable type.
 */
fun <T> userData(): ReadWriteProperty<UserDataHolder, T?> =
  object : UserDataReadWriteProperty<UserDataHolder, T?>() {
    override fun getValue(thisRef: UserDataHolder, property: KProperty<*>): T? {
      return thisRef.getUserData(getKey(property))
    }

    override fun setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) {
      thisRef.putUserData(getKey(property), value)
    }
  }

/**
 * Function for delegated properties.
 * The property will be saved to caret if this caret is not primary
 *   and to caret and editor otherwise.
 * In case of primary caret getter uses value stored in caret. If it's null, then the value from editor
 * Has nullable type.
 */
private fun <T> userDataCaretToEditor(): ReadWriteProperty<Caret, T?> =
  object : UserDataReadWriteProperty<Caret, T?>() {
    override fun getValue(thisRef: Caret, property: KProperty<*>): T? {
      return if (thisRef == thisRef.editor.caretModel.primaryCaret) {
        thisRef.getUserData(getKey(property)) ?: thisRef.editor.getUserData(getKey(property))
      } else {
        thisRef.getUserData(getKey(property))
      }
    }

    override fun setValue(thisRef: Caret, property: KProperty<*>, value: T?) {
      if (thisRef == thisRef.editor.caretModel.primaryCaret) {
        thisRef.editor.putUserData(getKey(property), value)
      }
      thisRef.putUserData(getKey(property), value)
    }
  }

/**
 * Function for delegated properties.
 * The property will be saved to caret if this caret is not primary
 *   and to caret and editor otherwise.
 * In case of primary caret getter uses value stored in caret. If it's null, then the value from editor
 * Has not nullable type.
 */
private fun <T> userDataCaretToEditorOr(default: UserDataHolder.() -> T): ReadWriteProperty<Caret, T> =
  object : UserDataReadWriteProperty<Caret, T>() {
    override fun getValue(thisRef: Caret, property: KProperty<*>): T {
      val res = if (thisRef == thisRef.editor.caretModel.primaryCaret) {
        thisRef.getUserData(getKey(property)) ?: thisRef.editor.getUserData(getKey(property))
      } else {
        thisRef.getUserData(getKey(property))
      }

      if (res == null) {
        val defaultValue = thisRef.default()
        setValue(thisRef, property, defaultValue)
        return defaultValue
      }
      return res
    }

    override fun setValue(thisRef: Caret, property: KProperty<*>, value: T) {
      if (thisRef == thisRef.editor.caretModel.primaryCaret) {
        thisRef.editor.putUserData(getKey(property), value)
      }
      thisRef.putUserData(getKey(property), value)
    }
  }

/**
 * Function for delegated properties.
 * The property will be delegated to UserData and has non-nullable type.
 * [default] action will be executed if UserData doesn't have this property now.
 *   The result of [default] will be put to user data and returned.
 */
private fun <T> userDataOr(default: UserDataHolder.() -> T): ReadWriteProperty<UserDataHolder, T> =
  object : UserDataReadWriteProperty<UserDataHolder, T>() {
    override fun getValue(thisRef: UserDataHolder, property: KProperty<*>): T {
      return thisRef.getUserData(getKey(property)) ?: run<ReadWriteProperty<UserDataHolder, T>, T> {
        val defaultValue = thisRef.default()
        thisRef.putUserData(getKey(property), defaultValue)
        defaultValue
      }
    }

    override fun setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T) {
      thisRef.putUserData(getKey(property), value)
    }
  }

private abstract class UserDataReadWriteProperty<in R, T> : ReadWriteProperty<R, T> {
  private var key: Key<T>? = null
  protected fun getKey(property: KProperty<*>): Key<T> {
    if (key == null) {
      key = Key.create(property.name + " by userData()")
    }
    return key as Key<T>
  }
}
