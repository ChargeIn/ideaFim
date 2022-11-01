package com.flop.idea.fim.api

interface ExEntryPanel {
  fun isActive(): Boolean
  fun clearCurrentAction()
  fun setCurrentActionPromptCharacter(char: Char)
}
