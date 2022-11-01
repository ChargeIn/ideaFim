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

package org.jetbrains.plugins.ideafim.ex.implementation.commands

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.key.ShortcutOwner
import com.flop.idea.fim.key.ShortcutOwnerInfo
import com.flop.idea.fim.fimscript.model.commands.SetHandlerCommand
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class SetHandlerCommandTest : FimTestCase() {
  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test parse a mappings`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "a:ide")!!
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test i mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "i:ide")!!
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test n mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "n:ide")!!
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test n-v mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "n-v:ide")!!
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test v mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "v:ide")!!
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test x mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "x:ide")!!
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test s mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "s:ide")!!
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.VIM, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test i-n-v mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "i-n-v:ide")!!
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.insert)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.normal)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.select)
    TestCase.assertEquals(ShortcutOwner.IDE, newOwner.visual)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test broken mapping`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "l:ide")
    TestCase.assertNull(newOwner)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test broken mapping 2`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    val newOwner = SetHandlerCommand.updateOwner(owner, "hau2h2:ide")
    TestCase.assertNull(newOwner)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test to notation`() {
    var owner = ShortcutOwnerInfo.allPerModeFim
    owner = owner.copy(normal = ShortcutOwner.IDE)
    TestCase.assertEquals("n:ide i-v:fim", owner.toNotation())
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test to notation 2`() {
    var owner = ShortcutOwnerInfo.allPerModeFim
    owner = owner.copy(normal = ShortcutOwner.IDE, insert = ShortcutOwner.IDE)
    TestCase.assertEquals("n-i:ide v:fim", owner.toNotation())
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test to notation 3`() {
    val owner = ShortcutOwnerInfo.allPerModeFim
    TestCase.assertEquals("a:fim", owner.toNotation())
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test to notation 4`() {
    var owner = ShortcutOwnerInfo.allPerModeFim
    owner = owner.copy(
      normal = ShortcutOwner.IDE,
      insert = ShortcutOwner.IDE,
      visual = ShortcutOwner.IDE,
      select = ShortcutOwner.IDE
    )
    TestCase.assertEquals("a:ide", owner.toNotation())
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test executing command`() {
    configureByText("")
    TestCase.assertTrue(com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.isEmpty())
    typeText(commandToKeys("sethandler <C-C> a:ide"))
    TestCase.assertTrue(com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.isNotEmpty())
    val key = com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.entries.single().key
    val owner = com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.entries.single().value
    TestCase.assertEquals("<C-C>", injector.parser.toKeyNotation(key))
    assertEquals(ShortcutOwnerInfo.allPerModeIde, owner)
  }

  @TestWithoutNeofim(SkipNeofimReason.NOT_VIM_TESTING)
  fun `test executing command 1`() {
    configureByText("")
    TestCase.assertTrue(com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.isEmpty())
    typeText(commandToKeys("sethandler <C-C> a:ide "))
    TestCase.assertTrue(com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.isNotEmpty())
    val key = com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.entries.single().key
    val owner = com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.entries.single().value
    TestCase.assertEquals("<C-C>", injector.parser.toKeyNotation(key))
    assertEquals(ShortcutOwnerInfo.allPerModeIde, owner)
  }
}
