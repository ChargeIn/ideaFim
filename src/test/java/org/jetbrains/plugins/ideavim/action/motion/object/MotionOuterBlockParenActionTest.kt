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

package org.jetbrains.plugins.ideafim.action.motion.`object`

import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.FimTestCase

class MotionOuterBlockParenActionTest : FimTestCase() {
  // VIM-1633 |v_a)|
  fun `test single letter with single parentheses`() {
    configureByText("(${c}a)")
    typeText(injector.parser.parseKeys("va)"))
    assertSelection("(a)")
  }

  fun `test single letter with double parentheses`() {
    configureByText("((${c}a))")
    typeText(injector.parser.parseKeys("va)"))
    assertSelection("(a)")
  }

  fun `test multiline outside parentheses`() {
    configureByText(
      """(outer
                      |$c(inner))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("va)"))
    assertSelection("(inner)")
  }

  fun `test multiline in parentheses`() {
    configureByText(
      """(outer
                      |(inner$c))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("va)"))
    assertSelection("(inner)")
  }

  fun `test multiline inside of outer parentheses`() {
    configureByText(
      """(outer
                     |$c (inner))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("va)"))
    assertSelection(
      """(outer
                        | (inner))""".trimMargin()
    )
  }

  fun `test double motion`() {
    configureByText(
      """(outer
                      |$c(inner))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("va)a)"))
    assertSelection(
      """(outer
                      |(inner))""".trimMargin()
    )
  }

  fun `test motion with count`() {
    configureByText(
      """(outer
                      |$c(inner))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("v2a)"))
    assertSelection(
      """(outer
                      |(inner))""".trimMargin()
    )
  }

  fun `test text object after motion`() {
    configureByText(
      """(outer
                      |$c(inner))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("vlla)"))
    assertSelection(
      """(outer
                      |(inner))""".trimMargin()
    )
  }

  fun `test text object after motion outside parentheses`() {
    configureByText(
      """(outer
                      |(inner$c))""".trimMargin()
    )
    typeText(injector.parser.parseKeys("vlla)"))
    assertSelection("(inner)")
  }

  // |d| |v_ab|
  fun testDeleteOuterBlock() {
    typeTextInFile(
      injector.parser.parseKeys("da)"),
      "foo(b${c}ar, baz);\n"
    )
    assertState("foo;\n")
  }
}
