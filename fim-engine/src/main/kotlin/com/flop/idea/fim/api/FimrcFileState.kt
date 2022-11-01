package com.flop.idea.fim.api

interface FimrcFileState {
  var filePath: String?

  fun saveFileState(filePath: String)
}
