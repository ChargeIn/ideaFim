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
import org.jetbrains.plugins.ideafim.ex.parser.ParserTest
import org.junit.Test
import kotlin.test.assertEquals

class DictionaryTests : ParserTest() {

  @Test
  fun `empty dictionary test`() {
    assertEquals(
      FimDictionary(linkedMapOf()),
      FimscriptParser.parseExpression("{}")!!.evaluate()
    )
  }

  @Test
  fun `dictionary of simple types test`() {
    val dictString = "{$ZERO_OR_MORE_SPACES" +
      "'a'$ZERO_OR_MORE_SPACES:$ZERO_OR_MORE_SPACES'string expression'$ZERO_OR_MORE_SPACES,$ZERO_OR_MORE_SPACES" +
      "'b'$ZERO_OR_MORE_SPACES:$ZERO_OR_MORE_SPACES[1, 2]$ZERO_OR_MORE_SPACES" +
      "}"
    for (s in getTextWithAllSpacesCombinations(dictString)) {
      assertEquals(
        FimDictionary(
          linkedMapOf(
            FimString("a") to FimString("string expression"),
            FimString("b") to FimList(mutableListOf(FimInt(1), FimInt(2))),
          )
        ),
        FimscriptParser.parseExpression(s)!!.evaluate()
      )
    }
  }

  @Test
  fun `dictionary of simple types test 2`() {
    val dictString = "{$ZERO_OR_MORE_SPACES" +
      "'c'$ZERO_OR_MORE_SPACES:$ZERO_OR_MORE_SPACES{'key':'value'},$ZERO_OR_MORE_SPACES" +
      "'d'$ZERO_OR_MORE_SPACES:${ZERO_OR_MORE_SPACES}5$ZERO_OR_MORE_SPACES" +
      "}"
    for (s in getTextWithAllSpacesCombinations(dictString)) {
      assertEquals(
        FimDictionary(
          linkedMapOf(
            FimString("c") to FimDictionary(linkedMapOf(FimString("key") to FimString("value"))),
            FimString("d") to FimInt(5),
          )
        ),
        FimscriptParser.parseExpression(s)!!.evaluate()
      )
    }
  }

  @Test
  fun `dictionary of simple types test 3`() {
    val dictString = "{$ZERO_OR_MORE_SPACES" +
      "'e'$ZERO_OR_MORE_SPACES:${ZERO_OR_MORE_SPACES}4.2$ZERO_OR_MORE_SPACES" +
      "}"
    for (s in getTextWithAllSpacesCombinations(dictString)) {
      assertEquals(
        FimDictionary(
          linkedMapOf(
            FimString("e") to FimFloat(4.2)
          )
        ),
        FimscriptParser.parseExpression(s)!!.evaluate()
      )
    }
  }

  @Test
  fun `empty literal dictionary test`() {
    assertEquals(
      FimDictionary(linkedMapOf()),
      FimscriptParser.parseExpression("#{}")!!.evaluate()
    )
  }

  @Test
  fun `literal dictionary of simple types test`() {
    val dictString =
      "#{${ZERO_OR_MORE_SPACES}test$ZERO_OR_MORE_SPACES:${ZERO_OR_MORE_SPACES}12$ZERO_OR_MORE_SPACES," +
        "${ZERO_OR_MORE_SPACES}2-1$ZERO_OR_MORE_SPACES:$ZERO_OR_MORE_SPACES'string value'$ZERO_OR_MORE_SPACES}"
    for (s in getTextWithAllSpacesCombinations(dictString)) {
      assertEquals(
        FimDictionary(
          linkedMapOf(
            FimString("test") to FimInt(12),
            FimString("2-1") to FimString("string value")
          )
        ),
        FimscriptParser.parseExpression(s)!!.evaluate()
      )
    }
  }

  @Test
  fun `comma at dictionary end test`() {
    assertEquals(
      FimDictionary(linkedMapOf(FimString("one") to FimInt(1))),
      FimscriptParser.parseExpression("{'one': 1,}")!!.evaluate()
    )
  }

  @Test
  fun `comma at literal dictionary end test`() {
    assertEquals(
      FimDictionary(linkedMapOf(FimString("one") to FimInt(1))),
      FimscriptParser.parseExpression("#{one: 1,}")!!.evaluate()
    )
  }
}
