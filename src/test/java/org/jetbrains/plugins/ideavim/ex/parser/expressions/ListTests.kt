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

package org.jetbrains.plugins.ideafim.ex.parser.expressions

import com.flop.idea.fim.fimscript.model.datatypes.FimDictionary
import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimList
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.parser.FimscriptParser
import org.jetbrains.plugins.ideafim.ex.evaluate
import org.junit.Test
import kotlin.test.assertEquals

class ListTests {

  @Test
  fun `empty list test`() {
    assertEquals(FimList(mutableListOf()), FimscriptParser.parseExpression("[]")!!.evaluate())
  }

  @Test
  fun `list of simple types test`() {
    assertEquals(
      FimList(
        mutableListOf(
          FimInt(1),
          FimFloat(4.6),
          FimString("bla bla"),
          FimList(mutableListOf(FimInt(5), FimInt(9))),
          FimDictionary(linkedMapOf(FimString("key") to FimString("value")))
        )
      ),
      FimscriptParser.parseExpression("[1, 4.6, 'bla bla', [5, 9], {'key' : 'value'}]")!!.evaluate()
    )
  }

  @Test
  fun `comma at the list end test`() {
    assertEquals(FimList(mutableListOf(FimInt(1), FimInt(2))), FimscriptParser.parseExpression("[1, 2,]")!!.evaluate())
  }
}
