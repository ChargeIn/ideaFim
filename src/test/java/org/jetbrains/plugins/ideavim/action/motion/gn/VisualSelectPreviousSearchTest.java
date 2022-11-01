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

package org.jetbrains.plugins.ideafim.action.motion.gn;

import com.intellij.idea.TestFor;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.action.motion.search.SearchWholeWordForwardAction;
import com.flop.idea.fim.api.FimInjectorKt;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.common.Direction;
import org.jetbrains.plugins.ideafim.SkipNeofimReason;
import org.jetbrains.plugins.ideafim.TestWithoutNeofim;
import org.jetbrains.plugins.ideafim.FimTestCase;

public class VisualSelectPreviousSearchTest extends FimTestCase {
  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testSearch() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*w" + "gN"), "h<caret>ello world\nhello world hello world");

    assertOffset(12);
    assertSelection("hello");
    assertMode(FimStateMachine.Mode.VISUAL);
  }

  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testSearchMulticaret() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*" + "b" + "gN"), "h<caret>ello world\nh<caret>ello world hello world");

    assertEquals(1, myFixture.getEditor().getCaretModel().getCaretCount());
    assertMode(FimStateMachine.Mode.VISUAL);
  }

  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testSearchWhenOnMatch() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*" + "gN"), "h<caret>ello world\nhello world hello world");

    assertOffset(12);
    assertSelection("hello");
    assertMode(FimStateMachine.Mode.VISUAL);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testWithoutSpaces() {
    configureByText("tes<caret>ttest");
    FimPlugin.getSearch().setLastSearchState(myFixture.getEditor(), "test", "", Direction.FORWARDS);
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("gN"));

    assertOffset(0);
    assertSelection("test");
    assertMode(FimStateMachine.Mode.VISUAL);
  }

  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testSearchTwice() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*" + "2gN"), "hello world\nh<caret>ello world hello");

    assertOffset(12);
    assertSelection("hello");
  }

  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testTwoSearchesStayInVisualMode() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*" + "gN" + "gN"), "hello world\nh<caret>ello world hello");

    assertOffset(12);
    assertSelection("hello world hello");
    assertMode(FimStateMachine.Mode.VISUAL);
  }

  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testCanExitVisualMode() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*" + "gN" + "gN" + "<Esc>"), "hello world\nh<caret>ello world hello");

    assertOffset(12);
    assertSelection(null);
    assertMode(FimStateMachine.Mode.COMMAND);
  }

  @TestFor(classes = {SearchWholeWordForwardAction.class})
  public void testIfInMiddlePositionOfSearchAndInVisualModeThenSelectCurrent() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*llv" + "gN"), "hello hello");

    assertOffset(6);
    assertSelection("hel");
    assertMode(FimStateMachine.Mode.VISUAL);
  }

  public void testWithTabs() {
    typeTextInFile(FimInjectorKt.getInjector().getParser().parseKeys("*" + "gN" + "gN"), "hello 1\n\thello 2\n\the<caret>llo 3\n\thello 4");

    assertOffset(18);
    assertSelection("hello 3\n\thello");
    assertMode(FimStateMachine.Mode.VISUAL);
  }
}
