package com.flop.idea.fim.api

interface FimTemplateManager {
  fun getTemplateState(editor: FimEditor): FimTemplateState?
}

interface FimTemplateState
