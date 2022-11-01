package com.flop.idea.fim.api

interface FimLookupManager {
  fun getActiveLookup(editor: FimEditor): IdeLookup?
}

interface IdeLookup {
  fun down(caret: FimCaret, context: ExecutionContext)
  fun up(caret: FimCaret, context: ExecutionContext)
}
