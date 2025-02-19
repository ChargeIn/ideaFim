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

package org.jetbrains.plugins.ideafim.helper;

import com.flop.idea.fim.api.FimSearchHelperBase;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.helper.SearchHelperKtKt;
import org.jetbrains.plugins.ideafim.SkipNeofimReason;
import org.jetbrains.plugins.ideafim.TestWithoutNeofim;
import org.jetbrains.plugins.ideafim.FimTestCase;

public class SearchHelperTest extends FimTestCase {
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindNextWord() {
    String text = "first second";
    int nextWordPosition = (int)FimSearchHelperBase.Companion.findNextWord(text, 0, text.length(), 1, true, false);

    assertEquals(nextWordPosition, text.indexOf("second"));
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindSecondNextWord() {
    String text = "first second third";
    int nextWordPosition = (int)FimSearchHelperBase.Companion.findNextWord(text, 0, text.length(), 2, true, false);

    assertEquals(nextWordPosition, text.indexOf("third"));
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindAfterLastWord() {
    String text = "first second";
    int nextWordPosition = (int)FimSearchHelperBase.Companion.findNextWord(text, 0, text.length(), 3, true, false);

    assertEquals(nextWordPosition, text.length());
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindPreviousWord() {
    String text = "first second";
    int previousWordPosition =
      (int)FimSearchHelperBase.Companion.findNextWord(text, text.indexOf("second"), text.length(), -1, true, false);

    //noinspection ConstantConditions
    assertEquals(previousWordPosition, text.indexOf("first"));
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindSecondPreviousWord() {
    String text = "first second third";
    int previousWordPosition =
      (int)FimSearchHelperBase.Companion.findNextWord(text, text.indexOf("third"), text.length(), -2, true, false);

    //noinspection ConstantConditions
    assertEquals(previousWordPosition, text.indexOf("first"));
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindBeforeFirstWord() {
    String text = "first second";
    int previousWordPosition =
      (int)FimSearchHelperBase.Companion.findNextWord(text, text.indexOf("second"), text.length(), -3, true, false);

    //noinspection ConstantConditions
    assertEquals(previousWordPosition, text.indexOf("first"));
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testFindPreviousWordWhenCursorOutOfBound() {
    String text = "first second";
    int previousWordPosition =
      (int)FimSearchHelperBase.Companion.findNextWord(text, text.length(), text.length(), -1, true, false);

    assertEquals(previousWordPosition, text.indexOf("second"));
  }

  public void testMotionOuterWordAction() {
    doTest("va(", "((int) nu<caret>m)", "<selection>((int) num)</selection>", FimStateMachine.Mode.VISUAL,
           FimStateMachine.SubMode.VISUAL_CHARACTER);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringInsideDoubleQuotes() {
    String text = "abc\"def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringWithoutClosingDoubleQuote() {
    String text = "abcdef\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringOnUnpairedSingleQuote() {
    String text = "abc\"d'ef\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringOutsideOfDoubleQuotesPair() {
    String text = "abc\"def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 2, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringEscapedDoubleQuote() {
    String text = "abc\\\"def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringOddNumberOfDoubleQuotes() {
    String text = "abc\"def\"gh\"i";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringInsideSingleQuotesPair() {
    String text = "abc\"d'e'f\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 6, false);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringOnOpeningDoubleQuote() {
    String text = "abc\"def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 3, true);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringOnClosingDoubleQuote() {
    String text = "abc\"def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 7, true);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringWithoutQuotes() {
    String text = "abcdefghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringDoubleQuoteInsideSingleQuotes() {
    String text = "abc'\"'ef\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringSingleQuotesAreTooFarFromEachOtherToMakePair() {
    String text = "abc'\"de'f\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 5, true);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringDoubleQuoteInsideSingleQuotesIsInsideSingleQuotedString() {
    String text = "abc'\"'def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 4, false);
    assertTrue(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringAfterClosingDoubleQuote() {
    String text = "abc\"def\"ghi";
    boolean inString = SearchHelperKtKt.checkInString(text, 9, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringOnMiddleDoubleQuote() {
    String text = "abc\"def\"gh\"i";
    boolean inString = SearchHelperKtKt.checkInString(text, 7, true);
    assertFalse(inString);
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  public void testCheckInStringBetweenPairs() {
    String text = "abc\"def\"gh\"ij\"k";
    boolean inString = SearchHelperKtKt.checkInString(text, 8, true);
    assertFalse(inString);
  }
}
