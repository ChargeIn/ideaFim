package com.flop.idea.fim.api.stubs

import com.flop.idea.fim.api.FimMessagesBase
import com.flop.idea.fim.diagnostic.fimLogger

class FimMessagesStub : FimMessagesBase() {
  init {
    fimLogger<ExecutionContextManagerStub>().warn("FimMessagesStub is used. Please replace it with your own implementation of FimMessages.")
  }

  override fun showStatusBarMessage(message: String?) {
    TODO("Not yet implemented")
  }

  override fun getStatusBarMessage(): String? {
    TODO("Not yet implemented")
  }

  override fun indicateError() {
    TODO("Not yet implemented")
  }

  override fun clearError() {
    TODO("Not yet implemented")
  }

  override fun isError(): Boolean {
    TODO("Not yet implemented")
  }

  override fun message(key: String, vararg params: Any): String {
    TODO("Not yet implemented")
  }

  override fun updateStatusBar() {
    TODO("Not yet implemented")
  }
}
