package com.flop.idea.fim.api

interface FimExOutputPanelService {
  fun getPanel(editor: FimEditor): FimExOutputPanel
}

interface FimExOutputPanel {
  val text: String?

  fun output(text: String)
  fun clear()
}
