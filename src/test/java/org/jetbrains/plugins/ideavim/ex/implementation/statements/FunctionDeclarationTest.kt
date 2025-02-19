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

package org.jetbrains.plugins.ideafim.ex.implementation.statements

import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimTestCase

class FunctionDeclarationTest : FimTestCase() {

  fun `test user defined function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function GetHiString(name) |" +
          "  return 'Oh hi ' . a:name | " +
          "endfunction |" +
          "echo GetHiString('Mark')"
      )
    )
    assertExOutput("Oh hi Mark\n")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test unknown function`() {
    configureByText("\n")
    typeText(commandToKeys("echo GetHiString('Mark')"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E117: Unknown function: GetHiString")
  }

  fun `test nested function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  function F2() |" +
          "    return 555 |" +
          "  endfunction |" +
          "  return 10 * F2() |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    assertExOutput("5550\n")
    typeText(commandToKeys("echo F2()"))
    assertExOutput("555\n")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test call nested function without calling a container function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  function F2() |" +
          "    return 555 |" +
          "  endfunction |" +
          "  return 10 * F2() |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F2()"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E117: Unknown function: F2")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test defining an existing function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  return 10 |" +
          "endfunction"
      )
    )
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  return 100 |" +
          "endfunction"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("E122: Function F1 already exists, add ! to replace it")

    typeText(commandToKeys("echo F1()"))
    assertExOutput("10\n")

    typeText(commandToKeys("delf! F1"))
  }

  fun `test redefining an existing function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  return 10 |" +
          "endfunction"
      )
    )
    typeText(
      commandToKeys(
        "" +
          "function! F1() |" +
          "  return 100 |" +
          "endfunction"
      )
    )
    assertPluginError(false)
    typeText(commandToKeys("echo F1()"))
    assertExOutput("100\n")

