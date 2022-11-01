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
import com.flop.idea.fim.command.FimStateMachine;
import org.jetbrains.plugins.ideafim.FimTestCase;

/**
 * @author Tuomas Tynkkynen
 */
public class ChangeNumberActionTest extends FimTestCase {
  public void testIncrementDecimalZero() {
    doTest("<C-A>", "0", "1", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementHexZero() {
    doTest("<C-A>", "0x0", "0x1", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testDecrementZero() {
    doTest("<C-X>", "0", "-1", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementDecimal() {
    doTest("<C-A>", "199", "200", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testDecrementDecimal() {
    doTest("<C-X>", "1000", "999", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementOctal() {
    doTest(Lists.newArrayList(":set nf=octal<Enter>", "<C-A>"), "0477", "0500", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testDecrementOctal() {
    doTest(Lists.newArrayList(":set nf=octal<Enter>", "<C-X>"), "010", "007", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementHex() {
    doTest("<C-A>", "0xff", "0x100", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testDecrementHex() {
    doTest("<C-X>", "0xa100", "0xa0ff", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementNegativeDecimal() {
    doTest("<C-A>", "-199", "-198", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testDecrementNegativeDecimal() {
    doTest("<C-X>", "-1000", "-1001", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementNegativeOctal() {
    // Minus isn't processed
    doTest(Lists.newArrayList(":set nf=octal<Enter>", "<C-A>"), "-0477", "-0500", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testDecrementNegativeOctal() {
    // Minus isn't processed
    doTest(Lists.newArrayList(":set nf=octal<Enter>", "<C-X>"), "-010", "-007", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementNegativeHex() {
    doTest("<C-A>", "-0xff", "-0x100", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testDecrementNegativeHex() {
    doTest("<C-X>", "-0xa100", "-0xa0ff", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementWithCount() {
    doTest("123<C-A>", "456", "579", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testDecrementWithCount() {
    doTest("200<C-X>", "100", "-100", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementAlphaWithoutNumberFormatAlpha() {
    doTest("<C-A>", "foo", "foo", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementAlphaWithNumberFormatAlpha() {
    doTest(Lists.newArrayList(":set nf=alpha<Enter>", "<C-A>"), "foo", "goo", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementZWithNumberFormatAlpha() {
    doTest(Lists.newArrayList(":set nf=alpha<Enter>", "<C-A>"), "zzz", "zzz", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementXInHexNumberWithNumberFormatAlphaButNotHex() {
    doTest(Lists.newArrayList(":set nf=alpha<Enter>", "<C-A>"), "0<caret>x1", "0y1", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementXInHexNumberWithNumberFormatHexAlpha() {
    doTest(Lists.newArrayList(":set nf=alpha,hex<Enter>", "<C-A>"), "0<caret>x1", "0x2", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementHexNumberWithoutNumberFormatHex() {
    doTest(Lists.newArrayList(":set nf=octal<Enter>", "<C-A>"), "0x42", "1x42", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementOctalNumberWithoutNumberFormatOctal() {
    doTest(Lists.newArrayList(":set nf=hex<Enter>", "<C-A>"), "077", "078", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementNegativeOctalNumberWithoutNumberFormatOctal() {
    doTest(Lists.newArrayList(":set nf=hex<Enter>", "<C-A>"), "-077", "-076", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }

  public void testIncrementHexPreservesCaseOfX() {
    doTest("<C-A>", "0X88", "0X89", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementHexTakesCaseFromLastLetter() {
    doTest("<C-A>", "0xaB0", "0xAB1", FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE);
  }

  public void testIncrementLocatesNumberOnTheSameLine() {
    doTest("<C-A>", "foo ->* bar 123\n", "foo ->* bar 12<caret>4\n", FimStateMachine.Mode.COMMAND,
           FimStateMachine.SubMode.NONE);
  }
}
