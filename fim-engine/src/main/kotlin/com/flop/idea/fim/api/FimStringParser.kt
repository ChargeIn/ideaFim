package com.flop.idea.fim.api

import org.jetbrains.annotations.NonNls
import javax.swing.KeyStroke

interface FimStringParser {
  /**
   * Fake key for `<Plug>` mappings
   */
  val plugKeyStroke: KeyStroke
  /**
   * Fake key for `<Action>` mappings
   */
  val actionKeyStroke: KeyStroke

  /**
   * Parses Fim key notation strings.
   * @see <a href="http://fimdoc.sourceforge.net/htmldoc/intro.html#key-notation">Fim key notation</a>
   *
   * @throws java.lang.IllegalArgumentException if the mapping doesn't make sense for Fim emulation
   */
  fun parseKeys(@NonNls string: String): List<KeyStroke>

  /**
   * Transforms string of regular and control characters (e.g. "ihello") to list of keystrokes
   */
  fun stringToKeys(@NonNls string: String): List<KeyStroke>

  /**
   * Transforms a keystroke to a string in Fim key notation.
   * @see <a href="http://fimdoc.sourceforge.net/htmldoc/intro.html#key-notation">Fim key notation</a>
   */
  fun toKeyNotation(keyStroke: KeyStroke): String

  /**
   * Transforms list of keystrokes to a string in Fim key notation.
   * @see <a href="http://fimdoc.sourceforge.net/htmldoc/intro.html#key-notation">Fim key notation</a>
   */
  fun toKeyNotation(keyStrokes: List<KeyStroke>): String

  /**
   * Transforms list of keystrokes to a pastable to editor string
   *
   * e.g. "`<C-I>hello<Esc>`" -> "  hello" (<C-I> is a tab character)
   */
  // todo better name
  fun toPrintableString(keys: List<KeyStroke>): String

  /**
   * This method is used to parse content of double-quoted strings in FimScript.
   * @see <a href="http://fimdoc.sourceforge.net/htmldoc/eval.html#expr-string">:help string</a>
   */
  fun parseFimScriptString(string: String): String
}
