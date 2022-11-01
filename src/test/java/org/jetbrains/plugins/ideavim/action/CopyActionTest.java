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

import com.intellij.openapi.editor.Editor;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.FimInjectorKt;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.register.Register;
import com.flop.idea.fim.ex.ExException;
import com.flop.idea.fim.fimscript.model.datatypes.FimString;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import org.jetbrains.plugins.ideafim.SkipNeofimReason;
import org.jetbrains.plugins.ideafim.TestWithoutNeofim;
import org.jetbrains.plugins.ideafim.FimTestCase;

/**
 * @author vlan
 */
public class CopyActionTest extends FimTestCase {
  // |y| |p| |count|
  public void testYankPutCharacters() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("y2h" + "p"), "one two<caret> three\n");
    assertState("one twwoo three\n");
  }

  // |yy|
  public void testYankLine() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("yy" + "p"), "one\n" + "tw<caret>o\n" + "three\n");
    assertState("one\n" + "two\n" + "two\n" + "three\n");
  }

  // VIM-723 |p|
  public void testYankPasteToEmptyLine() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("yiw" + "j" + "p"), "foo\n" + "\n" + "bar\n");
    assertState("foo\n" + "foo\n" + "bar\n");
  }

  // VIM-390 |yy| |p|
  public void testYankLinePasteAtLastLine() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("yy" + "p"), "one two\n" + "<caret>three four\n");
    assertState("one two\n" + "three four\n" + "three four\n");
  }

  // |register| |y|
  public void testYankRegister() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("\"ayl" + "l" + "\"byl" + "\"ap" + "\"bp"), "hel<caret>lo world\n");
    assertState("hellolo world\n");
  }

  // |register| |y| |quote|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testYankRegisterUsesLastEnteredRegister() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("\"a\"byl" + "\"ap"), "hel<caret>lo world\n");
    assertState("helllo world\n");
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testYankAppendRegister() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("\"Ayl" + "l" + "\"Ayl" + "\"Ap"), "hel<caret>lo world\n");
    assertState("hellolo world\n");
  }

  public void testYankWithInvalidRegister() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("\"&"), "hel<caret>lo world\n");
    assertPluginError(true);
  }

  // |P|
  public void testYankPutBefore() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("y2l" + "P"), "<caret>two\n");
    assertState("twtwo\n");
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  public void testWrongYankQuoteMotion() {
    assertPluginError(false);
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("y\""), "one <caret>two\n" + "three\n" + "four\n");
    assertPluginError(true);
  }

  public void testWrongYankQuoteYankLine() {
    assertPluginError(false);
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("y\"" + "yy" + "p"), "one <caret>two\n" + "three\n" + "four\n");
    assertPluginError(false);
    assertState("one two\n" + "one two\n" + "three\n" + "four\n");
  }

  public void testWrongYankRegisterMotion() {
    final Editor editor = typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("y\"" + "0"), "one <caret>two\n" + "three\n" + "four\n");
    assertEquals(0, editor.getCaretModel().getOffset());
  }

  // VIM-632 |CTRL-V| |v_y| |p|
  public void testYankVisualBlock() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("<C-V>" + "jl" + "yl" + "p"), "<caret>* one\n" + "* two\n");

    // XXX:
    // The correct output should be:
    //
    // * * one
    // * * two
    //
    // The problem is that the selection range should be 1-char wide when entering the visual block mode

    assertState("* * one\n" + "* * two\n");
    assertSelection(null);
    assertOffset(2);
  }

  // VIM-632 |CTRL-V| |v_y|
  public void testStateAfterYankVisualBlock() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("<C-V>" + "jl" + "y"), "<caret>foo\n" + "bar\n");
    assertOffset(0);
    assertMode(FimStateMachine.Mode.COMMAND);
    assertSelection(null);
  }

  // VIM-476 |yy| |'clipboard'|
  // TODO: Review this test
  // This doesn't use the system clipboard, but the TestClipboardModel
  public void testClipboardUnnamed() throws ExException {
    assertEquals('\"', FimPlugin.getRegister().getDefaultRegister());
    FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.clipboardName, new FimString("unnamed"), OptionConstants.clipboardName);
    assertEquals('*', FimPlugin.getRegister().getDefaultRegister());
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("yy"), "foo\n" + "<caret>bar\n" + "baz\n");
    final Register starRegister = FimPlugin.getRegister().getRegister('*');
    assertNotNull(starRegister);
    assertEquals("bar\n", starRegister.getText());
  }

  // VIM-792 |"*| |yy| |p|
  // TODO: Review this test
  // This doesn't use the system clipboard, but the TestClipboardModel
  public void testLineWiseClipboardYankPaste() {
    configureByText("<caret>foo\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("\"*yy" + "\"*p"));
    final Register register = FimPlugin.getRegister().getRegister('*');
    assertNotNull(register);
    assertEquals("foo\n", register.getText());
    assertState("foo\n" + "<caret>foo\n");
  }

  // VIM-792 |"*| |CTRL-V| |v_y| |p|
  // TODO: Review this test
  // This doesn't use the system clipboard, but the TestClipboardModel
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testBlockWiseClipboardYankPaste() {
    configureByText("<caret>foo\n" + "bar\n" + "baz\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("<C-V>j" + "\"*y" + "\"*p"));
    final Register register = FimPlugin.getRegister().getRegister('*');
    assertNotNull(register);
    assertEquals("f\n" + "b", register.getText());
    assertState("ffoo\n" + "bbar\n" + "baz\n");
  }

  // VIM-1431
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testPutInEmptyFile() {
    FimPlugin.getRegister().setKeys('a', FimInjectorKt.getInjector().getParser().parseKeys("test"));
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("\"ap"), "");
    assertState("test");
  }

  public void testOverridingRegisterWithEmptyTag() {
    configureByText("<root>\n" + "<a><caret>value</a>\n" + "<b></b>\n" + "</root>\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("dit" + "j" + "cit" + "<C-R>\""));
    assertState("<root>\n" + "<a></a>\n" + "<b>value</b>\n" + "</root>\n");
  }
}
