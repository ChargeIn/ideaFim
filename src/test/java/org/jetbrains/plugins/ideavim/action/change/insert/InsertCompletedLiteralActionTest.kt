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

package org.jetbrains.plugins.ideafim.action.change.insert

import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

@RunWith(Theories::class)
class InsertCompletedLiteralActionTest : FimTestCase() {

  companion object {
    @JvmStatic
    val octalPrefix = listOf("o", "O")
      @DataPoints("octalPrefix") get

    @JvmStatic
    val shortHexPrefix = listOf("x", "X")
      @DataPoints("shortHexPrefix") get

    @JvmStatic
    val hexPrefix = listOf("u")
      @DataPoints("hexPrefix") get

    @JvmStatic
    val longHexPrefix = listOf("U")
      @DataPoints("longHexPrefix") get

    @JvmStatic
    val insertDigraph = listOf("<C-v>", "<C-q>")
      @DataPoints("insertDigraph") get
  }

  private fun checkInsert(code: String, result: String) {
    for (binding in insertDigraph) {
      configureByText("\n")
      typeText(injector.parser.parseKeys("i$binding$code"))
      assertState("$result\n")
      typeText(injector.parser.parseKeys("<Esc>"))
    }
  }

  // todo parametrized
  // FYI space key after code will be sent via FimPlugin.getMacro().postKey(key, editor);, that's why we ignore the last space in tests
  // fun `octal codes`(@FromDataPoints("octalPrefix") prefix: String) {
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  @Theory
  fun `octal codes`() {
    for (prefix in octalPrefix) {
      checkInsert("$prefix ", " ")
      checkInsert("${prefix}7 ", "${7.toChar()}")
      checkInsert("${prefix}07 ", "${7.toChar()}")
      checkInsert("${prefix}007", "${7.toChar()}")
    }
  }

  // FYI space key after code will be sent via FimPlugin.getMacro().postKey(key, editor);, that's why we ignore the last space in tests
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  @Theory
  fun `decimal codes`() {
    checkInsert("1 ", "${1.toChar()}")
    checkInsert("01 ", "${1.toChar()}")
    checkInsert("001", "${1.toChar()}")
  }

  // FYI space key after code will be sent via FimPlugin.getMacro().postKey(key, editor);, that's why we ignore the last space in tests
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  @Theory
  fun `hex codes`() {
    for (prefix in shortHexPrefix) {
      checkInsert("$prefix ", " ")
      checkInsert("${prefix}B ", "${11.toChar()}")
      checkInsert("${prefix}0B", "${11.toChar()}")
    }

    for (prefix in hexPrefix) {
      checkInsert("$prefix ", " ")
      checkInsert("${prefix}B ", "${11.toChar()}")
      checkInsert("${prefix}0B ", "${11.toChar()}")
      checkInsert("${prefix}00B ", "${11.toChar()}")
      checkInsert("${prefix}000B", "${11.toChar()}")
    }

    for (prefix in longHexPrefix) {
      checkInsert("$prefix ", " ")
      checkInsert("${prefix}B ", "${11.toChar()}")
      checkInsert("${prefix}0B ", "${11.toChar()}")
      checkInsert("${prefix}00B ", "${11.toChar()}")
      checkInsert("${prefix}000B ", "${11.toChar()}")
      checkInsert("${prefix}0000B ", "${11.toChar()}")
      checkInsert("${prefix}00000B ", "${11.toChar()}")
      checkInsert("${prefix}000000B ", "${11.toChar()}")
      checkInsert("${prefix}0000000B", "${11.toChar()}")
    }
  }

  @Theory
  fun `unexpected prefix ending`() {
    checkInsert("o<C-a>", 1.toChar().toString())
    checkInsert("O<C-a>", 1.toChar().toString())
    checkInsert("x<C-a>", 1.toChar().toString())
    checkInsert("X<C-a>", 1.toChar().toString())
    checkInsert("u<C-a>", 1.toChar().toString())
    checkInsert("U<C-a>", 1.toChar().toString())

    checkInsert("o<Esc>", 27.toChar().toString())
    checkInsert("O<Esc>", 27.toChar().toString())
    checkInsert("x<Esc>", 27.toChar().toString())
    checkInsert("X<Esc>", 27.toChar().toString())
    checkInsert("u<Esc>", 27.toChar().toString())
    checkInsert("U<Esc>", 27.toChar().toString())
  }

