package com.flop.idea.fim.api.stubs

import com.flop.idea.fim.api.FimEnabler
import com.flop.idea.fim.diagnostic.fimLogger

class FimEnablerStub : FimEnabler {
  init {
    fimLogger<ExecutionContextManagerStub>().warn("FimEnablerStub is used. Please replace it with your own implementation of FimEnabler.")
  }

  override fun isEnabled(): Boolean {
    TODO("Not yet implemented")
  }
}
