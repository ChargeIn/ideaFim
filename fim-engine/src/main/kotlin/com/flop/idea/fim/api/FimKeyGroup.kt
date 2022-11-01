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
package com.flop.idea.fim.api

import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.key.CommandPartNode
import com.flop.idea.fim.key.KeyMapping
import com.flop.idea.fim.key.KeyMappingLayer
import com.flop.idea.fim.key.MappingInfo
import com.flop.idea.fim.key.MappingOwner
import com.flop.idea.fim.key.OperatorFunction
import com.flop.idea.fim.key.ShortcutOwnerInfo
import com.flop.idea.fim.fimscript.model.expressions.Expression
import javax.swing.KeyStroke

interface FimKeyGroup {
  fun getKeyRoot(mappingMode: MappingMode): CommandPartNode<FimActionsInitiator>
  fun getKeyMappingLayer(mode: MappingMode): KeyMappingLayer
  fun getActions(editor: FimEditor, keyStroke: KeyStroke): List<NativeAction>
  fun getKeymapConflicts(keyStroke: KeyStroke): List<NativeAction>

  fun putKeyMapping(
    modes: Set<MappingMode>,
    fromKeys: List<KeyStroke>,
    owner: MappingOwner,
    extensionHandler: ExtensionHandler,
    recursive: Boolean,
  )

  fun putKeyMapping(
    modes: Set<MappingMode>,
    fromKeys: List<KeyStroke>,
    owner: MappingOwner,
    toKeys: List<KeyStroke>,
    recursive: Boolean,
  )

  fun putKeyMapping(
    modes: Set<MappingMode>,
    fromKeys: List<KeyStroke>,
    owner: MappingOwner,
    toExpr: Expression,
    originalString: String,
    recursive: Boolean,
  )

  fun removeKeyMapping(owner: MappingOwner)
  fun removeKeyMapping(modes: Set<MappingMode>)
  fun removeKeyMapping(modes: Set<MappingMode>, keys: List<KeyStroke>)
  fun showKeyMappings(modes: Set<MappingMode>, editor: FimEditor): Boolean
  fun getKeyMapping(mode: MappingMode): KeyMapping
  fun getKeyMappingByOwner(owner: MappingOwner): List<Pair<List<KeyStroke>, MappingInfo>>
  fun updateShortcutKeysRegistration()
  fun getMapTo(mode: MappingMode, toKeys: List<KeyStroke>): List<Pair<List<KeyStroke>, MappingInfo>>
  fun unregisterCommandActions()
  fun resetKeyMappings()
  fun hasmapto(mode: MappingMode, toKeys: List<KeyStroke>): Boolean

  val shortcutConflicts: MutableMap<KeyStroke, ShortcutOwnerInfo>
  val savedShortcutConflicts: MutableMap<KeyStroke, ShortcutOwnerInfo>
  var operatorFunction: OperatorFunction?
}
