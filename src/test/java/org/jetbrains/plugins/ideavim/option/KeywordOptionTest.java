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

package org.jetbrains.plugins.ideafim.option;

import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.FimInjectorKt;
import com.flop.idea.fim.helper.CharacterHelper;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import com.flop.idea.fim.options.helpers.KeywordOptionHelper;
import com.flop.idea.fim.fimscript.model.datatypes.FimString;
import org.jetbrains.plugins.ideafim.FimTestCase;

import java.util.ArrayList;
import java.util.List;

public class KeywordOptionTest extends FimTestCase {

  private List<String> getValues() {
    return KeywordOptionHelper.INSTANCE.parseValues(getOptionValue());
  }

  private String getOptionValue() {
    return ((FimString)FimPlugin.getOptionService()
      .getOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.iskeywordName, OptionConstants.iskeywordName)).getValue();
  }

  private void setKeyword(String val) {
    FimPlugin.getOptionService()
      .setOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.iskeywordName, new FimString(val), "testToken");
  }

  private void assertIsKeyword(char c) {
    CharacterHelper.CharacterType charType = CharacterHelper.charType(c, false);
    assertSame(CharacterHelper.CharacterType.KEYWORD, charType);
  }

  private void assertIsNotKeyword(char c) {
    CharacterHelper.CharacterType charType = CharacterHelper.charType(c, false);
    assertSame(CharacterHelper.CharacterType.PUNCTUATION, charType);
  }

  public void testSingleCommaIsAValue() {
    setKeyword(",");
    assertEquals(",", getValues().get(0));
  }

  public void testSingleCommaIsAValueAsAppend() {
    FimInjectorKt.getInjector().getFimscriptExecutor().execute("set iskeyword^=,", false);
    assertTrue(getValues().contains(","));
  }

  public void testSingleNegatedCommaIsAValue() {
    setKeyword("^,");
    assertEquals("^,", getValues().get(0));
  }

  public void testCommaInARangeIsAValue() {
    setKeyword("+-,");
    assertEquals("+-,", getValues().get(0));
  }

  public void testSecondCommaIsASeparator() {
    setKeyword(",,a");
    assertEquals(",", getValues().get(0));
    assertEquals("a", getValues().get(1));
  }

  public void testSingleHyphenIsAValue() {
    setKeyword("-");
    assertEquals("-", getValues().get(0));
  }

  public void testHyphenBetweenCharNumsIsARange() {
    setKeyword("a-b");
    assertEquals("a-b", getValues().get(0));
  }

  public void testRangeInWhichLeftValueIsHigherThanRightValueIsInvalid() {
    try {
      setKeyword("b-a");
      fail("exception missing");
    } catch (Exception e) {
      assertEquals("E474: Invalid argument: testToken", e.getMessage());
    }
    assertDoesntContain(getValues(), new ArrayList<>() {{
      add("b-a");
    }});
  }

  public void testTwoAdjacentLettersAreInvalid() {
    try {
      setKeyword("ab");
      fail("exception missing");
    } catch (Exception e) {
      assertEquals("E474: Invalid argument: testToken", e.getMessage());
    }
    assertDoesntContain(getValues(), new ArrayList<>() {{
      add("ab");
    }});
  }

  public void testAddsACharByChar() {
    setKeyword("-");
    assertIsKeyword('-');
  }

  public void testAddsACharByUnicodeCodePoint() {
    setKeyword("" + (int)'-');
    assertIsKeyword('-');
  }

  public void testAddsARange() {
    setKeyword("a-c");
    assertIsKeyword('a');
    assertIsKeyword('b');
    assertIsKeyword('c');
  }

  public void testAtSignRepresentsAllLetters() {
    setKeyword("@");
    assertIsKeyword('A');
    assertIsKeyword('Ā');
  }

  public void testRangeOfAtSignToAtSignRepresentsAtSign() {
    setKeyword("@-@");
    assertIsKeyword('@');
  }

  public void testCaretRemovesAChar() {
    setKeyword("a");
    FimInjectorKt.getInjector().getFimscriptExecutor().execute("set iskeyword+=^a", true);
    assertIsNotKeyword('a');
  }

  public void testCaretRemovesARange() {
    setKeyword("a-c");
    FimInjectorKt.getInjector().getFimscriptExecutor().execute("set iskeyword+=^b-c,d", true);
    assertIsKeyword('a');
    assertIsNotKeyword('b');
    assertIsNotKeyword('c');
  }

  public void testCaretAloneRepresentsACaret() {
    setKeyword("^");
    assertIsKeyword('^');
  }

  public void testMultibyteCharactersAreKeywords() {
    assertIsKeyword('Ź');
  }

  public void testToRegex() {
    setKeyword("-,a-c");
    final List<String> res = KeywordOptionHelper.INSTANCE.toRegex();
    assertEquals(2, res.size());
    assertTrue(res.contains("-"));
    assertTrue(res.contains("[a-c]"));
  }

  public void testAllLettersToRegex() {
    setKeyword("@");
    final List<String> res = KeywordOptionHelper.INSTANCE.toRegex();
    assertEquals(res.get(0), "\\p{L}");
  }
}
