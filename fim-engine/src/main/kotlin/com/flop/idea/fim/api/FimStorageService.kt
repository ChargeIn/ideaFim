package com.flop.idea.fim.api

interface FimStorageService {
  /**
   * Gets data from editor (in Fim it is called window).
   * Fim stores there window scoped (`w:`) variables and local options.
   *
   * NOTE: data should remain in window even if it is moved to another split
   * @param editor editor/window to get the value from
   * @param key key
   */
  fun <T> getDataFromEditor(editor: FimEditor, key: Key<T>): T?

  /**
   * Puts data to editor (in Fim it is called window).
   * Fim stores there window scoped (`w:`) variables and local options.
   *
   * NOTE: data should remain in window even if it is moved to another split
   * @param editor editor/window to store the value
   * @param key key
   * @param data data to store
   */
  fun <T> putDataToEditor(editor: FimEditor, key: Key<T>, data: T)

  /**
   * Gets data from buffer
   * Fim stores there buffer scoped (`b:`) variables and local options.
   *
   * @param editor editor/window with the buffer opened
   * @param key key
   */
  fun <T> getDataFromBuffer(editor: FimEditor, key: Key<T>): T?

  /**
   * Puts data to buffer
   * Fim stores there buffer scoped (`b:`) variables and local options.
   *
   * @param editor editor/window with the buffer opened
   * @param key key
   * @param data data to store
   */
  fun <T> putDataToBuffer(editor: FimEditor, key: Key<T>, data: T)

  /**
   * Gets data from tab (group of windows)
   * Fim stores there tab page scoped (`t:`) variables
   *
   * @param editor editor/window in the tap page
   * @param key key
   */
  fun <T> getDataFromTab(editor: FimEditor, key: Key<T>): T?

  /**
   * Puts data to tab (group of windows)
   * Fim stores there tab page scoped (`t:`) variables
   *
   * @param editor editor/window in the tap page
   * @param key key
   * @param data data to store
   */
  fun <T> putDataToTab(editor: FimEditor, key: Key<T>, data: T)
}

data class Key<T>(val name: String)
