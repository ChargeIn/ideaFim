package com.flop.idea.fim.api.stubs

import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.ExecutionContextManager
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.diagnostic.fimLogger

class ExecutionContextManagerStub : ExecutionContextManager {
  init {
    fimLogger<ExecutionContextManagerStub>().warn("ExecutionContextManagerStub is used. Please replace it with your own implementation of ExecutionContextManager.")
  }

  override fun onEditor(editor: FimEditor, prevContext: ExecutionContext?): ExecutionContext {
    TODO("Not yet implemented")
  }

  override fun onCaret(caret: FimCaret, prevContext: ExecutionContext): ExecutionContext {
    TODO("Not yet implemented")
  }

  override fun createCaretSpecificDataContext(context: ExecutionContext, caret: FimCaret): ExecutionContext {
    TODO("Not yet implemented")
  }

  override fun createEditorDataContext(editor: FimEditor, context: ExecutionContext): ExecutionContext {
    TODO("Not yet implemented")
  }
}
