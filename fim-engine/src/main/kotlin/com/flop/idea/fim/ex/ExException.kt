package com.flop.idea.fim.ex

open class ExException(s: String? = null) : Exception(s) {
  var code: String? = null
}
