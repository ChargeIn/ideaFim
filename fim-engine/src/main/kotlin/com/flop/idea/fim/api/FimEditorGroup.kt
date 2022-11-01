package com.flop.idea.fim.api

interface FimEditorGroup {
  fun notifyIdeaJoin(editor: FimEditor)
  fun localEditors(): Collection<FimEditor>
}
