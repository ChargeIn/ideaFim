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

import com.flop.idea.fim.api.injector
import org.junit.Test
import java.awt.event.InputEvent.CTRL_DOWN_MASK
import javax.swing.KeyStroke.getKeyStroke
import kotlin.test.assertEquals

class DoubleQuotedStringTest {

  @Test
  fun `test three-digit octal number`() {
    assertEquals(injector.parser.parseFimScriptString("\\162"), "r")
    assertEquals(injector.parser.parseFimScriptString("\\162eturn"), "return")
    assertEquals(injector.parser.parseFimScriptString("t\\162ee"), "tree")
  }

  @Test
  fun `test two-digit octal number`() {
    assertEquals(injector.parser.parseFimScriptString("\\77"), "?")
    assertEquals(injector.parser.parseFimScriptString("\\77!"), "?!")
    assertEquals(injector.parser.parseFimScriptString("\\779"), "?9")
    assertEquals(injector.parser.parseFimScriptString("hi\\77"), "hi?")
  }

  @Test
  fun `test one-digit octal number`() {
    assertEquals(injector.parser.parseFimScriptString("\\7"), "" + 7.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\7!"), 7.toChar() + "!")
    assertEquals(injector.parser.parseFimScriptString("\\79"), 7.toChar() + "9")
    assertEquals(injector.parser.parseFimScriptString("hi\\7"), "hi" + 7.toChar())
  }

  @Test
  fun `test byte specified with two hex numbers`() {
    assertEquals(injector.parser.parseFimScriptString("\\x"), "x")
    assertEquals(injector.parser.parseFimScriptString("\\x1f"), "" + 31.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\x1fz"), 31.toChar() + "z")
    assertEquals(injector.parser.parseFimScriptString("word\\x1f"), "word" + 31.toChar())

    assertEquals(injector.parser.parseFimScriptString("\\X"), "X")
    assertEquals(injector.parser.parseFimScriptString("\\X1f"), "" + 31.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\X1fz"), 31.toChar() + "z")
    assertEquals(injector.parser.parseFimScriptString("word\\X1f"), "word" + 31.toChar())
  }

  @Test
  fun `test byte specified with one hex number`() {
    assertEquals(injector.parser.parseFimScriptString("\\x"), "x")
    assertEquals(injector.parser.parseFimScriptString("\\xf"), "" + 15.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\xfz"), 15.toChar() + "z")
    assertEquals(injector.parser.parseFimScriptString("word\\xf"), "word" + 15.toChar())

    assertEquals(injector.parser.parseFimScriptString("\\X"), "X")
    assertEquals(injector.parser.parseFimScriptString("\\Xf"), "" + 15.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\Xfz"), 15.toChar() + "z")
    assertEquals(injector.parser.parseFimScriptString("word\\Xf"), "word" + 15.toChar())
  }

  @Test
  fun `test up to 4 hex numbers`() {
    assertEquals(injector.parser.parseFimScriptString("\\u"), "u")
    assertEquals(injector.parser.parseFimScriptString("\\u7"), "" + 7.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\u72"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\u072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\u0072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\u00072"), 7.toChar() + "2")
  }

  @Test
  fun `test up to 8 hex numbers`() {
    assertEquals(injector.parser.parseFimScriptString("\\U"), "U")
    assertEquals(injector.parser.parseFimScriptString("\\U7"), "" + 7.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U72"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U0072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U00072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U000072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U0000072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U00000072"), "" + 114.toChar())
    assertEquals(injector.parser.parseFimScriptString("\\U000000072"), 7.toChar() + "2")
  }

  @Test
  fun `test escaped chars`() {
    assertEquals(injector.parser.parseFimScriptString("\\r"), "\r")
    assertEquals(injector.parser.parseFimScriptString("\\n"), "\n")
    assertEquals(injector.parser.parseFimScriptString("\\f"), 12.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\e"), 27.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\t"), "\t")
    assertEquals(injector.parser.parseFimScriptString("\\\\"), "\\")
    assertEquals(injector.parser.parseFimScriptString("\\\""), "\"")
    assertEquals(injector.parser.parseFimScriptString("\\b"), 8.toChar().toString())
  }

  @Test
  fun `test invalid char escaped`() {
    assertEquals(injector.parser.parseFimScriptString("oh \\hi Mark"), "oh hi Mark")
  }

  @Test
  fun `test force end of the string`() {
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\0blabla"), "oh hi Mark")
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\x0 blabla"), "oh hi Mark")
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\x00 blabla"), "oh hi Mark")
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\X0 blabla"), "oh hi Mark")
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\X00 blabla"), "oh hi Mark")
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\u0 blabla"), "oh hi Mark")
    assertEquals(injector.parser.parseFimScriptString("oh hi Mark\\U00 blabla"), "oh hi Mark")
  }

  @Test
  fun `test multiple escaped chars in a row`() {
    assertEquals(injector.parser.parseFimScriptString("\\162\\u72\\U72\\x72\\X72\\n"), "rrrrr\n")
  }

  @Test
  fun `test special keys`() {
    assertEquals(injector.parser.parseFimScriptString("\\<Esc>"), 27.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<Esk>"), "<Esk>")
    assertEquals(injector.parser.parseFimScriptString("l\\<Esc>l"), "l" + 27.toChar() + "l")
    assertEquals(injector.parser.parseFimScriptString("\\<Space>"), " ")
  }

  @Test
  fun `test ctrl-char`() {
    assertEquals(injector.parser.parseFimScriptString("\\<C-A>"), 1.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-B>"), 2.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-C>"), 3.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-D>"), 4.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-E>"), 5.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-F>"), 6.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-G>"), 7.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-H>"), 8.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-I>"), 9.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-J>"), 0.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-K>"), 11.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-L>"), 12.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-M>"), 13.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-N>"), 14.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-O>"), 15.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-P>"), 16.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-Q>"), 17.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-R>"), 18.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-S>"), 19.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-T>"), 20.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-U>"), 21.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-V>"), 22.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-W>"), 23.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-X>"), 24.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-Y>"), 25.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-Z>"), 26.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-[>"), 27.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-\\>"), 28.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-]>"), 29.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-^>"), 30.toChar().toString())
    assertEquals(injector.parser.parseFimScriptString("\\<C-_>"), 31.toChar().toString())
  }

  @Test
  fun `test 1-31 keycodes`() {
    assertEquals(listOf(getKeyStroke(65, CTRL_DOWN_MASK)), injector.parser.stringToKeys(1.toChar().toString()))
    assertEquals(listOf(getKeyStroke(66, CTRL_DOWN_MASK)), injector.parser.stringToKeys(2.toChar().toString()))
    assertEquals(listOf(getKeyStroke(67, CTRL_DOWN_MASK)), injector.parser.stringToKeys(3.toChar().toString()))
    assertEquals(listOf(getKeyStroke(68, CTRL_DOWN_MASK)), injector.parser.stringToKeys(4.toChar().toString()))
    assertEquals(listOf(getKeyStroke(69, CTRL_DOWN_MASK)), injector.parser.stringToKeys(5.toChar().toString()))
    assertEquals(listOf(getKeyStroke(70, CTRL_DOWN_MASK)), injector.parser.stringToKeys(6.toChar().toString()))
    assertEquals(listOf(getKeyStroke(71, CTRL_DOWN_MASK)), injector.parser.stringToKeys(7.toChar().toString()))
    assertEquals(listOf(getKeyStroke(72, CTRL_DOWN_MASK)), injector.parser.stringToKeys(8.toChar().toString()))
    assertEquals(listOf(getKeyStroke('\t')), injector.parser.stringToKeys(9.toChar().toString()))
    assertEquals(listOf(getKeyStroke(74, CTRL_DOWN_MASK)), injector.parser.stringToKeys(0.toChar().toString()))
    assertEquals(listOf(getKeyStroke(75, CTRL_DOWN_MASK)), injector.parser.stringToKeys(11.toChar().toString()))
    assertEquals(listOf(getKeyStroke(76, CTRL_DOWN_MASK)), injector.parser.stringToKeys(12.toChar().toString()))
    assertEquals(listOf(getKeyStroke(77, CTRL_DOWN_MASK)), injector.parser.stringToKeys(13.toChar().toString()))
    assertEquals(listOf(getKeyStroke(78, CTRL_DOWN_MASK)), injector.parser.stringToKeys(14.toChar().toString()))
    assertEquals(listOf(getKeyStroke(79, CTRL_DOWN_MASK)), injector.parser.stringToKeys(15.toChar().toString()))
    assertEquals(listOf(getKeyStroke(80, CTRL_DOWN_MASK)), injector.parser.stringToKeys(16.toChar().toString()))
    assertEquals(listOf(getKeyStroke(81, CTRL_DOWN_MASK)), injector.parser.stringToKeys(17.toChar().toString()))
    assertEquals(listOf(getKeyStroke(82, CTRL_DOWN_MASK)), injector.parser.stringToKeys(18.toChar().toString()))
    assertEquals(listOf(getKeyStroke(83, CTRL_DOWN_MASK)), injector.parser.stringToKeys(19.toChar().toString()))
    assertEquals(listOf(getKeyStroke(84, CTRL_DOWN_MASK)), injector.parser.stringToKeys(20.toChar().toString()))
    assertEquals(listOf(getKeyStroke(85, CTRL_DOWN_MASK)), injector.parser.stringToKeys(21.toChar().toString()))
    assertEquals(listOf(getKeyStroke(86, CTRL_DOWN_MASK)), injector.parser.stringToKeys(22.toChar().toString()))
    assertEquals(listOf(getKeyStroke(87, CTRL_DOWN_MASK)), injector.parser.stringToKeys(23.toChar().toString()))
    assertEquals(listOf(getKeyStroke(88, CTRL_DOWN_MASK)), injector.parser.stringToKeys(24.toChar().toString()))
    assertEquals(listOf(getKeyStroke(89, CTRL_DOWN_MASK)), injector.parser.stringToKeys(25.toChar().toString()))
    assertEquals(listOf(getKeyStroke(90, CTRL_DOWN_MASK)), injector.parser.stringToKeys(26.toChar().toString()))
    assertEquals(listOf(getKeyStroke(91, CTRL_DOWN_MASK)), injector.parser.stringToKeys(27.toChar().toString()))
    assertEquals(listOf(getKeyStroke(92, CTRL_DOWN_MASK)), injector.parser.stringToKeys(28.toChar().toString()))
    assertEquals(listOf(getKeyStroke(93, CTRL_DOWN_MASK)), injector.parser.stringToKeys(29.toChar().toString()))
    assertEquals(listOf(getKeyStroke(94, CTRL_DOWN_MASK)), injector.parser.stringToKeys(30.toChar().toString()))
    assertEquals(listOf(getKeyStroke(95, CTRL_DOWN_MASK)), injector.parser.stringToKeys(31.toChar().toString()))
  }
}
