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

package org.jetbrains.plugins.ideafim.extension.exchange

import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.extension.exchange.FimExchangeExtension
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.FimTestCase

class FimExchangeExtensionTest : FimTestCase() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    enableExtensions("exchange")
  }

  // |cx|
  fun `test exchange words left to right`() {
    doTest(
      listOf("cxe", "w", "cxe"),
      "The quick ${c}brown fox catch over the lazy dog",
      "The quick fox ${c}brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |cx|
  fun `test exchange words dot repeat`() {
    doTest(
      listOf("cxiw", "w", "."),
      "The quick ${c}brown fox catch over the lazy dog",
      "The quick fox ${c}brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |cx|
  fun `test exchange words right to left`() {
    doTest(
      listOf("cxe", "b", "cxe"),
      "The quick brown ${c}fox catch over the lazy dog",
      "The quick ${c}fox brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |cx|
  fun `test exchange words right to left with dot`() {
    doTest(
      listOf("cxe", "b", "."),
      "The quick brown ${c}fox catch over the lazy dog",
      "The quick ${c}fox brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |X|
  fun `test visual exchange words left to right`() {
    doTest(
      listOf("veX", "w", "veX"),
      "The quick ${c}brown fox catch over the lazy dog",
      "The quick fox ${c}brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |X|
  @FimBehaviorDiffers(
    originalFimAfter = "The ${c}brown catch over the lazy dog",
    shouldBeFixed = true
  )
  fun `test visual exchange words from inside`() {
    doTest(
      listOf("veX", "b", "v3e", "X"),
      "The quick ${c}brown fox catch over the lazy dog",
      "The brow${c}n catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |X|
  @FimBehaviorDiffers(
    originalFimAfter = "The brown ${c}catch over the lazy dog",
    shouldBeFixed = true
  )
  fun `test visual exchange words from outside`() {
    doTest(
      listOf("v3e", "X", "w", "veX"),
      "The ${c}quick brown fox catch over the lazy dog",
      "The brow${c}n catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |cxx|
  @FimBehaviorDiffers(
    originalFimAfter =
    """The quick
       catch over
       ${c}brown fox
       the lazy dog
       """,
    shouldBeFixed = true
  )
  fun `test exchange lines top down`() {
    doTest(
      listOf("cxx", "j", "cxx"),
      """The quick
         brown ${c}fox
         catch over
         the lazy dog
      """.trimIndent(),
      """The quick
         ${c}catch over
         brown fox
         the lazy dog
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |cxx|
  @FimBehaviorDiffers(
    originalFimAfter =
    """The quick
       catch over
       ${c}brown fox
       the lazy dog
       """,
    shouldBeFixed = true
  )
  fun `test exchange lines top down with dot`() {
    doTest(
      listOf("cxx", "j", "."),
      """The quick
         brown ${c}fox
         catch over
         the lazy dog
      """.trimIndent(),
      """The quick
         ${c}catch over
         brown fox
         the lazy dog
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
         The quick
         brown thecatch over
         fox
          lazy dog
    """
  )
  fun `test exchange to the line end`() {
    doTest(
      listOf("v$", "X", "jj^ve", "X"),
      """The quick
         brown ${c}fox
         catch over
         the lazy dog
      """.trimIndent(),
      """The quick
         brown the
         catch over
         fox lazy dog
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  @FimBehaviorDiffers(
    originalFimAfter =
    """
         catch over
         the lazy dog
         ${c}The quick
         brown fox
      """,
    shouldBeFixed = true
  )
  fun `test exchange visual lines`() {
    doTest(
      listOf("Vj", "X", "jj", "Vj", "X"),
      """
         The ${c}quick
         brown fox
         catch over
         the lazy dog
      """.trimIndent(),
      """
         ${c}catch over
         the lazy dog
         The quick
         brown fox
         
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test visual char highlighter`() {
    val before = """
         The ${c}quick
         brown fox
         catch over
         the lazy dog
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("vlll" + "X"))

    assertHighlighter(4, 8, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test visual line highdhitligthhter`() {
    val before = """
         The ${c}quick
         brown fox
         catch over
         the lazy dog
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("Vj" + "X"))

    assertHighlighter(4, 15, HighlighterTargetArea.LINES_IN_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test till the line end highlighter`() {
    val before = """
         The ${c}quick
         brown fox
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("v$" + "X"))

    assertHighlighter(4, 10, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test pre line end highlighter`() {
    val before = """
         The ${c}quick
         brown fox
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("v\$h" + "X"))

    assertHighlighter(4, 9, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test pre pre line end highlighter`() {
    val before = """
         The ${c}quick
         brown fox
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("v\$hh" + "X"))

    assertHighlighter(4, 8, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test to file end highlighter`() {
    val before = """
         The quick
         brown ${c}fox
    """.trimIndent()
    configureByText(before)
    typeText(
      injector.parser.parseKeys(
        buildString {
          append("v\$")
          append("X")
        }
      )
    )

    assertHighlighter(16, 19, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test to file end with new line highlighter`() {
    val before = """
         The quick
         brown ${c}fox
         
    """.trimIndent()
    configureByText(before)
    typeText(
      injector.parser.parseKeys(
        buildString {
          append("v\$")
          append("X")
        }
      )
    )

    assertHighlighter(16, 20, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test back selection`() {
    val before = """
         The quick
         brown ${c}fox
         
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("vb" + "X"))

    assertHighlighter(10, 17, HighlighterTargetArea.EXACT_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test back selection exchange 1`() {
    doTest(
      listOf("vb", "X", "bevb", "X"),
      "The quick brow${c}n fox catch over the lazy dog",
      "The ${c}brown quick fox catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test back selection exchange 2`() {
    doTest(
      listOf("vb", "X", "wve", "X"),
      "The quick brow${c}n fox catch over the lazy dog",
      "The quick fox ${c}brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test back selection exchange 3`() {
    doTest(
      listOf("ve", "X", "wevb", "X"),
      "The quick ${c}brown fox catch over the lazy dog",
      "The quick fox ${c}brown catch over the lazy dog",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test change with down motion`() {
    val before = """
         The ${c}quick
         brown fox
         catch over
         the lazy dog
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("cxj"))

    // Note that this is the range of the motion. The implementation will select the text with linewise 'V', so the
    // correct text is yanked. The LINES_IN_RANGE means the highlight is drawn across the whole line
    // so the correct text is
    assertHighlighter(4, 14, HighlighterTargetArea.LINES_IN_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  fun `test cxx`() {
    val before = """
         The ${c}quick
         brown fox
         catch over
         the lazy dog
    """.trimIndent()
    configureByText(before)
    typeText(injector.parser.parseKeys("cxx"))

    // Note that this is the range of the motion. The implementation will select the text with linewise 'V', so the
    // correct text is yanked. The LINES_IN_RANGE means the highlight is drawn across the whole line
    assertHighlighter(0, 4, HighlighterTargetArea.LINES_IN_RANGE)

    // Exit fim-exchange
    exitExchange()
  }

  private fun exitExchange() {
    typeText(injector.parser.parseKeys("cxc"))
  }

  private fun assertHighlighter(start: Int, end: Int, area: HighlighterTargetArea) {
    val currentExchange = myFixture.editor.getUserData(FimExchangeExtension.EXCHANGE_KEY)!!
    val highlighter = currentExchange.getHighlighter()!!
    assertEquals(start, highlighter.startOffset)
    assertEquals(end, highlighter.endOffset)
    assertEquals(area, highlighter.targetArea)
  }
}
