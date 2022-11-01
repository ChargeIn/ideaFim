package com.flop.idea.fim.api.stubs

import com.flop.idea.fim.api.FimApplicationBase
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.diagnostic.fimLogger
import javax.swing.KeyStroke

class FimApplicationStub : FimApplicationBase() {
  init {
    fimLogger<ExecutionContextManagerStub>().warn("FimApplicationStub is used. Please replace it with your own implementation of FimApplication.")
  }

  override fun isMainThread(): Boolean {
    TODO("Not yet implemented")
  }

  override fun invokeLater(action: () -> Unit, editor: FimEditor) {
    TODO("Not yet implemented")
  }

  override fun invokeLater(action: () -> Unit) {
    TODO("Not yet implemented")
  }

  override fun isUnitTest(): Boolean {
    TODO("Not yet implemented")
  }

  override fun postKey(stroke: KeyStroke, editor: FimEditor) {
    TODO("Not yet implemented")
  }

  override fun localEditors(): List<FimEditor> {
    TODO("Not yet implemented")
  }

  override fun runWriteCommand(editor: FimEditor, name: String?, groupId: Any?, command: Runnable) {
    TODO("Not yet implemented")
  }

  override fun runReadCommand(editor: FimEditor, name: String?, groupId: Any?, command: Runnable) {
    TODO("Not yet implemented")
  }

  override fun <T> runWriteAction(action: () -> T): T {
    TODO("Not yet implemented")
  }

  override fun <T> runReadAction(action: () -> T): T {
    TODO("Not yet implemented")
  }

  override fun currentStackTrace(): String {
    TODO("Not yet implemented")
  }

  override fun runAfterGotFocus(runnable: Runnable) {
    TODO("Not yet implemented")
  }
}
