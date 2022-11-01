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
package org.jetbrains.plugins.ideafim.action

import com.intellij.codeInsight.folding.CodeFoldingManager
import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.FimBehaviorDiffers
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

/**
 * @author vlan
 */
class ChangeActionTest : FimTestCase() {
  // VIM-620 |i_CTRL-O|
  fun testInsertSingleCommandAndInserting() {
    doTest(
      listOf("i", "<C-O>", "a", "123", "<Esc>", "x"), "abc${c}d\n", "abcd12\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-620 |i_CTRL-O|
  fun testInsertSingleCommandAndNewLineInserting() {
    doTest(
      listOf("i", "<C-O>", "o", "123", "<Esc>", "x"),
      "abc${c}d\n", "abcd\n12\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-620 |i_CTRL-O|
  fun testInsertSingleCommandAndNewLineInserting2() {
    doTest(
      listOf("i", "<C-O>", "v"),
      "12${c}345", "12${s}${c}3${se}45", FimStateMachine.Mode.INSERT_VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  // VIM-620 |i_CTRL-O|
  fun testInsertSingleCommandAndNewLineInserting3() {
    doTest(
      listOf("i", "<C-O>", "v", "<esc>"),
      "12${c}345", "12${c}345", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-620 |i_CTRL-O|
  fun testInsertSingleCommandAndNewLineInserting4() {
    doTest(
      listOf("i", "<C-O>", "v", "d"),
      "12${c}345", "12${c}45", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-620 |i_CTRL-O|
  @TestWithoutNeofim(SkipNeofimReason.SELECT_MODE)
  fun testInsertSingleCommandAndNewLineInserting5() {
    doTest(
      listOf("i", "<C-O>", "v", "<C-G>"),
      "12${c}345", "12${s}3${c}${se}45", FimStateMachine.Mode.INSERT_SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  // VIM-620 |i_CTRL-O|
  @TestWithoutNeofim(SkipNeofimReason.SELECT_MODE)
  fun testInsertSingleCommandAndNewLineInserting6() {
    doTest(
      listOf("i", "<C-O>", "gh"),
      "12${c}345", "12${s}3${c}${se}45", FimStateMachine.Mode.INSERT_SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER
    )
  }

  // VIM-620 |i_CTRL-O|
  @TestWithoutNeofim(SkipNeofimReason.SELECT_MODE)
  fun testInsertSingleCommandAndNewLineInserting7() {
    doTest(
      listOf("i", "<C-O>", "gh", "<esc>"),
      "12${c}345", "123${c}45", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

/*
  // Turn it on after typing via handlers are implemented for tests
  // VIM-620 |i_CTRL-O|
  fun ignoreTestInsertSingleCommandAndNewLineInserting8() {
    doTest(
      listOf("i", "<C-O>", "gh", "d"),
      "12${c}345", "12d${c}45", CommandState.Mode.INSERT, CommandState.SubMode.NONE
    )
  }
*/

  // VIM-311 |i_CTRL-O|
  fun testInsertSingleCommand() {
    doTest(
      listOf("i", "def", "<C-O>", "d2h", "x"),
      "abc$c.\n", "abcdx.\n", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-321 |d| |count|
  fun testDeleteEmptyRange() {
    doTest("d0", "${c}hello\n", "hello\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // VIM-157 |~|
  fun testToggleCharCase() {
    doTest("~~", "${c}hello world\n", "HEllo world\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // VIM-157 |~|
  fun testToggleCharCaseLineEnd() {
    doTest("~~", "hello wor${c}ld\n", "hello worLD\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun testToggleCaseMotion() {
    doTest("g~w", "${c}FooBar Baz\n", "fOObAR Baz\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun testChangeUpperCase() {
    doTest(
      "gUw", "${c}FooBar Baz\n", "FOOBAR Baz\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testChangeLowerCase() {
    doTest("guw", "${c}FooBar Baz\n", "foobar Baz\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  fun testToggleCaseVisual() {
    doTest(
      "ve~", "${c}FooBar Baz\n", "fOObAR Baz\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testChangeUpperCaseVisual() {
    doTest(
      "veU", "${c}FooBar Baz\n", "FOOBAR Baz\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testChangeLowerCaseVisual() {
    doTest(
      "veu", "${c}FooBar Baz\n", "foobar Baz\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-85 |i| |gi| |gg|
  fun testInsertAtPreviousAction() {
    doTest(
      listOf("i", "hello", "<Esc>", "gg", "gi", " world! "),
      """
   one
   two ${c}three
   four
   
      """.trimIndent(),
      """
   one
   two hello world! three
   four
   
      """.trimIndent(),
      FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-312 |d| |w|
  fun testDeleteLastWordInFile() {
    doTest(
      "dw",
      """
        one
        ${c}two
        
      """.trimIndent(),
      """
        one
        
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
    assertOffset(4)
  }

  // |d| |w|
  fun testDeleteLastWordBeforeEOL() {
    doTest(
      "dw",
      """
   one ${c}two
   three
   
      """.trimIndent(),
      """
   one 
   three
   
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-105 |d| |w|
  fun testDeleteLastWordBeforeEOLs() {
    doTest(
      "dw",
      """
   one ${c}two
   
   three
   
      """.trimIndent(),
      """
   one 
   
   three
   
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-105 |d| |w|
  fun testDeleteLastWordBeforeEOLAndWhitespace() {
    doTest(
      "dw",
      """one ${c}two
 three
""",
      """one 
 three
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
    assertOffset(3)
  }

  // VIM-105 |d| |w| |count|
  fun testDeleteTwoWordsOnTwoLines() {
    doTest(
      "d2w",
      """
   one ${c}two
   three four
   
      """.trimIndent(),
      "one four\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-1380 |d| |w| |count|
  fun testDeleteTwoWordsAtLastChar() {
    doTest(
      "d2w", "on${c}e two three\n", "on${c}three\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-394 |d| |v_aw|
  fun testDeleteIndentedWordBeforePunctuation() {
    doTest(
      "daw",
      """foo
  ${c}bar, baz
""",
      """foo
  , baz
""",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // |d| |v_aw|
  fun testDeleteLastWordAfterPunctuation() {
    doTest(
      "daw",
      """
   foo(${c}bar
   baz
   
      """.trimIndent(),
      """
   foo(
   baz
   
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-244 |d| |l|
  fun testDeleteLastCharInLine() {
    doTest(
      "dl",
      """
        fo${c}o
        bar
        
      """.trimIndent(),
      """
        fo
        bar
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
    assertOffset(1)
  }

  // VIM-393 |d|
  fun testDeleteBadArgument() {
    doTest(
      listOf("dD", "dd"),
      """
   one
   two
   
      """.trimIndent(),
      "two\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-262 |i_CTRL-R|
  fun testInsertFromRegister() {
    setRegister('a', "World")
    doTest(
      listOf("A", ", ", "<C-R>", "a", "!"), "${c}Hello\n", "Hello, World!\n", FimStateMachine.Mode.INSERT,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-404 |O|
  fun testInsertNewLineAboveFirstLine() {
    doTest(
      listOf("O", "bar"),
      "fo${c}o\n", "bar\nfoo\n", FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-472 |v|
  fun testVisualSelectionRightMargin() {
    doTest(
      listOf("v", "k\$d"),
      "foo\n${c}bar\n", "fooar\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-632 |CTRL-V| |v_d|
  fun testDeleteVisualBlock() {
    doTest(
      listOf("<C-V>", "jjl", "d"),
      """
        ${c}foo
        bar
        baz
        quux
        
      """.trimIndent(),
      """
        ${c}o
        r
        z
        quux
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteCharVisualBlock() {
    doTest(
      listOf("<C-V>", "jjl", "x"),
      """
        ${c}foo
        bar
        baz
        quux
        
      """.trimIndent(),
      """
        ${c}o
        r
        z
        quux
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteJoinLinesSpaces() {
    doTest(
      "3J",
      """    a$c 1
    b 2
    c 3
quux
""",
      """    a 1 b 2 c 3
quux
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteJoinLines() {
    doTest(
      "3gJ",
      """    a$c 1
    b 2
    c 3
quux
""",
      """    a 1    b 2    c 3
quux
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  @FimBehaviorDiffers(originalFimAfter = "foo  bar")
  fun testDeleteJoinLinesWithTrailingSpaceThenEmptyLine() {
    doTest(
      "3J",
      """
        foo.
        
        bar
      """.dotToSpace().trimIndent(),
      "foo bar", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteJoinLinesWithTwoTrailingSpaces() {
    doTest(
      "J",
      """
        foo..
        bar
      """.dotToSpace().trimIndent(),
      "foo  bar", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteJoinVisualLinesSpaces() {
    doTest(
      "v2jJ",
      """    a$c 1
    b 2
    c 3
quux
""",
      """    a 1 b 2 c 3
quux
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteJoinVisualLines() {
    doTest(
      "v2jgJ",
      """    a$c 1
    b 2
    c 3
quux
""",
      """    a 1    b 2    c 3
quux
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteCharVisualBlockOnLastCharOfLine() {
    doTest(
      listOf("<C-V>", "x"),
      "fo${c}o\n", "fo\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testDeleteCharVisualBlockOnEmptyLinesDoesntDeleteAnything() {
    setupChecks {
      this.neoFim.ignoredRegisters = setOf('1', '"')
    }
    doTest(
      listOf("<C-V>", "j", "x"),
      "\n\n", "\n\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-781 |CTRL-V| |j|
  fun testDeleteCharVisualBlockWithEmptyLineInTheMiddle() {
    doTest(
      listOf("l", "<C-V>", "jj", "x"),
      """
        foo
        
        bar
        
      """.trimIndent(),
      """
        fo
        
        br
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-781 |CTRL-V| |j|
  @FimBehaviorDiffers(description = "Different registers content")
  fun testDeleteCharVisualBlockWithShorterLineInTheMiddle() {
    doTest(
      listOf("l", "<C-V>", "jj", "x"),
      """
        foo
        x
        bar
        
      """.trimIndent(),
      """
        fo
        x
        br
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-845 |CTRL-V| |x|
  fun testDeleteVisualBlockOneCharWide() {
    configureByText(
      """
  foo
  bar
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("<C-V>" + "j" + "x"))
    assertState(
      """
  oo
  ar
  
      """.trimIndent()
    )
  }

  // |r|
  fun testReplaceOneChar() {
    doTest("rx", "b${c}ar\n", "b${c}xr\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // |r|
  @FimBehaviorDiffers(originalFimAfter = "foXX${c}Xr\n")
  fun testReplaceMultipleCharsWithCount() {
    doTest("3rX", "fo${c}obar\n", "fo${c}XXXr\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // |r|
  fun testReplaceMultipleCharsWithCountPastEndOfLine() {
    doTest("6rX", "fo${c}obar\n", "fo${c}obar\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // |r|
  @FimBehaviorDiffers(description = "Different caret position")
  fun testReplaceMultipleCharsWithVisual() {
    doTest(
      listOf("v", "ll", "j", "rZ"),
      """
        fo${c}obar
        foobaz
        
      """.trimIndent(),
      """
        foZZZZ
        ZZZZZz
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // |r|
  fun testReplaceOneCharWithNewline() {
    doTest(
      "r<Enter>",
      """    fo${c}obar
foobaz
""",
      """    fo
    bar
foobaz
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // |r|
  @FimBehaviorDiffers(description = "Different caret position")
  fun testReplaceCharWithNewlineAndCountAddsOnlySingleNewline() {
    doTest(
      "3r<Enter>",
      """    fo${c}obar
foobaz
""",
      """    fo
    r
foobaz
""",
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // |s|
  fun testReplaceOneCharWithText() {
    doTest("sxy<Esc>", "b${c}ar\n", "bx${c}yr\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // |s|
  fun testReplaceMultipleCharsWithTextWithCount() {
    doTest(
      "3sxy<Esc>",
      "fo${c}obar\n", "fox${c}yr\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // |s|
  fun testReplaceMultipleCharsWithTextWithCountPastEndOfLine() {
    doTest(
      "99sxyz<Esc>",
      """
        foo${c}bar
        biff
        
      """.trimIndent(),
      """
        fooxy${c}z
        biff
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // |R|
  fun testReplaceMode() {
    doTest("Rbaz<Esc>", "foo${c}bar\n", "fooba${c}z\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
  }

  // |R| |i_<Insert>|
  @FimBehaviorDiffers(description = "Different caret position")
  fun testReplaceModeSwitchToInsertModeAndBack() {
    doTest(
      "RXXX<Ins>YYY<Ins>ZZZ<Esc>",
      "aaa${c}bbbcccddd\n", "aaaXXXYYYZZ${c}Zddd\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // |i| |i_<Insert>|
  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR, "<INS> works strange")
  fun testInsertModeSwitchToReplaceModeAndBack() {
    doTest(
      "iXXX<Ins>YYY<Ins>ZZZ<Esc>",
      "aaa${c}bbbcccddd\n", "aaaXXXYYYZZ${c}Zcccddd\n", FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-511 |.|
  @TestWithoutNeofim(SkipNeofimReason.UNCLEAR, "Backspace workspace strange")
  fun testRepeatWithBackspaces() {
    doTest(
      listOf("ce", "foo", "<BS><BS><BS>", "foo", "<Esc>", "j0", "."),
      """
        ${c}foo baz
        baz quux
        
      """.trimIndent(),
      """
        foo baz
        fo${c}o quux
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-511 |.|
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testRepeatWithParensAndQuotesAutoInsertion() {
    configureByJavaText(
      """
  class C $c{
  }
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("o" + "foo(\"<Right>, \"<Right><Right>;" + "<Esc>" + "."))
    assertState(
      """class C {
    foo("", "");
    foo("", "");
}
"""
    )
  }

  // VIM-511 |.|
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testDeleteBothParensAndStartAgain() {
    configureByJavaText(
      """
  class C $c{
  }
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("o" + "C(" + "<BS>" + "(int i) {}" + "<Esc>" + "."))
    assertState(
      """class C {
    C(int i) {}
    C(int i) {}
}
"""
    )
  }

  // VIM-613 |.|
  fun testDeleteEndOfLineAndAgain() {
    configureByText(
      """
  $c- 1
  - 2
  - 3
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("d$" + "j" + "."))
    assertState(
      """
  
  
  - 3
  
      """.trimIndent()
    )
  }

  // VIM-511 |.|
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testAutoCompleteCurlyBraceWithEnterWithinFunctionBody() {
    configureByJavaText(
      """
  class C $c{
  }
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("o" + "C(" + "<BS>" + "(int i) {" + "<Enter>" + "i = 3;" + "<Esc>" + "<Down>" + "."))
    assertState(
      """class C {
    C(int i) {
        i = 3;
    }
    C(int i) {
        i = 3;
    }
}
"""
    )
  }

  // VIM-1067 |.|
  @TestWithoutNeofim(SkipNeofimReason.DIFFERENT)
  fun testRepeatWithInsertAfterLineEnd() {
    // Case 1
    configureByText(
      """
  $c- 1
  - 2
  - 3
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("A" + "<BS>" + "<Esc>" + "j" + "."))
    assertState(
      """
  - 
  - 
  - 3
  
      """.trimIndent()
    )

    // Case 2
    configureByText(
      """
  $c- 1
  - 2
  - 3
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("A" + "4" + "<BS>" + "<Esc>" + "j" + "."))
    assertState(
      """
  - 1
  - 2
  - 3
  
      """.trimIndent()
    )

    // Case 3
    configureByText(
      """
  $c- 1
  - 2
  - 3
  
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("A" + "<BS>" + "4" + "<Esc>" + "j" + "."))
    assertState(
      """
  - 4
  - 4
  - 3
  
      """.trimIndent()
    )
  }

  // VIM-287 |zc| |O|
  fun testInsertAfterFold() {
    configureByJavaText(
      """$c/**
 * I should be fold
 * a little more text
 * and final fold
 */
and some text after"""
    )
    typeText(injector.parser.parseKeys("zc" + "G" + "O"))
    assertState(
      """/**
 * I should be fold
 * a little more text
 * and final fold
 */
$c
and some text after"""
    )
  }

  // VIM-287 |zc| |o|
  @TestWithoutNeofim(SkipNeofimReason.FOLDING)
  fun testInsertBeforeFold() {
    configureByJavaText(
      """
          $c/**
           * I should be fold
           * a little more text
           * and final fold
           */
          and some text after
      """.trimIndent()
    )

    myFixture.editor.foldingModel.runBatchFoldingOperation {
      CodeFoldingManager.getInstance(myFixture.project).updateFoldRegions(myFixture.editor)
      FoldingUtil.findFoldRegionStartingAtLine(myFixture.editor, 0)!!.isExpanded = false
    }

    typeText(injector.parser.parseKeys("o"))
    assertState(
      """
            /**
             * I should be fold
             * a little more text
             * and final fold
             */
            $c
            and some text after
      """.trimIndent()
    )
  }

  fun testRepeatChangeWordDoesNotBreakNextRepeatFind() {
    doTest(
      "fXcfYPATATA<Esc>fX.;.", "${c}aaaaXBBBBYaaaaaaaXBBBBYaaaaaaXBBBBYaaaaaaaa\n",
      "aaaaPATATAaaaaaaaPATATAaaaaaaPATATAaaaaaaaa\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testRepeatReplace() {
    configureByText("${c}foobarbaz spam\n")
    typeText(injector.parser.parseKeys("R"))
    assertMode(FimStateMachine.Mode.REPLACE)
    typeText(injector.parser.parseKeys("FOO" + "<Esc>" + "l" + "2."))
    assertState("FOOFOOFO${c}O spam\n")
    assertMode(FimStateMachine.Mode.COMMAND)
  }

  fun testDownMovementAfterDeletionToStart() {
    doTest(
      "ld^j",
      """
        lorem ${c}ipsum dolor sit amet
        lorem ipsum dolor sit amet
      """.trimIndent(),
      """
        psum dolor sit amet
        ${c}lorem ipsum dolor sit amet
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testDownMovementAfterDeletionToPrevWord() {
    doTest(
      "ldbj",
      """
        lorem$c ipsum dolor sit amet
        lorem ipsum dolor sit amet
      """.trimIndent(),
      """
        ipsum dolor sit amet
        ${c}lorem ipsum dolor sit amet
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testDownMovementAfterChangeToPrevWord() {
    doTest(
      "lcb<Esc>j",
      """
        lorem$c ipsum dolor sit amet
        lorem ipsum dolor sit amet
      """.trimIndent(),
      """
        ipsum dolor sit amet
        ${c}lorem ipsum dolor sit amet
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testDownMovementAfterChangeToLineStart() {
    doTest(
      "lc^<Esc>j",
      """
        lorem$c ipsum dolor sit amet
        lorem ipsum dolor sit amet
      """.trimIndent(),
      """
        ipsum dolor sit amet
        ${c}lorem ipsum dolor sit amet
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testUpMovementAfterDeletionToStart() {
    doTest(
      "ld^k",
      """
        lorem ipsum dolor sit amet
        lorem ${c}ipsum dolor sit amet
      """.trimIndent(),
      """
        ${c}lorem ipsum dolor sit amet
        psum dolor sit amet
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun testUpMovementAfterChangeToPrevWord() {
    doTest(
      "lcb<Esc>k",
      """
        lorem ipsum dolor sit amet
        lorem$c ipsum dolor sit amet
      """.trimIndent(),
      """
        ${c}lorem ipsum dolor sit amet
        ipsum dolor sit amet
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  // VIM-714 |v|
  fun testDeleteVisualColumnPositionOneLine() {
    doTest(
      "vwxj",
      """
        ${c}lorem ipsum dolor sit amet
        lorem ipsum dolor sit amet
        
      """.trimIndent(),
      """
        psum dolor sit amet
        ${c}lorem ipsum dolor sit amet
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  // VIM-714 |v|
  fun testDeleteVisualColumnPositionMultiLine() {
    doTest(
      "v3wfixj",
      """
        gaganis ${c}gaganis gaganis
        gaganis gaganis gaganis
        gaganis gaganis gaganis
        
      """.trimIndent(),
      """
        gaganis s gaganis
        gaganis ${c}gaganis gaganis
        
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }

  fun testChangeSameLine() {
    doTest(
      "d_",
      """
        line 1
        line$c 2
        line 3
      """.trimIndent(),
      """
        line 1
        ${c}line 3
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE
    )
  }
}
