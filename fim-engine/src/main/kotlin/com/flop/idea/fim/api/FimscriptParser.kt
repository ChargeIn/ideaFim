package com.flop.idea.fim.api

import com.flop.idea.fim.fimscript.model.Script
import com.flop.idea.fim.fimscript.model.commands.Command
import com.flop.idea.fim.fimscript.model.expressions.Expression

interface FimscriptParser {

  fun parse(script: String): Script
  fun parseCommand(command: String): Command?
  fun parseExpression(expression: String): Expression?
}
