package com.flop.idea.fim.api

import java.lang.Long.toHexString

abstract class FimFileBase : FimFile {
  override fun displayHexInfo(editor: FimEditor) {
    val offset = editor.currentCaret().offset.point
    val ch = editor.text()[offset]

    injector.messages.showStatusBarMessage(toHexString(ch.code.toLong()))
  }
}
