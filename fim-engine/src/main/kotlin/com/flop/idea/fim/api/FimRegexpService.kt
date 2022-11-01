package com.flop.idea.fim.api

interface FimRegexpService {
  fun matches(pattern: String, text: String?, ignoreCase: Boolean = false): Boolean
}
