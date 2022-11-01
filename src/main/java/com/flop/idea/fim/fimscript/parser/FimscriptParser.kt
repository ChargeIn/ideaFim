/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.flop.idea.fim.fimscript.parser

import com.intellij.openapi.diagnostic.logger
import com.flop.idea.fim.fimscript.model.Script
import com.flop.idea.fim.fimscript.model.commands.Command
import com.flop.idea.fim.fimscript.model.expressions.Expression
import com.flop.idea.fim.fimscript.parser.errors.IdeafimErrorListener
import com.flop.idea.fim.fimscript.parser.generated.FimscriptLexer
import com.flop.idea.fim.fimscript.parser.generated.FimscriptParser
import com.flop.idea.fim.fimscript.parser.visitors.CommandVisitor
import com.flop.idea.fim.fimscript.parser.visitors.ExpressionVisitor
import com.flop.idea.fim.fimscript.parser.visitors.ScriptVisitor
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

object FimscriptParser : com.flop.idea.fim.api.FimscriptParser {

  private val logger = logger<FimscriptParser>()
  val linesWithErrors = mutableListOf<Int>()
  private const val MAX_NUMBER_OF_TRIES = 5
  private var tries = 0

  override fun parse(script: String): Script {
    val preprocessedText = uncommentIdeaFimIgnore(getTextWithoutErrors(script))
    linesWithErrors.clear()
    val parser = getParser(addNewlineIfMissing(preprocessedText), true)
    val AST: ParseTree = parser.script()
    return if (linesWithErrors.isNotEmpty()) {
      if (tries > MAX_NUMBER_OF_TRIES) {
        // I don't think, that it's possible to enter an infinite recursion with any fimrc, but let's have it just in case
        logger.warn("Reached the maximum number of tries to fix a script. Parsing is stopped.")
        linesWithErrors.clear()
        tries = 0
        return Script(listOf())
      } else {
        tries += 1
        parse(preprocessedText)
      }
    } else {
      tries = 0
      ScriptVisitor.visit(AST)
    }
  }

  override fun parseExpression(expression: String): Expression? {
    val parser = getParser(expression, true)
    val AST: ParseTree = parser.expr()
    if (linesWithErrors.isNotEmpty()) {
      linesWithErrors.clear()
      return null
    }
    return ExpressionVisitor.visit(AST)
  }

  override fun parseCommand(command: String): Command? {
    val parser = getParser(addNewlineIfMissing(command), true)
    val AST: ParseTree = parser.command()
    if (linesWithErrors.isNotEmpty()) {
      linesWithErrors.clear()
      return null
    }
    return CommandVisitor.visit(AST)
  }

  // grammar expects that any command or script ends with a newline character
  private fun addNewlineIfMissing(text: String): String {
    if (text.isEmpty()) return "\n"
    return if (text.last() == '\n') {
      text
    } else if (text.last() == '\r') {
      // fix to do not erase the \r (e.g. :normal /search^M)
      text + "\r\n"
    } else {
      text + "\n"
    }
  }

  fun parseLetCommand(text: String): Command? {
    val parser = getParser(addNewlineIfMissing(text), true)
    val AST: ParseTree = parser.letCommands()
    if (linesWithErrors.isNotEmpty()) {
      linesWithErrors.clear()
      return null
    }
    return CommandVisitor.visit(AST)
  }

  private fun getParser(text: String, addListener: Boolean = false): FimscriptParser {
    val input: CharStream = CharStreams.fromString(text)
    val lexer = FimscriptLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = FimscriptParser(tokens)
    parser.errorListeners.clear()
    if (addListener) {
      parser.addErrorListener(IdeafimErrorListener())
    }
    return parser
  }

  private fun getTextWithoutErrors(text: String): String {
    linesWithErrors.sortDescending()
    val lineNumbersToDelete = linesWithErrors
    val lines = text.split("\n", "\r\n").toMutableList()
    for (lineNumber in lineNumbersToDelete) {
      // this may happen if we have an error somewhere at the end and parser can't find any matching token till EOF (EOF's line number is lines.size)
      if (lines.size <= lineNumber) {
        logger.warn("Parsing error affects lines till EOF")
      } else {
        lines.removeAt(lineNumber - 1)
      }
    }
    return lines.joinToString(separator = "\n")
  }

  private fun uncommentIdeaFimIgnore(configuration: String): String {
    return configuration.replace(Regex("\"( )*ideafim ignore", RegexOption.IGNORE_CASE), "ideafim ignore")
  }
}
