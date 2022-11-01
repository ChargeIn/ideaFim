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

package org.jetbrains.plugins.ideafim.action

import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.experimentalApi
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class GuardedBlocksTest : FimTestCase() {
  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete char with block`() {
    if (!experimentalApi()) return
    configureAndGuard("[123${c}4567890]")
    try {
      typeText(injector.parser.parseKeys("x"))
    } catch (e: Throwable) {
      // Catch exception
      return
    }
    fail()
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line with block`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [1234567890
      ]123${c}4567890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("dd"))
    assertState(
      """
      1234567890
      $c
      1234567890
      """.trimIndent()
    )
  }

/*
  // Probably it's better to put the caret after 1
  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line with block and longer start`() {
    if (!experimentalApi()) return
    configureAndGuard("""
      [1234567890
      1]23${c}4567890[
      1234567890]
      """.trimIndent())
    typeText(injector.parser.parseKeys("dd"))
    assertState("""
      1234567890
      ${c}1
      1234567890
    """.trimIndent())
  }
*/

/*
  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line with block and shorter end`() {
    if (!experimentalApi()) return
    configureAndGuard("""
      [1234567890
      ]123${c}456789[0
      1234567890]
      """.trimIndent())
    typeText(injector.parser.parseKeys("dd"))
    assertState("""
      1234567890
      ${c}0
      1234567890
    """.trimIndent())
  }
*/

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line fully unmodifiable`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [123${c}4567890
      ]123456789[0
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("dd"))
    assertState(
      """
      123${c}4567890
      1234567890
      1234567890
      """.trimIndent()
    )
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line fully unmodifiable end`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [1234567890
      ]123456789[0
      123456${c}7890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("dd"))
    assertState(
      """
      1234567890
      1234567890
      123456${c}7890
      """.trimIndent()
    )
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line with block`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [1234567890
      ]123${c}4567890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("cc"))
    assertState(
      """
      1234567890
      $c
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.INSERT)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line with block1`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [1234567890
      ]123${c}4567890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("O"))
    assertState(
      """
      1234567890
      $c
      1234567890
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.INSERT)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line with block2`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [1234567890
      ]123${c}4567890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("o"))
    assertState(
      """
      1234567890
      1234567890
      $c
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.INSERT)
  }

/*
  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line with block with longer start`() {
    if (!experimentalApi()) return
    configureAndGuard("""
      [1234567890
      1]23${c}4567890[
      1234567890]
      """.trimIndent())
    typeText(injector.parser.parseKeys("cc"))
    assertState("""
      1234567890
      1${c}
      1234567890
    """.trimIndent())
    assertMode(CommandState.Mode.INSERT)
  }
*/

/*
  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line with block with shorter end`() {
    if (!experimentalApi()) return
    configureAndGuard("""
      [1234567890
      ]123${c}456789[0
      1234567890]
      """.trimIndent())
    typeText(injector.parser.parseKeys("cc"))
    assertState("""
      1234567890
      ${c}0
      1234567890
    """.trimIndent())
    assertMode(CommandState.Mode.INSERT)
  }
*/

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line with block at the end`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      [1234567890
      ]12345${c}67890
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("cc"))
    assertState(
      """
      1234567890
      $c
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.INSERT)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line near the guard`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      123456${c}7890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("dd"))
    assertState(
      """
      $c
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line near the guard with line above`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      1234567890
      123456${c}7890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("dd"))
    assertState(
      """
      ${c}1234567890
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test change line near the guard with line above`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      1234567890
      123456${c}7890[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("cc"))
    assertState(
      """
      1234567890
      $c
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.INSERT)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.GUARDED_BLOCKS)
  fun `test delete line near the guard with line above on empty line`() {
    if (!experimentalApi()) return
    configureAndGuard(
      """
      1234567890
      $c[
      1234567890]
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("dd"))
    assertState(
      """
      1234567890
      1234567890
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
  }
}
