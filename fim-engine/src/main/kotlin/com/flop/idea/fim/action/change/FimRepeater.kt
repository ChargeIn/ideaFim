package com.flop.idea.fim.action.change

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command

object FimRepeater {
  var repeatHandler = false

  var lastChangeCommand: Command? = null
    private set
  var lastChangeRegister = injector.registerGroup.defaultRegister

  fun saveLastChange(command: Command) {
    lastChangeCommand = command
    lastChangeRegister = injector.registerGroup.currentRegister
  }
}
