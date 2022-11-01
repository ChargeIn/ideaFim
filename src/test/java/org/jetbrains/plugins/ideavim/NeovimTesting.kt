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

package org.jetbrains.plugins.ideafim

import com.ensarsarajcic.neovim.java.api.NeovimApi
import com.ensarsarajcic.neovim.java.api.NeovimApis
import com.ensarsarajcic.neovim.java.api.types.api.VimCoords
import com.ensarsarajcic.neovim.java.corerpc.client.ProcessRpcConnection
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.CharacterPosition
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.register.RegisterConstants.ALTERNATE_BUFFER_REGISTER
import com.flop.idea.fim.register.RegisterConstants.BLACK_HOLE_REGISTER
import com.flop.idea.fim.register.RegisterConstants.CLIPBOARD_REGISTERS
import com.flop.idea.fim.register.RegisterConstants.CURRENT_FILENAME_REGISTER
import com.flop.idea.fim.register.RegisterConstants.EXPRESSION_BUFFER_REGISTER
import com.flop.idea.fim.register.RegisterConstants.LAST_INSERTED_TEXT_REGISTER
import com.flop.idea.fim.register.RegisterConstants.LAST_SEARCH_REGISTER
import com.flop.idea.fim.register.RegisterConstants.VALID_REGISTERS
import org.junit.Assert.assertEquals

internal object NeoVimTesting {
  private lateinit var neofimApi: NeovimApi
  private lateinit var neofim: Process

  private var neofimTestsCounter = 0

  private var currentTestName = ""
  private val untested = mutableListOf<String>()

  private lateinit var exitCommand: String
  private lateinit var escapeCommand: String
  private lateinit var ctrlcCommand: String

  private var singleCaret = true

  fun setUp(test: FimTestCase) {
    if (!neofimEnabled(test)) return
    val nvimPath = System.getenv("ideafim.nvim.path") ?: "nvim"

    val pb = ProcessBuilder(
      nvimPath,
      "-u", "NONE",
      "--embed",
      "--headless",
      "--clean",
      "--cmd", "set noswapfile",
      "--cmd", "set sol"
    )

    neofim = pb.start()
    val neofimConnection = ProcessRpcConnection(neofim, true)
    neofimApi = NeovimApis.getApiForConnection(neofimConnection)
    exitCommand = neofimApi.replaceTermcodes("<esc><esc>:qa!", true, false, true).get()
    escapeCommand = neofimApi.replaceTermcodes("<esc>", true, false, true).get()
    ctrlcCommand = neofimApi.replaceTermcodes("<C-C>", true, false, true).get()
    currentTestName = test.name
  }

  fun tearDown(test: FimTestCase) {
    if (!neofimEnabled(test)) return
    println("Tested with neovim: $neofimTestsCounter")
    if (FimTestCase.Checks.neoFim.exitOnTearDown) {
      neofimApi.input(exitCommand).get()
    }
    neofim.destroy()
    if (currentTestName.isNotBlank()) {
      untested.add(currentTestName)
      println("----")
      println("$untested : ${untested.size}")
    }
  }

  private fun neofimEnabled(test: FimTestCase, editor: Editor? = null): Boolean {
    val method = test.javaClass.getMethod(test.name)
    val noBehaviourDiffers = !method.isAnnotationPresent(FimBehaviorDiffers::class.java)
    val noTestingWithoutNeofim = !method.isAnnotationPresent(TestWithoutNeofim::class.java)
    val neofimTestingEnabled = System.getProperty("ideafim.nvim.test", "false")!!.toBoolean()
    val notParserTest = "org.jetbrains.plugins.ideafim.ex.parser" !in test.javaClass.packageName
    val notScriptImplementation = "org.jetbrains.plugins.ideafim.ex.implementation" !in test.javaClass.packageName
    val notExtension = "org.jetbrains.plugins.ideafim.extension" !in test.javaClass.packageName
    if (singleCaret) {
      singleCaret = editor == null || editor.caretModel.caretCount == 1
    }
    return noBehaviourDiffers &&
      noTestingWithoutNeofim &&
      neofimTestingEnabled &&
      notParserTest &&
      notScriptImplementation &&
      notExtension &&
      singleCaret
  }

  fun setupEditor(editor: Editor, test: FimTestCase) {
    if (!neofimEnabled(test, editor)) return
    neofimApi.currentBuffer.get().setLines(0, -1, false, editor.document.text.split("\n")).get()
    val charPosition = CharacterPosition.fromOffset(editor, editor.caretModel.offset)
    neofimApi.currentWindow.get().setCursor(VimCoords(charPosition.line + 1, charPosition.column)).get()
  }

