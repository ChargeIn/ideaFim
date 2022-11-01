package com.flop.idea.fim.api

import com.flop.idea.fim.fimscript.model.ExecutionResult
import com.flop.idea.fim.fimscript.model.FimLContext
import java.io.File

interface FimscriptExecutor {

  var executingFimscript: Boolean

  fun execute(script: String, editor: FimEditor, context: ExecutionContext, skipHistory: Boolean, indicateErrors: Boolean = true, fimContext: FimLContext? = null): ExecutionResult

  fun execute(script: String, skipHistory: Boolean = true)

  fun executeFile(file: File, indicateErrors: Boolean = false)

  fun executeLastCommand(editor: FimEditor, context: ExecutionContext): Boolean
}
