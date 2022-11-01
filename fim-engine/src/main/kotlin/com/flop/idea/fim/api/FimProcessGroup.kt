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

import com.flop.idea.fim.command.Command
import javax.swing.KeyStroke

interface FimProcessGroup {
  val lastCommand: String?

  fun startSearchCommand(editor: FimEditor, context: ExecutionContext?, count: Int, leader: Char)
  fun endSearchCommand(): String
  fun processExKey(editor: FimEditor, stroke: KeyStroke): Boolean
  fun startFilterCommand(editor: FimEditor, context: ExecutionContext?, cmd: Command)
  fun startExCommand(editor: FimEditor, context: ExecutionContext?, cmd: Command)
  fun processExEntry(editor: FimEditor, context: ExecutionContext): Boolean
  fun cancelExEntry(editor: FimEditor, resetCaret: Boolean)
  @kotlin.jvm.Throws(java.lang.Exception::class)
  fun executeCommand(editor: FimEditor, command: String, input: CharSequence?, currentDirectoryPath: String?): String?
}
