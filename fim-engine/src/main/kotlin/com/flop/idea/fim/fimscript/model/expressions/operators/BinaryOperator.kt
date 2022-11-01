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

package com.flop.idea.fim.fimscript.model.expressions.operators

import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.AdditionHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.BinaryOperatorHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.ConcatenationHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.DivisionHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.DoesntMatchHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.DoesntMatchIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.EqualsCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.EqualsHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.EqualsIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.GreaterCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.GreaterHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.GreaterIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.GreaterOrEqualsCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.GreaterOrEqualsHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.GreaterOrEqualsIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.IsCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.IsHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.IsIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.IsNotCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.IsNotHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.IsNotIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LessCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LessHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LessIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LessOrEqualsCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LessOrEqualsHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LessOrEqualsIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LogicalAndHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.LogicalOrHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.MatchesCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.MatchesHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.MatchesIgnoreCaseHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.ModulusHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.MultiplicationHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.SubtractionHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.UnequalsCaseSensitiveHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.UnequalsHandler
import com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary.UnequalsIgnoreCaseHandler

enum class BinaryOperator(val value: String, val handler: BinaryOperatorHandler) {
  MULTIPLICATION("*", MultiplicationHandler),
  DIVISION("/", DivisionHandler),
  ADDITION("+", AdditionHandler),
  SUBTRACTION("-", SubtractionHandler),
  CONCATENATION(".", ConcatenationHandler),
  CONCATENATION2("..", ConcatenationHandler),
  LESS("<", LessHandler),
  LESS_IGNORE_CASE("<?", LessIgnoreCaseHandler),
  LESS_CASE_SENSITIVE("<#", LessCaseSensitiveHandler),
  GREATER(">", GreaterHandler),
  GREATER_IGNORE_CASE(">?", GreaterIgnoreCaseHandler),
  GREATER_CASE_SENSITIVE(">#", GreaterCaseSensitiveHandler),
  EQUALS("==", EqualsHandler),
  EQUALS_IGNORE_CASE("==?", EqualsIgnoreCaseHandler),
  EQUALS_CASE_SENSITIVE("==#", EqualsCaseSensitiveHandler),
  UNEQUALS("!=", UnequalsHandler),
  UNEQUALS_IGNORE_CASE("!=?", UnequalsIgnoreCaseHandler),
  UNEQUALS_CASE_SENSITIVE("!=#", UnequalsCaseSensitiveHandler),
  GREATER_OR_EQUALS(">=", GreaterOrEqualsHandler),
  GREATER_OR_EQUALS_IGNORE_CASE(">=?", GreaterOrEqualsIgnoreCaseHandler),
  GREATER_OR_EQUALS_CASE_SENSITIVE(">=#", GreaterOrEqualsCaseSensitiveHandler),
  LESS_OR_EQUALS("<=", LessOrEqualsHandler),
  LESS_OR_EQUALS_IGNORE_CASE("<=?", LessOrEqualsIgnoreCaseHandler),
  LESS_OR_EQUALS_CASE_SENSITIVE("<=#", LessOrEqualsCaseSensitiveHandler),
  MODULUS("%", ModulusHandler),
  LOGICAL_AND("&&", LogicalAndHandler),
  LOGICAL_OR("||", LogicalOrHandler),
  IS("is", IsHandler),
  IS_IGNORE_CASE("is?", IsIgnoreCaseHandler),
  IS_CASE_SENSITIVE("is#", IsCaseSensitiveHandler),
  IS_NOT("isnot", IsNotHandler),
  IS_NOT_IGNORE_CASE("isnot?", IsNotIgnoreCaseHandler),
  IS_NOT_CASE_SENSITIVE("isnot#", IsNotCaseSensitiveHandler),
  MATCHES("=~", MatchesHandler),
  MATCHES_IGNORE_CASE("=~?", MatchesIgnoreCaseHandler),
  MATCHES_CASE_SENSITIVE("=~#", MatchesCaseSensitiveHandler),
  DOESNT_MATCH("!~", DoesntMatchHandler),
  DOESNT_MATCH_IGNORE_CASE("!~?", DoesntMatchIgnoreCaseHandler),
  DOESNT_MATCH_CASE_SENSITIVE("!~#", DoesntMatchIgnoreCaseHandler);

  companion object {
    fun getByValue(value: String): BinaryOperator? {
      return values().firstOrNull { it.value == value }
    }
  }
}
