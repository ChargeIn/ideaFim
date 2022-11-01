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

import com.flop.idea.fim.api.FimInjectorKt;
import org.jetbrains.plugins.ideafim.SkipNeofimReason;
import org.jetbrains.plugins.ideafim.TestWithoutNeofim;
import org.jetbrains.plugins.ideafim.FimTestCase;

/**
 * @author Aleksey Lagoshin
 */
public class AutoIndentTest extends FimTestCase {
  // VIM-256 |==|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testCaretPositionAfterAutoIndent() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int <caret>b;\n" + "   int c;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("=="));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "   int c;\n" + "}\n");
  }

  // |2==|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testAutoIndentWithCount() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int <caret>b;\n" + "   int c;\n" + "   int d;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("2=="));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "    int c;\n" + "   int d;\n" + "}\n");
  }

  // |=k|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testAutoIndentWithUpMotion() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int b;\n" + "   int <caret>c;\n" + "   int d;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("=k"));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "    int c;\n" + "   int d;\n" + "}\n");
  }

  // |=l|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testAutoIndentWithRightMotion() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int <caret>b;\n" + "   int c;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("=l"));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "   int c;\n" + "}\n");
  }

  // |2=j|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testAutoIndentWithCountsAndDownMotion() {
    configureByJavaText("class C {\n" + "   int <caret>a;\n" + "   int b;\n" + "   int c;\n" + "   int d;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("2=j"));
    assertState("class C {\n" + "    <caret>int a;\n" + "    int b;\n" + "    int c;\n" + "   int d;\n" + "}\n");
  }

  // |v| |l| |=|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testVisualAutoIndent() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int <caret>b;\n" + "   int c;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("v" + "l" + "="));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "   int c;\n" + "}\n");
  }

  // |v| |j| |=|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testVisualMultilineAutoIndent() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int <caret>b;\n" + "   int c;\n" + "   int d;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("v" + "j" + "="));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "    int c;\n" + "   int d;\n" + "}\n");
  }

  // |C-v| |j| |=|
  @TestWithoutNeofim(reason = SkipNeofimReason.DIFFERENT)
  public void testVisualBlockAutoIndent() {
    configureByJavaText("class C {\n" + "   int a;\n" + "   int <caret>b;\n" + "   int c;\n" + "   int d;\n" + "}\n");
    typeText(FimInjectorKt.getInjector().getParser().parseKeys("<C-V>" + "j" + "="));
    assertState("class C {\n" + "   int a;\n" + "    <caret>int b;\n" + "    int c;\n" + "   int d;\n" + "}\n");
  }
}
