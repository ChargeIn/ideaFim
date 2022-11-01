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

package org.jetbrains.plugins.ideafim

import com.flop.idea.fim.RegisterActions.VIM_ACTIONS_EP
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.handler.ActionBeanClass
import com.flop.idea.fim.key.CommandNode
import com.flop.idea.fim.key.CommandPartNode
import junit.framework.TestCase
import javax.swing.KeyStroke

class RegisterActionsTest : FimTestCase() {
  fun `test simple action`() {
    val before = "I ${c}found it in a legendary land"
    val after = "I f${c}ound it in a legendary land"
    doTest("l", before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.EDITOR_MODIFICATION)
  fun `test action in disabled plugin`() {
    try {
      setupChecks {
        caretShape = false
      }
      val before = "I ${c}found it in a legendary land"
      val after = "I jklwB${c}found it in a legendary land"
      doTest("jklwB", before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE) {
        com.flop.idea.fim.FimPlugin.setEnabled(false)
      }
    } finally {
      com.flop.idea.fim.FimPlugin.setEnabled(true)
    }
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.EDITOR_MODIFICATION)
  fun `test turn plugin off and on`() {
    val before = "I ${c}found it in a legendary land"
    val after = "I f${c}ound it in a legendary land"
    doTest("l", before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE) {
      com.flop.idea.fim.FimPlugin.setEnabled(false)
      com.flop.idea.fim.FimPlugin.setEnabled(true)
    }
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.EDITOR_MODIFICATION)
  fun `test enable twice`() {
    val before = "I ${c}found it in a legendary land"
    val after = "I f${c}ound it in a legendary land"
    doTest("l", before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE) {
      com.flop.idea.fim.FimPlugin.setEnabled(false)
      com.flop.idea.fim.FimPlugin.setEnabled(true)
      com.flop.idea.fim.FimPlugin.setEnabled(true)
    }
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.EDITOR_MODIFICATION)
  fun `test unregister extension`() {
    val before = "I ${c}found it in a legendary land"
    val after = "I f${c}ound it in a legendary land"
    var motionRightAction: ActionBeanClass? = null
    doTest("l", before, after, FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE) {
      motionRightAction = VIM_ACTIONS_EP.extensions().filter { it.actionId == "FimPreviousTabAction" }.findFirst().get()

      assertNotNull(getCommandNode())

      @Suppress("DEPRECATION")
      VIM_ACTIONS_EP.getPoint(null).unregisterExtension(motionRightAction!!)
      assertNull(getCommandNode())
    }
    @Suppress("DEPRECATION")
    VIM_ACTIONS_EP.getPoint(null).registerExtension(motionRightAction!!)
    TestCase.assertNotNull(getCommandNode())
  }

  private fun getCommandNode(): CommandNode<ActionBeanClass>? {
    // TODO: 08.02.2020 Sorry if your tests will fail because of this test
    val node = com.flop.idea.fim.FimPlugin.getKey().getKeyRoot(MappingMode.NORMAL)[KeyStroke.getKeyStroke('g')] as CommandPartNode
    return node[KeyStroke.getKeyStroke('T')] as CommandNode<ActionBeanClass>?
  }
}
