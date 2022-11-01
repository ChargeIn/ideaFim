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

package org.jetbrains.plugins.ideafim.action;

import com.google.common.collect.Lists;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.FimInjectorKt;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.mark.Mark;
import com.flop.idea.fim.newapi.IjFimEditor;
import org.jetbrains.plugins.ideafim.SkipNeofimReason;
import org.jetbrains.plugins.ideafim.TestWithoutNeofim;
import org.jetbrains.plugins.ideafim.FimTestCase;

/**
 * @author Tuomas Tynkkynen
 */
public class MarkTest extends FimTestCase {
  // |m|
  public void testLocalMark() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("ma"), "    foo\n" + "    ba<caret>r\n" + "    baz\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'a');
    assertNotNull(mark);
    assertEquals(1, mark.getLogicalLine());
    assertEquals(6, mark.getCol());
  }

  // |m|
  public void testGlobalMark() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mG"), "    foo\n" + "    ba<caret>r\n" + "    baz\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'G');
    assertNotNull(mark);
    assertEquals(1, mark.getLogicalLine());
    assertEquals(6, mark.getCol());
  }

  // |m|
  public void testMarkIsDeletedWhenLineIsDeleted() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mx" + "dd"), "    foo\n" + "    ba<caret>r\n" + "    baz\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'x');
    assertNull(mark);
  }

  // |m|
  public void testMarkIsNotDeletedWhenLineIsOneCharAndReplaced() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("ma" + "r1"), "foo\n" + "<caret>0\n" + "bar\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'a');
    assertNotNull(mark);
  }

  // |m|
  public void testMarkIsNotDeletedWhenLineIsChanged() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("ma" + "cc"), "    foo\n" + "    ba<caret>r\n" + "    baz\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'a');
    assertNotNull(mark);
  }

  // |m|
  public void testMarkIsMovedUpWhenLinesArePartiallyDeletedAbove() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mx" + "2k" + "dd" + "0dw"), "    foo\n" + "    bar\n" + "    ba<caret>z\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'x');
    assertNotNull(mark);
    assertEquals(1, mark.getLogicalLine());
    assertEquals(6, mark.getCol());
  }

  // |m|
  public void testMarkIsMovedUpWhenLinesAreDeletedAbove() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mx" + "2k" + "2dd"), "    foo\n" + "    bar\n" + "    ba<caret>z\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'x');
    assertNotNull(mark);
    assertEquals(0, mark.getLogicalLine());
    assertEquals(6, mark.getCol());
  }

  // |m|
  public void testMarkIsMovedDownWhenLinesAreInsertedAbove() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mY" + "Obiff"), "foo\n" + "ba<caret>r\n" + "baz\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'Y');
    assertNotNull(mark);
    assertEquals(2, mark.getLogicalLine());
    assertEquals(2, mark.getCol());
  }

  // |m|
  public void testMarkIsMovedDownWhenLinesAreInsertedAboveWithIndentation() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mY" + "Obiff"), "    foo\n" + "    ba<caret>r\n" + "    baz\n");
    Mark mark = FimPlugin.getMark().getMark(new IjFimEditor(myFixture.getEditor()), 'Y');
    assertNotNull(mark);
    assertEquals(2, mark.getLogicalLine());
    assertEquals(6, mark.getCol());
  }

  // |m| |`|
  public void testMarkAndJumpToMark() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("6l" + "mZ" + "G$" + "`Z"), "    foo\n" + "    bar\n" + "    baz\n");
    assertOffset(6);
  }

  // |m| |'|
  public void testMarkAndJumpToMarkLeadingSpace() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("6l" + "mb" + "G$" + "'b"), "    foo\n" + "    bar\n" + "    baz\n");
    assertOffset(4);
  }

  // |m| |`|
  public void testDeleteBacktickMotionIsCharacterWise() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mk" + "kh" + "d`k"), "    abcd\n" + "    efgh\n" + "    ij<caret>kl\n" + "    mnop\n");
    assertState("    abcd\n" + "    ekl\n" + "    mnop\n");
  }

  // |m| |`|
  public void testDeleteSingleQuoteMotionIsLineWise() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("mk" + "kh" + "d'k"), "    abcd\n" + "    efgh\n" + "    ij<caret>kl\n" + "    mnop\n");
    assertState("    abcd\n" + "    mnop\n");
  }

  // VIM-43 |i| |`.|
  @TestWithoutNeofim(reason = SkipNeofimReason.UNCLEAR)
  public void testGotoLastChangePosition() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("i" + "hello " + "<Esc>" + "gg" + "`."),
                   "one two\n" + "<caret>hello world\n" + "three four\n");
    assertOffset(13);
  }

  // VIM-43 |p| |`.|
  public void testGotoLastPutPosition() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("yy" + "p" + "gg" + "`."), "one two\n" + "<caret>three\n" + "four five\n");
    assertOffset(14);
  }

  // |i| |`]|
  public void testGotoLastChangePositionEnd() {
    doTest(Lists.newArrayList("yiw", "P", "gg", "`]"), "one two\n" + "<caret>three\n" + "four five\n",
           "one two\n" + "thre<caret>ethree\n" + "four five\n", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }
}
