package com.flop.idea.fim.undo

import com.flop.idea.fim.api.ExecutionContext

interface FimUndoRedo {
  fun undo(context: ExecutionContext): Boolean
  fun redo(context: ExecutionContext): Boolean
}
