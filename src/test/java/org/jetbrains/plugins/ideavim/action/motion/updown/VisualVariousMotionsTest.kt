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

package org.jetbrains.plugins.ideafim.action.motion.updown

import com.flop.idea.fim.api.injector
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author Alex Plate
 */
class VisualVariousMotionsTest : FimTestCase() {

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

    typeText(injector.parser.parseKeys("<C-V>" + "k".repeat(2) + "l".repeat(2)))

    assertState(
      """
        class Scratch {
        .public static void main(String[] args) {
        ..try {
        ...if ()
        ..}
        .}
        }

        ${s}fu${c}n${se}c myFunc() {
        ${s}${c}${se}.return anything
        ${s}${c}}${se}
      """.trimIndent().dotToTab()
    )

    typeText(injector.parser.parseKeys("k".repeat(7) + "l".repeat(3)))

    // Carets 2-4 have 0 column as logical position, but ${se} - 1 column as visual position
    assertState(
      """
        class Scratch {
        ${s}.pu${c}b${se}lic static void main(String[] args) {
        ${s}${c}.${se}.try {
        ${s}${c}.${se}..if ()
        ${s}${c}.${se}.}
        ${s}.${c}}${se}
        ${s}${c}}${se}

        ${s}func m${c}y${se}Func() {
        ${s}.re${c}t${se}urn anything
        ${s}${c}}${se}
      """.trimIndent().dotToTab()
    )

    TestCase.assertEquals(3, myFixture.editor.caretModel.allCarets[1].visualPosition.column)
    TestCase.assertEquals(3, myFixture.editor.caretModel.allCarets[2].visualPosition.column)
    TestCase.assertEquals(3, myFixture.editor.caretModel.allCarets[3].visualPosition.column)

    typeText(injector.parser.parseKeys("l".repeat(2)))

    assertState(
      """
        class Scratch {
        ${s}.publ${c}i${se}c static void main(String[] args) {
        ${s}..${c}t${se}ry {
        ${s}.${c}.${se}.if ()
        ${s}..${c}}${se}
        ${s}.${c}}${se}
        ${s}${c}}${se}

        ${s}func myF${c}u${se}nc() {
        ${s}.retu${c}r${se}n anything
        ${s}${c}}${se}
      """.trimIndent().dotToTab()
    )
    TestCase.assertEquals(7, myFixture.editor.caretModel.allCarets[2].visualPosition.column)
  }
}
