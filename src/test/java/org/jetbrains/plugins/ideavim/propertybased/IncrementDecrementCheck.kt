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

package org.jetbrains.plugins.ideafim.propertybased

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.PlatformTestUtil
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import org.jetbrains.jetCheck.Generator
import org.jetbrains.jetCheck.ImperativeCommand
import org.jetbrains.jetCheck.PropertyChecker
import org.jetbrains.plugins.ideafim.NeoVimTesting
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase
import kotlin.math.absoluteValue
import kotlin.math.sign

class IncrementDecrementTest : FimPropertyTestBase() {
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testPlayingWithNumbers() {
    PropertyChecker.checkScenarios {
      ImperativeCommand { env ->
        val editor = configureByText(numbers)
        try {
          moveCaretToRandomPlace(env, editor)
          env.executeCommands(Generator.sampledFrom(IncrementDecrementActions(editor, this)))
        } finally {
          reset(editor)
        }
      }
    }
  }

  fun testPlayingWithNumbersGenerateNumber() {
    setupChecks {
      this.neoFim.ignoredRegisters = setOf(':')
    }
    com.flop.idea.fim.FimPlugin.getOptionService().appendValue(OptionScope.GLOBAL, OptionConstants.nrformatsName, "octal", OptionConstants.nrformatsName)
    PropertyChecker.checkScenarios {
      ImperativeCommand { env ->
        val number = env.generateValue(testNumberGenerator, "Generate %s number")
        val editor = configureByText(number)
        try {
          moveCaretToRandomPlace(env, editor)

          NeoVimTesting.setupEditor(editor, this)
          NeoVimTesting.typeCommand(":set nrformats+=octal<CR>", this, editor)

          env.executeCommands(Generator.sampledFrom(IncrementDecrementActions(editor, this)))

          NeoVimTesting.assertState(editor, this)
        } finally {
          reset(editor)
        }
      }
    }
  }
}

private class IncrementDecrementActions(private val editor: Editor, val test: FimTestCase) : ImperativeCommand {
  override fun performCommand(env: ImperativeCommand.Environment) {
    val generator = Generator.sampledFrom("<C-A>", "<C-X>")
    val key = env.generateValue(generator, null)
    val action = injector.parser.parseKeys(key).single()
    env.logMessage("Use command: ${injector.parser.toKeyNotation(action)}.")
    FimTestCase.typeText(listOf(action), editor, editor.project)
    NeoVimTesting.typeCommand(key, test, editor)

    IdeEventQueue.getInstance().flushQueue()
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
  }
}

val differentFormNumberGenerator = Generator.from { env ->
  val form = env.generate(Generator.sampledFrom(/*2,*/ 8, 10, 16))
  env.generate(
    Generator.integers().suchThat { it != Int.MIN_VALUE }.map {
      val sign = it.sign
      val stringNumber = it.absoluteValue.toString(form)
      if (sign < 0) "-$stringNumber" else stringNumber
    }
  )
}

val brokenNumberGenerator = Generator.from { env ->
  val bigChar = env.generate(Generator.anyOf(Generator.charsInRange('8', '9'), Generator.charsInRange('G', 'Z')))
  val number = env.generate(differentFormNumberGenerator)
  if (number.length > 4) {
    val insertAt = env.generate(Generator.integers(4, number.length - 1))
    number.take(insertAt) + bigChar + number.substring(insertAt)
  } else "$number$bigChar"
}

val testNumberGenerator = Generator.from { env ->
  env.generate(
    Generator.frequency(
      10, differentFormNumberGenerator,
      1, brokenNumberGenerator
    )
  )
}
