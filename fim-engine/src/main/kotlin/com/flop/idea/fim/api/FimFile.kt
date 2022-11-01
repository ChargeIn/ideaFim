package com.flop.idea.fim.api

interface FimFile {
  fun displayFileInfo(fimEditor: FimEditor, fullPath: Boolean)
  fun displayHexInfo(editor: FimEditor)
  fun displayLocationInfo(fimEditor: FimEditor)
  fun selectPreviousTab(context: ExecutionContext)
  fun saveFile(context: ExecutionContext)
  fun saveFiles(context: ExecutionContext)
  fun closeFile(editor: FimEditor, context: ExecutionContext)
  fun closeFile(number: Int, context: ExecutionContext)
  fun selectFile(count: Int, context: ExecutionContext): Boolean
  fun selectNextFile(count: Int, context: ExecutionContext)
  fun openFile(filename: String, context: ExecutionContext): Boolean
}
