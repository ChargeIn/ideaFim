package org.jetbrains.plugins.ideafim.action.motion.text

import com.flop.idea.fim.command.FimStateMachine
import org.jetbrains.plugins.ideafim.FimTestCase

class MotionParagraphNextActionTest : FimTestCase() {
  fun `test delete paragraph`() {
    doTest(
      "d}",
      """
        void foo() {
        }
        $c
        void bar() {
        }

        void baz() {
        }
      """.trimIndent(),
      """
        void foo() {
        }
        $c
        void baz() {
        }
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
