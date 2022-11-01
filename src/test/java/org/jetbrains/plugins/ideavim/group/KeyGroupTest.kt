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

package org.jetbrains.plugins.ideafim.group

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.key.MappingOwner
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class KeyGroupTest : FimTestCase() {
  private val owner = MappingOwner.Plugin.get("KeyGroupTest")

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN)
  fun `test remove key mapping`() {
    val keyGroup = com.flop.idea.fim.FimPlugin.getKey()
    val keys = injector.parser.parseKeys("<C-S-B>")

    configureByText("I ${c}found it in a legendary land")
    typeText(keys)
    assertState("I ${c}found it in a legendary land")

    keyGroup.putKeyMapping(MappingMode.N, keys, owner, injector.parser.parseKeys("h"), false)
    typeText(keys)
    assertState("I$c found it in a legendary land")

    keyGroup.removeKeyMapping(owner)
    typeText(keys)
    assertState("I$c found it in a legendary land")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN)
  fun `test remove and add key mapping`() {
    val keyGroup = com.flop.idea.fim.FimPlugin.getKey()
    val keys = injector.parser.parseKeys("<C-S-B>")

    configureByText("I ${c}found it in a legendary land")
    typeText(keys)
    assertState("I ${c}found it in a legendary land")

    keyGroup.putKeyMapping(MappingMode.N, keys, owner, injector.parser.parseKeys("h"), false)
    typeText(keys)
    assertState("I$c found it in a legendary land")

    repeat(10) {
      keyGroup.removeKeyMapping(owner)
      keyGroup.putKeyMapping(MappingMode.N, keys, owner, injector.parser.parseKeys("h"), false)
    }
    typeText(keys)
    assertState("${c}I found it in a legendary land")
  }
}
