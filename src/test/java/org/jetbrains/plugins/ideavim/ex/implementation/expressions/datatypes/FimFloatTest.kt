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

package org.jetbrains.plugins.ideafim.ex.implementation.expressions.datatypes

import com.flop.idea.fim.fimscript.model.datatypes.FimFloat
import org.junit.Test
import kotlin.test.assertEquals

class FimFloatTest {

  @Test
  fun `round 6 digits`() {
    assertEquals("0.999999", FimFloat(0.999999).toString())
  }

  @Test
  fun `round 7 digits`() {
    assertEquals("1.0", FimFloat(0.9999999).toString())
  }
}
