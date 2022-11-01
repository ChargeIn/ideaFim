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

package org.jetbrains.plugins.ideafim.ui

import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.ui.ShowCmd
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class ShowCmdTest : FimTestCase() {
  override fun setUp() {
    super.setUp()
    val before = "${c}I found it in a legendary land"
    configureByText(before)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd on by default`() {
    com.flop.idea.fim.FimPlugin.getOptionService().resetAllOptions()
    assertTrue(com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.showcmdName))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows nothing if disabled`() {
    com.flop.idea.fim.FimPlugin.getOptionService().unsetOption(OptionScope.GLOBAL, OptionConstants.showcmdName)

    typeText(injector.parser.parseKeys("3"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd count`() {
    typeText(injector.parser.parseKeys("3"))
    assertEquals("3", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd multiple count`() {
    typeText(injector.parser.parseKeys("320"))
    assertEquals("320", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd incomplete command`() {
    typeText(injector.parser.parseKeys("3d2"))
    assertEquals("3d2", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears on completed command`() {
    typeText(injector.parser.parseKeys("3d2w"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears on Escape`() {
    typeText(injector.parser.parseKeys("3d2<Esc>"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd expands mapped keys`() {
    enterCommand("nmap rrrr d")
    typeText(injector.parser.parseKeys("32rrrr"))
    assertEquals("32d", getShowCmdText())
  }

  // TODO: This test fails because IdeaFim's mapping handler doesn't correctly expand unhandled keys on timeout
//  fun `test showcmd expands ambiguous mapped keys on timeout`() {
  // `rrr` should timeout and replay `rr` which is mapped to `42`
//    enterCommand("nmap rr 42")
//    enterCommand("nmap rrr 55")
//    typeText(injector.parser.parseKeys("12rr"))
//    waitAndAssert { "1242" == getShowCmdText() }
//  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd updates count when expanding mapped keys`() {
    enterCommand("nmap rrrr 55d")
    typeText(injector.parser.parseKeys("32rrrr"))
    assertEquals("3255d", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd removes count on Delete`() {
    typeText(injector.parser.parseKeys("32<Del>"))
    assertEquals("3", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears if Delete all count chars`() {
    typeText(injector.parser.parseKeys("32<Del><Del>"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd removes motion count on Delete`() {
    typeText(injector.parser.parseKeys("32d44<Del><Del>"))
    assertEquals("32d", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears if Delete on operator`() {
    typeText(injector.parser.parseKeys("32d<Del>"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows nothing in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "hello world"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows digraph entry in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-K>O"))
    assertEquals("^KO", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears when cancelling digraph entry in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-K>O" + "<Esc>"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows literal entry in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-V>12"))
    assertEquals("^V12", getShowCmdText())
  }

  // Fim seems to hard code <C-Q> and swaps it for <C-V>
  @FimBehaviorDiffers("^V12")
  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows literal entry with CTRL-Q in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-Q>12"))
    assertEquals("^Q12", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears when cancelling literal entry in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-V>1" + "<Esc>"))
    assertEquals("", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows register entry in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-R>"))
    assertEquals("^R", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd clears when cancelling registry entry in insert mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-R>" + "<Esc>"))
    assertEquals("", getShowCmdText())
  }

  // Note that Fim shows the number of lines, or rows x cols for visual mode. We don't because IntelliJ already
  // shows this kind of information in the position panel
  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd works in visual mode`() {
    typeText(injector.parser.parseKeys("v" + "32f"))
    assertEquals("32f", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd works in single command mode`() {
    typeText(injector.parser.parseKeys("i" + "<C-O>" + "32f"))
    assertEquals("32f", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd only shows last 10 characters of buffer`() {
    typeText(injector.parser.parseKeys("12345678900987654321"))
    assertEquals("0987654321", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd tooltip shows full buffer`() {
    typeText(injector.parser.parseKeys("12345678900987654321"))
    assertEquals("12345678900987654321", getShowCmdTooltipText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows select register command`() {
    typeText(injector.parser.parseKeys("\"a32d"))
    assertEquals("\"a32d", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows count and select register command`() {
    typeText(injector.parser.parseKeys("32\"ad"))
    assertEquals("32\"ad", getShowCmdText())
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.SHOW_CMD)
  fun `test showcmd shows repeated select register with counts`() {
    typeText(injector.parser.parseKeys("22\"a22\"a22\"a22\"a22d22"))
    assertEquals("a22\"a22d22", getShowCmdText())
    assertEquals("22\"a22\"a22\"a22\"a22d22", getShowCmdTooltipText())
  }

  private fun getShowCmdText() = ShowCmd.getWidgetText(myFixture.editor!!)

  private fun getShowCmdTooltipText() = ShowCmd.getFullText(myFixture.editor!!)
}