  fun typeCommand(keys: String, test: FimTestCase, editor: Editor) {
    if (!neofimEnabled(test, editor)) return
    when {
      keys.equals("<esc>", ignoreCase = true) -> neofimApi.input(escapeCommand).get()
      keys.equals("<C-C>", ignoreCase = true) -> neofimApi.input(ctrlcCommand).get()
      else -> {
        val replacedCodes = neofimApi.replaceTermcodes(keys, true, false, true).get()
        neofimApi.input(replacedCodes).get()
      }
    }
  }

  fun assertState(editor: Editor, test: FimTestCase) {
    if (!neofimEnabled(test, editor)) return
    if (currentTestName != "") {
      currentTestName = ""
      neofimTestsCounter++
    }
    assertText(editor)
    assertCaret(editor, test)
    assertMode(editor)
    assertRegisters()
  }

  fun setRegister(register: Char, keys: String, test: FimTestCase) {
    if (!neofimEnabled(test)) return
    neofimApi.callFunction("setreg", listOf(register, keys, 'c'))
  }

  private fun getCaret(): VimCoords = neofimApi.currentWindow.get().cursor.get()
  private fun getText(): String = neofimApi.currentBuffer.get().getLines(0, -1, false).get().joinToString("\n")

  fun assertCaret(editor: Editor, test: FimTestCase) {
    if (!neofimEnabled(test, editor)) return
    if (currentTestName != "") {
      currentTestName = ""
      neofimTestsCounter++
    }
    val fimCoords = getCaret()
    val resultFimCoords = CharacterPosition.atCaret(editor).toFimCoords()
    assertEquals(fimCoords.toString(), resultFimCoords.toString())
  }

  private fun assertText(editor: Editor) {
    val neofimContent = getText()
    assertEquals(neofimContent, editor.document.text)
  }

  private fun assertMode(editor: Editor) {
    val ideafimState = editor.fim.fimStateMachine.toFimNotation()
    val neofimState = neofimApi.mode.get().mode
    assertEquals(neofimState, ideafimState)
  }

  private const val nonCheckingRegisters =
    CLIPBOARD_REGISTERS +
      LAST_INSERTED_TEXT_REGISTER +
      BLACK_HOLE_REGISTER +
      LAST_SEARCH_REGISTER +
      ALTERNATE_BUFFER_REGISTER +
      EXPRESSION_BUFFER_REGISTER +
      CURRENT_FILENAME_REGISTER

  private fun assertRegisters() {
    for (register in VALID_REGISTERS) {
      if (register in nonCheckingRegisters) continue
      if (register in FimTestCase.Checks.neoFim.ignoredRegisters) continue
      val neofimRegister = neofimApi.callFunction("getreg", listOf(register)).get().toString()
      val fimPluginRegister = com.flop.idea.fim.FimPlugin.getRegister().getRegister(register)
      val ideafimRegister = fimPluginRegister?.text ?: ""
      assertEquals("Register '$register'", neofimRegister, ideafimRegister)

      if (neofimRegister.isNotEmpty()) {
        val neofimRegisterType = neofimApi.callFunction("getregtype", listOf(register)).get().toString()
        val expectedType = when (fimPluginRegister?.type) {
          SelectionType.CHARACTER_WISE -> "v"
          SelectionType.LINE_WISE -> "V"
          SelectionType.BLOCK_WISE -> "\u0016"
          else -> ""
        }

        // We take only the first char because neofim returns width for block selection
        val neofimChar = neofimRegisterType.getOrNull(0)?.toString() ?: ""
        assertEquals("Register '$register'", expectedType, neofimChar)
      }
    }
  }
}

annotation class TestWithoutNeofim(val reason: SkipNeofimReason, val description: String = "")

enum class SkipNeofimReason {
  PLUGIN,

  @Suppress("unused")
  INLAYS,
  OPTION,
  UNCLEAR,
  NON_ASCII,
  MAPPING,
  SELECT_MODE,
  VISUAL_BLOCK_MODE,
  DIFFERENT,

  // This test doesn't check fim behaviour
  NOT_VIM_TESTING,

  SHOW_CMD,
  SCROLL,
  TEMPLATES,
  EDITOR_MODIFICATION,

  CMD,
  ACTION_COMMAND,
  PLUG,
  FOLDING,
  TABS,
  PLUGIN_ERROR,

  VIM_SCRIPT,

  GUARDED_BLOCKS,
  CTRL_CODES,
}

fun LogicalPosition.toFimCoords(): VimCoords {
  return VimCoords(this.line + 1, this.column)
}
