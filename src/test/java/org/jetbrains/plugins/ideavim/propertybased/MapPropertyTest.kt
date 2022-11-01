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

import com.flop.idea.fim.api.injector
import org.jetbrains.jetCheck.Generator
import org.jetbrains.jetCheck.ImperativeCommand
import org.jetbrains.jetCheck.PropertyChecker
import org.jetbrains.plugins.ideafim.propertybased.samples.simpleText

// TODO: 25.01.2021 Add neofim test integration
class MapPropertyTest : FimPropertyTestBase() {
  // Disabled, it works too long
  @Suppress("TestFunctionName", "unused")
  fun /*test*/RandomMappings() {
    PropertyChecker.checkScenarios {
      ImperativeCommand { env ->
        val editor = configureByText(simpleText)
        try {
          editor.caretModel.moveToOffset(58) // At the word "lavender"

          val mappingCommands = generateSequence {
            val mode = env.generateValue(Generator.sampledFrom(modesList), null)

            val fromKeys = generateSequence {
              env.generateValue(Generator.sampledFrom(keysPlaying), null)
            }.take(env.generateValue(Generator.integers(1, 10), null)).joinToString(separator = "")

            val toKeys = generateSequence {
              env.generateValue(Generator.sampledFrom(keysPlaying), null)
            }.take(env.generateValue(Generator.integers(1, 10), null)).joinToString(separator = "")

            "$mode $fromKeys $toKeys"
          }.take(env.generateValue(Generator.integers(1, 10), null)).toList()

          val enteringKeys = generateSequence {
            env.generateValue(Generator.sampledFrom(keysPlaying), null)
          }.take(env.generateValue(Generator.integers(1, 10), null)).joinToString(separator = "")

          env.logMessage("Commands:\n${mappingCommands.joinToString("\n")}")
          mappingCommands.forEach { typeText(injector.parser.parseKeys(exCommand(it))) }

          env.logMessage("Enter keys: $enteringKeys")
          typeText(injector.parser.parseKeys(enteringKeys))
        } finally {
          reset(editor)
        }
      }
    }
  }

  fun testEmpty() {
    // Just an empty test
  }
}

// I think, it would be enough to test normal mode only. Not sure if it's true
private val modesList = arrayListOf(
  "map", /*"nmap", "vmap", "xmap", "omap", "imap", "vmap",*/
  "noremap"/*, "nnoremap", "vnoremap", "xnoremap", "onoremap", "inoremap", "vnoremap"*/
)

private val keysPlaying = arrayListOf("h", "j", "k", "l", "w", "b", "e")
