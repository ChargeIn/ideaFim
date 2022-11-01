package com.flop.idea.fim.api.stubs

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.FimProcessGroupBase
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.diagnostic.fimLogger
import javax.swing.KeyStroke

class FimProcessGroupStub : FimProcessGroupBase() {
  init {
    fimLogger<ExecutionContextManagerStub>().warn("FimProcessGroupStub is used. Please replace it with your own implementation of FimProcessGroup.")
  }

  override val lastCommand: String
    get() = TODO("Not yet implemented")

  override fun startSearchCommand(editor: FimEditor, context: ExecutionContext?, count: Int, leader: Char) {
    TODO("Not yet implemented")
  }

  override fun endSearchCommand(): String {
    TODO("Not yet implemented")
  }

  override fun processExKey(editor: FimEditor, stroke: KeyStroke): Boolean {
    TODO("Not yet implemented")
  }

  override fun startFilterCommand(editor: FimEditor, context: ExecutionContext?, cmd: Command) {
    TODO("Not yet implemented")
  }

  override fun startExCommand(editor: FimEditor, context: ExecutionContext?, cmd: Command) {
    TODO("Not yet implemented")
  }

  override fun processExEntry(editor: FimEditor, context: ExecutionContext): Boolean {
    TODO("Not yet implemented")
  }

  override fun cancelExEntry(editor: FimEditor, resetCaret: Boolean) {
    TODO("Not yet implemented")
  }

  override fun executeCommand(
    editor: FimEditor,
    command: String,
    input: CharSequence?,
    currentDirectoryPath: String?,
  ): String? {
    TODO("Not yet implemented")
  }
}
