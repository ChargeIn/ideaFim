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

package com.flop.idea.fim.fimscript.model.expressions.operators.handlers.binary

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType

abstract class BinaryOperatorWithIgnoreCaseOption(
  private val caseInsensitiveImpl: BinaryOperatorHandler,
  private val caseSensitiveImpl: BinaryOperatorHandler,
) : BinaryOperatorHandler() {

  private fun shouldIgnoreCase(): Boolean {
    return injector.optionService.isSet(OptionScope.GLOBAL, OptionConstants.ignorecaseName)
  }

  override fun performOperation(left: FimDataType, right: FimDataType): FimDataType {
    return if (shouldIgnoreCase()) caseInsensitiveImpl.performOperation(left, right) else
      caseSensitiveImpl.performOperation(left, right)
  }
}