    typeText(commandToKeys("delf! F1"))
  }

  fun `test closure function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  let x = 5 |" +
          "  function F2() closure |" +
          "    return 10 * x |" +
          "  endfunction |" +
          "  return F2() |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    assertExOutput("50\n")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN_ERROR)
  fun `test outer variable cannot be reached from inner function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  let x = 5 |" +
          "  function F2() |" +
          "    return 10 * x |" +
          "  endfunction |" +
          "  return F2() |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: x")
    assertExOutput("0\n")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  fun `test call closure function multiple times`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  let x = 0 |" +
          "  function F2() closure |" +
          "    let x += 1 |" +
          "    return x |" +
          "  endfunction |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    typeText(commandToKeys("echo F2()"))
    assertExOutput("1\n")
    typeText(commandToKeys("echo F2()"))
    assertExOutput("2\n")
    typeText(commandToKeys("echo F2()"))
    assertExOutput("3\n")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  fun `test local variables exist after delfunction command`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  let x = 0 |" +
          "  function F2() closure |" +
          "    let x += 1 |" +
          "    return x |" +
          "  endfunction |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    typeText(commandToKeys("echo F2()"))
    assertExOutput("1\n")
    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("echo F2()"))
    assertExOutput("2\n")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN_ERROR)
  fun `test outer function does not see inner closure function variable`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function F1() |" +
          "  function! F2() closure |" +
          "    let x = 1 |" +
          "    return 10 |" +
          "  endfunction |" +
          "  echo x |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: x")

    typeText(commandToKeys("echo F2()"))
    assertExOutput("10\n")
    assertPluginError(false)

    typeText(commandToKeys("echo F1()"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: x")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN_ERROR)
  fun `test function without abort flag`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function! F1() |" +
          "  echo unknownVar |" +
          "  let g:x = 10 |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: unknownVar")

    typeText(commandToKeys("echo x"))
    assertExOutput("10\n")
    assertPluginError(false)

    typeText(commandToKeys("delf! F1"))
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN_ERROR)
  fun `test function with abort flag`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function! F1() abort |" +
          "  echo unknownVar |" +
          "  let g:x = 10 |" +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo F1()"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: unknownVar")

    typeText(commandToKeys("echo x"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E121: Undefined variable: x")

    typeText(commandToKeys("delf! F1"))
  }

  fun `test function without range flag`() {
    configureByText(
      """
        -----
        ${c}12345
        abcde
        -----
      """.trimIndent()
    )
    typeText(
      commandToKeys(
        "" +
          "let rangesConcatenation = '' |" +
          "function! F1() |" +
          "  let g:rangesConcatenation .= line('.') |" +
          "endfunction |"
      )
    )
    typeText(commandToKeys("1,3call F1()"))
    typeText(commandToKeys("echo rangesConcatenation"))
    assertPluginError(false)
    assertExOutput("123\n")

    assertState(
      """
        -----
        12345
        ${c}abcde
        -----
      """.trimIndent()
    )
    typeText(commandToKeys("delf! F1"))
  }

  fun `test function with range flag`() {
    configureByText(
      """
        -----
        12345
        abcde
        $c-----
      """.trimIndent()
    )
    typeText(
      commandToKeys(
        "" +
          "let rangesConcatenation = '' |" +
          "function! F1() range |" +
          "  let g:rangesConcatenation .= line('.') |" +
          "endfunction |"
      )
    )
    typeText(commandToKeys("1,3call F1()"))
    typeText(commandToKeys("echo rangesConcatenation"))
    assertPluginError(false)
    assertExOutput("1\n")

    assertState(
      """
        $c-----
        12345
        abcde
        -----
      """.trimIndent()
    )
    typeText(commandToKeys("delf! F1"))
  }

  @TestWithoutNeofim(SkipNeofimReason.PLUGIN_ERROR)
  fun `test trying to create a function with firstline or lastline argument`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function! F1(firstline) |" +
          "  echo unknownVar |" +
          "  let g:x = 10 |" +
          "endfunction"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("E125: Illegal argument: firstline")

    typeText(
      commandToKeys(
        "" +
          "function! F1(lastline) |" +
          "  echo unknownVar |" +
          "  let g:x = 10 |" +
          "endfunction"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("E125: Illegal argument: lastline")

    typeText(commandToKeys("delf! F1"))
    typeText(commandToKeys("delf! F2"))
  }

  fun `test firstline and lastline default value if no range specified`() {
    configureByText(
      """
        -----
        12${c}345
        abcde
        -----
      """.trimIndent()
    )
    typeText(
      commandToKeys(
        "" +
          "function! F1() range |" +
          "  echo a:firstline .. ':' .. a:lastline |" +
          "endfunction |"
      )
    )
    typeText(commandToKeys("call F1()"))
    assertPluginError(false)
    assertExOutput("2:2\n")
    assertState(
      """
        -----
        12${c}345
        abcde
        -----
      """.trimIndent()
    )
    typeText(commandToKeys("delf! F1"))
  }

  fun `test firstline and lastline default value if range is specified`() {
    configureByText(
      """
        -----
        12${c}345
        abcde
        -----
      """.trimIndent()
    )
    typeText(
      commandToKeys(
        "" +
          "function! F1() |" +
          "  echo a:firstline .. ':' .. a:lastline |" +
          "endfunction |"
      )
    )
    typeText(commandToKeys("1,4call F1()"))
    assertPluginError(false)
    assertExOutput("1:4\n")
    assertState(
      """
        -----
        12345
        abcde
        $c-----
      """.trimIndent()
    )
    typeText(commandToKeys("delf! F1"))
  }

  fun `test functions without range flag columns`() {
    configureByText(
      """
        -----
        12345
        abcde
        ---$c--
      """.trimIndent()
    )
    typeText(
      commandToKeys(
        "" +
          "let columns = '' |" +
          "function! F1() |" +
          "  let g:columns .= col('.') .. ',' |" +
          "endfunction |"
      )
    )
    typeText(commandToKeys("call F1()"))
    typeText(commandToKeys("echo columns"))
    assertPluginError(false)
    assertExOutput("4,\n")

    typeText(commandToKeys("let columns = ''"))
    typeText(commandToKeys("1,3call F1()"))
    typeText(commandToKeys("echo columns"))
    assertPluginError(false)
    assertExOutput("1,1,1,\n")

    typeText(commandToKeys("delf! F1"))
  }

  fun `test functions with range flag columns`() {
    configureByText(
      """
        -----
        12345
        abcde
        ---$c--
      """.trimIndent()
    )
    typeText(
      commandToKeys(
        "" +
          "let columns = '' |" +
          "function! F1() range |" +
          "  let g:columns .= col('.') .. ',' |" +
          "endfunction |"
      )
    )
    typeText(commandToKeys("call F1()"))
    typeText(commandToKeys("echo columns"))
    assertPluginError(false)
    assertExOutput("4,\n")

    typeText(commandToKeys("let columns = ''"))
    typeText(commandToKeys("1,3call F1()"))
    typeText(commandToKeys("echo columns"))
    assertPluginError(false)
    assertExOutput("1,\n")

    typeText(commandToKeys("delf! F1"))
  }

  fun `test only optional arguments`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function GetOptionalArgs(...) |" +
          "  return a:000 | " +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo GetOptionalArgs()"))
    assertExOutput("[]\n")
    typeText(
      commandToKeys(
        "echo GetOptionalArgs(42, 'optional arg')"
      )
    )
    assertExOutput("[42, 'optional arg']\n")

    typeText(commandToKeys("delfunction! GetOptionalArgs"))
  }

  fun `test only default arguments`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function GetDefaultArgs(a = 10, b = 20) |" +
          "  return [a:a, a:b] | " +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo GetDefaultArgs()"))
    assertExOutput("[10, 20]\n")
    typeText(commandToKeys("echo GetDefaultArgs(42, 'optional arg')"))
    assertExOutput("[42, 'optional arg']\n")

    typeText(commandToKeys("delfunction! GetDefaultArgs"))
  }

  fun `test optional arguments`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function GetOptionalArgs(name, ...) |" +
          "  return a:000 | " +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional')"))
    assertExOutput("[]\n")
    typeText(
      commandToKeys(
        "echo GetOptionalArgs('this arg is not optional', 42, 'optional arg')"
      )
    )
    assertExOutput("[42, 'optional arg']\n")

    typeText(commandToKeys("delfunction! GetOptionalArgs"))
  }

  fun `test arguments with default values`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function GetOptionalArgs(name, a = 10, b = 20) |" +
          "  return 'a = ' .. a:a .. ', b = ' .. a:b | " +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional')"))
    assertExOutput("a = 10, b = 20\n")

    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional', 42)"))
    assertExOutput("a = 42, b = 20\n")

    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional', 100, 200)"))
    assertExOutput("a = 100, b = 200\n")

    typeText(commandToKeys("delfunction! GetOptionalArgs"))
  }

  fun `test arguments with default values and optional args`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function GetOptionalArgs(name, a = 10, b = 20, ...) |" +
          "  return {'a': a:a, 'b': a:b, '000': a:000} | " +
          "endfunction"
      )
    )
    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional')"))
    assertExOutput("{'a': 10, 'b': 20, '000': []}\n")

    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional', 42)"))
    assertExOutput("{'a': 42, 'b': 20, '000': []}\n")

    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional', 100, 200)"))
    assertExOutput("{'a': 100, 'b': 200, '000': []}\n")

    typeText(commandToKeys("echo GetOptionalArgs('this arg is not optional', 100, 200, 300)"))
    assertExOutput("{'a': 100, 'b': 200, '000': [300]}\n")

    typeText(commandToKeys("delfunction! GetOptionalArgs"))
  }

  fun `test finish statement in function`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        """
        let x = 3 |
        function! F() |
          finish |
          let g:x = 10 |
        endfunction |
        """.trimIndent()
      )
    )
    typeText(commandToKeys("call F()"))
    typeText(commandToKeys("echo x"))
    assertExOutput("3\n")

    typeText(commandToKeys("delfunction! F"))
  }

  fun `test args are passed to function by reference`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        """
        function! AddNumbers(dict) |
          let a:dict.one = 1 |
          let a:dict['two'] = 2 |
        endfunction
        """.trimIndent()
      )
    )
    typeText(commandToKeys("let d = {}"))
    typeText(commandToKeys("call AddNumbers(d)"))
    typeText(commandToKeys("echo d"))
    assertPluginError(false)
    assertExOutput("{'one': 1, 'two': 2}\n")

    typeText(commandToKeys("delfunction! AddNumbers"))
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test define script function in command line context`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function s:GetHiString(name) |" +
          "  return 'Oh hi ' . a:name | " +
          "endfunction"
      )
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("E81: Using <SID> not in a script context")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test get script function in command line context`() {
    configureByText("\n")
    typeText(
      commandToKeys("echo s:F1()")
    )
    assertPluginError(true)
    assertPluginErrorMessageContains("E120: Using <SID> not in a script context: s:F1")
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.PLUGIN_ERROR)
  fun `test get built-in function with global scope`() {
    configureByText("\n")
    typeText(commandToKeys("echo g:abs(-10)"))
    assertPluginError(true)
    assertPluginErrorMessageContains("E117: Unknown function: g:abs")
  }

  fun `test return with no expression`() {
    configureByText("\n")
    typeText(
      commandToKeys(
        "" +
          "function ZeroGenerator() |" +
          "  return | " +
          "endfunction"
      )
    )
    assertPluginError(false)
    typeText(commandToKeys("echo ZeroGenerator()"))
    assertExOutput("0\n")
  }
}