  @Theory
  fun `special keys`() {
    checkInsert("<Esc>", "${27.toChar()}")
//     checkInsert("<CR>", "${13.toChar()}") exception is thrown because of the \r symbol
  }

  // todo
//  @Theory
//  fun `keycode 10 is not 10 at all`() {
//    checkInsert("010", 0.toChar().toString())
//    checkInsert("o012", 0.toChar().toString())
//    checkInsert("O012", 0.toChar().toString())
//    checkInsert("010", 0.toChar().toString())
//    checkInsert("x0A", 0.toChar().toString())
//    checkInsert("X0A", 0.toChar().toString())
//    checkInsert("u000A", 0.toChar().toString())
//    checkInsert("U0000000A", 0.toChar().toString())
//  }

  @Theory
  fun `regular character`() {
    checkInsert("a", "a")
    checkInsert("A", "A")
    checkInsert("b", "b")
    checkInsert("B", "B")
    checkInsert("c", "c")
    checkInsert("C", "C")
    checkInsert("d", "d")
    checkInsert("D", "D")
    checkInsert("e", "e")
    checkInsert("E", "E")
    checkInsert("f", "f")
    checkInsert("F", "F")
    checkInsert("g", "g")
    checkInsert("G", "G")
    checkInsert("h", "h")
    checkInsert("H", "H")
    checkInsert("i", "i")
    checkInsert("I", "I")
    checkInsert("j", "j")
    checkInsert("J", "J")
    checkInsert("k", "k")
    checkInsert("K", "K")
    checkInsert("l", "l")
    checkInsert("L", "L")
    checkInsert("m", "m")
    checkInsert("M", "M")
    checkInsert("n", "n")
    checkInsert("N", "N")
    checkInsert("p", "p")
    checkInsert("P", "P")
    checkInsert("q", "q")
    checkInsert("Q", "Q")
    checkInsert("r", "r")
    checkInsert("R", "R")
    checkInsert("s", "s")
    checkInsert("S", "S")
    checkInsert("t", "t")
    checkInsert("T", "T")
    checkInsert("v", "v")
    checkInsert("V", "V")
    checkInsert("w", "w")
    checkInsert("W", "W")
    checkInsert("y", "y")
    checkInsert("Y", "Y")
    checkInsert("z", "z")
    checkInsert("Z", "Z")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  @Theory
  fun `control plus character`() {
    checkInsert("<C-a>", 1.toChar().toString())
    checkInsert("<C-b>", 2.toChar().toString())
    checkInsert("<C-c>", 3.toChar().toString())
    checkInsert("<C-d>", 4.toChar().toString())
    checkInsert("<C-e>", 5.toChar().toString())
    checkInsert("<C-f>", 6.toChar().toString())
    checkInsert("<C-g>", 7.toChar().toString())
    checkInsert("<C-h>", 8.toChar().toString())
    checkInsert("<C-i>", "\t")
//    checkInsert("<C-j>", 0.toChar().toString()) can't be inserted to IDE for some reason
    checkInsert("<C-k>", 11.toChar().toString())
    checkInsert("<C-l>", 12.toChar().toString())
//    checkInsert("<C-m>", 13.toChar().toString()) can't be inserted to IDE for some reason
    checkInsert("<C-n>", 14.toChar().toString())
    checkInsert("<C-o>", 15.toChar().toString())
    checkInsert("<C-p>", 16.toChar().toString())
    checkInsert("<C-q>", 17.toChar().toString())
    checkInsert("<C-r>", 18.toChar().toString())
    checkInsert("<C-s>", 19.toChar().toString())
    checkInsert("<C-t>", 20.toChar().toString())
    checkInsert("<C-u>", 21.toChar().toString())
    checkInsert("<C-v>", 22.toChar().toString())
    checkInsert("<C-w>", 23.toChar().toString())
    checkInsert("<C-x>", 24.toChar().toString())
    checkInsert("<C-y>", 25.toChar().toString())
    checkInsert("<C-z>", 26.toChar().toString())
    checkInsert("<C-[>", 27.toChar().toString())
    checkInsert("<C-\\>", 28.toChar().toString())
    checkInsert("<C-]>", 29.toChar().toString())
    checkInsert("<C-^>", 30.toChar().toString())
    checkInsert("<C-_>", 31.toChar().toString())
  }
}
