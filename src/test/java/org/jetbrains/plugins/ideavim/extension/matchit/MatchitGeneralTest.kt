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

package org.jetbrains.plugins.ideafim.extension.matchit

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.JavaFileType
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.helper.experimentalApi
import org.jetbrains.plugins.ideafim.FimTestCase

class MatchitGeneralTest : FimTestCase() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    enableExtensions("matchit")
  }

  /*
   * Tests to make sure we didn't break the default % motion
   */

  fun `test jump from Java comment start to end`() {
    doTest(
      "%",
      """
        /$c**
         *
         */
      """.trimIndent(),
      """
        /**
         *
         *$c/
      """.trimIndent(),
      fileType = JavaFileType.INSTANCE
    )
  }

  fun `test jump from Java comment end to start`() {
    doTest(
      "%",
      """
        /**
         *
         *$c/
      """.trimIndent(),
      """
        $c/**
         *
         */
      """.trimIndent(),
      fileType = JavaFileType.INSTANCE
    )
  }

  fun `test 25 percent jump`() {
    doTest(
      "25%",
      """
        int a;
        int b;
        in${c}t c;
        int d;
      """.trimIndent(),
      """
        ${c}int a;
        int b;
        int c;
        int d;
      """.trimIndent(),
      fileType = HtmlFileType.INSTANCE
    )
  }

  fun `test jump from visual end of line to opening parenthesis`() {
    doTest(
      "v$%",
      """foo(${c}bar)""",
      """foo${s}$c(b${se}ar)""",
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER, HtmlFileType.INSTANCE
    )
  }

  fun `test jump from visual end of line to opening parenthesis then back to closing`() {
    doTest(
      "v$%%",
      """foo(${c}bar)""",
      """foo(${s}bar$c)$se""",
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER, HtmlFileType.INSTANCE
    )
  }

  fun `test delete everything from opening parenthesis to closing parenthesis`() {
    doTest(
      "d%",
      "$c(x == 123)", "", fileType = HtmlFileType.INSTANCE
    )
  }

  fun `test delete everything from closing parenthesis to opening parenthesis`() {
    doTest(
      "d%",
      "(x == 123$c)", "", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE, HtmlFileType.INSTANCE
    )
  }

  fun `test delete everything from opening curly brace to closing curly brace`() {
    doTest(
      "d%",
      "$c{ foo: 123 }", "", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE, HtmlFileType.INSTANCE
    )
  }

  fun `test delete everything from closing curly brace to opening curly brace`() {
    doTest(
      "d%",
      "{ foo: 123 $c}", "", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE, HtmlFileType.INSTANCE
    )
  }

  fun `test delete everything from opening square bracket to closing square bracket`() {
    doTest(
      "d%",
      "$c[1, 2, 3]", "", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE, HtmlFileType.INSTANCE
    )
  }

  fun `test delete everything from closing square bracket to opening square bracket`() {
    doTest(
      "d%",
      "[1, 2, 3$c]", "", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE, HtmlFileType.INSTANCE
    )
  }

  /*
   * Tests for visual mode and deleting on the new Matchit patterns.
   */
  fun `test jump from visual end of line to opening angle bracket`() {
    doTest(
      "v$%",
      """</h${c}tml>""",
      """${s}$c</ht${se}ml>""",
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER, HtmlFileType.INSTANCE
    )
  }

  fun `test jump from visual end of line to start of for loop`() {
    doTest(
      "v$%",
      """
        for n in [1, 2, 3]
          puts n
        e${c}nd
      """.trimIndent(),
      """
        ${s}${c}for n in [1, 2, 3]
          puts n
        en${se}d
      """.trimIndent(),
      FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER, fileName = "ruby.rb"
    )
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
        if x == 0
          puts "Zero"
        $c
          puts "Positive"
        end
  """,
    description = "Our code changes the motion type to linewise, but it should not"
  )
  fun `test delete from elseif to else`() {
    doTest(
      "d%",
      """
        if x == 0
          puts "Zero"
        ${c}elsif x < -1
          puts "Negative"
        else
          puts "Positive"
        end
      """.trimIndent(),
      if (experimentalApi()) {
        """
        if x == 0
          puts "Zero"
          puts "Positive"
        end
        """.trimIndent()
      } else {
        """
              if x == 0
                puts "Zero"
              $c
                puts "Positive"
              end
        """.trimIndent()
      },
      fileName = "ruby.rb"
    )
  }

  fun `test delete from elseif to else 2`() {
    doTest(
      "d%",
      """
        if x == 0
          puts "Zero"
        el${c}sif x < -1
          puts "Negative"
        else
          puts "Positive"
        end
      """.trimIndent(),
      """
        if x == 0
          puts "Zero"
        e${c}l
          puts "Positive"
        end
      """.trimIndent(),
      fileName = "ruby.rb"
    )
  }

  fun `test delete from else to elsif with reverse motion`() {
    doTest(
      "dg%",
      """
        if x == 0
          puts "Zero"
        elsif x < -1
          puts "Negative"
        ${c}else
          puts "Positive"
        end
      """.trimIndent(),
      """
        if x == 0
          puts "Zero"
        ${c}lse
          puts "Positive"
        end
      """.trimIndent(),
      fileName = "ruby.rb"
    )
  }

  fun `test delete from opening to closing div`() {
    doTest(
      "d%",
      """
        <${c}div>
          <img src="fff">
        </div>
      """.trimIndent(),
      "$c<",
      fileType = HtmlFileType.INSTANCE
    )
  }

  fun `test delete from opening angle bracket to closing angle bracket`() {
    doTest(
      "d%",
      """
        $c<div></div>
      """.trimIndent(),
      "$c</div>",
      fileType = HtmlFileType.INSTANCE
    )
  }

  fun `test delete whole function from def`() {
    doTest(
      "d%",
      """
        ${c}def function
          puts "hello"
        end
      """.trimIndent(),
      "",
      fileName = "ruby.rb"
    )
  }

  fun `test delete whole function from def with reverse motion`() {
    doTest(
      "dg%",
      """
        ${c}def function
          puts "hello"
        end
      """.trimIndent(),
      "",
      fileName = "ruby.rb"
    )
  }

  fun `test delete whole function from end`() {
    doTest(
      "d%",
      """
        def function
          puts "hello"
        en${c}d
      """.trimIndent(),
      "",
      fileName = "ruby.rb"
    )
  }

  fun `test delete whole function from end with reverse motion`() {
    doTest(
      "dg%",
      """
        def function
          puts "hello"
        en${c}d
      """.trimIndent(),
      "",
      fileName = "ruby.rb"
    )
  }
}
