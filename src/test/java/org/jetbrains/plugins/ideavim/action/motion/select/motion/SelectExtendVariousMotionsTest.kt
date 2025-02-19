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

@file:Suppress("RemoveCurlyBracesFromTemplate")

package org.jetbrains.plugins.ideafim.action.motion.select.motion

import com.flop.idea.fim.api.injector
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 *
 * All dots in these tests are replaced with tabs
 */
class SelectExtendVariousMotionsTest : FimTestCase() {

  @TestWithoutNeofim(SkipNeofimReason.TABS)
  fun `test with tabs`() {
    val code = """
        class Scratch {
        .public static void main(String[] args) {
        ..try {
        ...if ()
        ..}
        .}
        }

        func myFunc() {
        .return anything
        ${c}}
    """.trimIndent().dotToTab()

    configureByText(code)

    typeText(injector.parser.parseKeys("g<C-H>" + "<S-UP>".repeat(2) + "<S-Right>".repeat(2)))

    assertState(
      """
        class Scratch {
        .public static void main(String[] args) {
        ..try {
        ...if ()
        ..}
        .}
        }

        ${s}fu${c}${se}nc myFunc() {
        ${s}${c}${se}.return anything
        ${s}}${c}${se}
      """.trimIndent().dotToTab()
    )

    typeText(injector.parser.parseKeys("<S-UP>".repeat(7) + "<S-Right>".repeat(3)))

    assertState(
      """
        class Scratch {
        ${s}.pu${c}${se}blic static void main(String[] args) {
        ${s}.${c}${se}.try {
        ${s}.${c}${se}..if ()
        ${s}.${c}${se}.}
        ${s}.}${c}${se}
        ${s}}${c}${se}

        ${s}func m${c}${se}yFunc() {
        ${s}.re${c}${se}turn anything
        ${s}}${c}${se}
      """.trimIndent().dotToTab()
    )

    typeText(injector.parser.parseKeys("<S-Right>".repeat(2)))

    assertState(
      """
        class Scratch {
        ${s}.publ${c}${se}ic static void main(String[] args) {
        ${s}..${c}${se}try {
        ${s}..${c}${se}.if ()
        ${s}..${c}${se}}
        ${s}.}${c}${se}
        ${s}}${c}${se}

        ${s}func myF${c}${se}unc() {
        ${s}.retu${c}${se}rn anything
        ${s}}${c}${se}
      """.trimIndent().dotToTab()
    )
  }
}
