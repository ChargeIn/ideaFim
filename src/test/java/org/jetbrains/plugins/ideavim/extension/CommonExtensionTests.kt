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

package org.jetbrains.plugins.ideafim.extension

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.invokeLater
import com.intellij.testFramework.PlatformTestUtil
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.MappingMode
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.extension.Alias
import com.flop.idea.fim.extension.ExtensionBeanClass
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.extension.FimExtension
import com.flop.idea.fim.extension.FimExtensionFacade.putExtensionHandlerMapping
import com.flop.idea.fim.extension.FimExtensionFacade.putKeyMapping
import com.flop.idea.fim.extension.FimExtensionFacade.putKeyMappingIfMissing
import com.flop.idea.fim.extension.FimExtensionRegistrar
import com.flop.idea.fim.group.MotionGroup
import com.flop.idea.fim.helper.isEndAllowed
import com.flop.idea.fim.newapi.ij
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionScope
import junit.framework.TestCase
import org.jetbrains.plugins.ideafim.FimTestCase

class OpMappingTest : FimTestCase() {
  private var initialized = false

  private lateinit var extension: ExtensionBeanClass

  override fun setUp() {
    super.setUp()
    if (!initialized) {
      initialized = true

      extension = TestExtension.createBean()

      com.flop.idea.fim.extension.FimExtension.EP_NAME.point.registerExtension(extension, com.flop.idea.fim.FimPlugin.getInstance())
      enableExtensions("TestExtension")
    }
  }

  override fun tearDown() {
    @Suppress("DEPRECATION")
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.unregisterExtension(extension)
    super.tearDown()
  }

  fun `test simple delete`() {
    doTest(
      "dI",
      "${c}I found it in a legendary land",
      "${c}nd it in a legendary land",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test simple delete backwards`() {
    doTest(
      "dP",
      "I found ${c}it in a legendary land",
      "I f${c}it in a legendary land",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test delete emulate inclusive`() {
    doTest(
      "dU",
      "${c}I found it in a legendary land",
      "${c}d it in a legendary land",
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test linewise delete`() {
    doTest(
      "dO",
      """
                A Discovery

                I ${c}found it in a legendary land
                all rocks and lavender and tufted grass,
                where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      """
                A Discovery

                ${c}where it was settled on some sodden sand
                hard by the torrent of a mountain pass.
      """.trimIndent(),
      FimStateMachine.Mode.COMMAND,
      FimStateMachine.SubMode.NONE
    )
  }

  fun `test disable extension via set`() {
    configureByText("${c}I found it in a legendary land")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I$c found it in a legendary land")

    enterCommand("set noTestExtension")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I$c found it in a legendary land")

    enterCommand("set TestExtension")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I ${c}found it in a legendary land")
  }

  fun `test disable extension as extension point`() {
    configureByText("${c}I found it in a legendary land")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I$c found it in a legendary land")

    @Suppress("DEPRECATION")
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.unregisterExtension(extension)
    assertEmpty(com.flop.idea.fim.FimPlugin.getKey().getKeyMappingByOwner(extension.instance.owner))
    typeText(injector.parser.parseKeys("Q"))
    assertState("I$c found it in a legendary land")

    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.registerExtension(extension, com.flop.idea.fim.FimPlugin.getInstance())
    assertEmpty(com.flop.idea.fim.FimPlugin.getKey().getKeyMappingByOwner(extension.instance.owner))
    enableExtensions("TestExtension")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I ${c}found it in a legendary land")
  }

  fun `test disable disposed extension`() {
    configureByText("${c}I found it in a legendary land")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I$c found it in a legendary land")

    enterCommand("set noTestExtension")
    @Suppress("DEPRECATION")
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.unregisterExtension(extension)
    typeText(injector.parser.parseKeys("Q"))
    assertState("I$c found it in a legendary land")

    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.registerExtension(extension, com.flop.idea.fim.FimPlugin.getInstance())
    enableExtensions("TestExtension")
    typeText(injector.parser.parseKeys("Q"))
    assertState("I ${c}found it in a legendary land")
  }

  fun `test delayed action`() {
    configureByText("${c}I found it in a legendary land")
    typeText(injector.parser.parseKeys("R"))
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    assertState("I fou${c}nd it in a legendary land")

    typeText(injector.parser.parseKeys("dR"))
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    assertState("I fou$c in a legendary land")
  }

  /**
   * This test tests an intentionally incorrectly implemented action
   */
  fun `test delayed incorrect action`() {
    configureByText("${c}I found it in a legendary land")
    typeText(injector.parser.parseKeys("E"))
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    assertState("I fou${c}nd it in a legendary land")

    typeText(injector.parser.parseKeys("dE"))
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()
    assertState("I found it$c in a legendary land")
  }
}

class PlugExtensionsTest : FimTestCase() {

  private lateinit var extension: ExtensionBeanClass

  override fun setUp() {
    super.setUp()

    extension = TestExtension.createBean()
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.registerExtension(extension, com.flop.idea.fim.FimPlugin.getInstance())
  }

  override fun tearDown() {
    @Suppress("DEPRECATION")
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.unregisterExtension(extension)
    super.tearDown()
  }

  fun `test enable via plug`() {
    injector.fimscriptExecutor.execute("Plug 'MyTest'", false)

    assertTrue(extension.ext.initialized)
  }

  fun `test enable via plugin`() {
    injector.fimscriptExecutor.execute("Plugin 'MyTest'", false)

    assertTrue(extension.ext.initialized)
  }

  fun `test enable via plug and disable via set`() {
    injector.fimscriptExecutor.execute("Plug 'MyTest'")
    injector.fimscriptExecutor.execute("set noTestExtension")
    assertTrue(extension.ext.initialized)
    assertTrue(extension.ext.disposed)
  }
}

class PlugMissingKeysTest : FimTestCase() {

  private lateinit var extension: ExtensionBeanClass

  override fun setUp() {
    super.setUp()

    extension = TestExtension.createBean()
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.registerExtension(extension, com.flop.idea.fim.FimPlugin.getInstance())
  }

  override fun tearDown() {
    @Suppress("DEPRECATION")
    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.unregisterExtension(extension)
    super.tearDown()
  }

  fun `test missing keys`() {
    executeLikeFimrc(
      "map myKey <Plug>TestMissing",
      "Plug 'MyTest'"
    )

    val keyMappings = com.flop.idea.fim.FimPlugin.getKey().getMapTo(MappingMode.NORMAL, injector.parser.parseKeys("<Plug>TestMissing"))
    TestCase.assertEquals(1, keyMappings.size)
    TestCase.assertEquals(injector.parser.parseKeys("myKey"), keyMappings.first().first)

    val iKeyMappings = com.flop.idea.fim.FimPlugin.getKey().getMapTo(MappingMode.INSERT, injector.parser.parseKeys("<Plug>TestMissing"))
    TestCase.assertEquals(1, iKeyMappings.size)
    TestCase.assertEquals(injector.parser.parseKeys("L"), iKeyMappings.first().first)
  }

  fun `test missing keys enable plugin first`() {
    executeLikeFimrc(
      "Plug 'MyTest'",
      "map myKey <Plug>TestMissing"
    )

    val keyMappings = com.flop.idea.fim.FimPlugin.getKey().getMapTo(MappingMode.NORMAL, injector.parser.parseKeys("<Plug>TestMissing"))
    TestCase.assertEquals(1, keyMappings.size)
    TestCase.assertEquals(injector.parser.parseKeys("myKey"), keyMappings.first().first)

    val iKeyMappings = com.flop.idea.fim.FimPlugin.getKey().getMapTo(MappingMode.INSERT, injector.parser.parseKeys("<Plug>TestMissing"))
    TestCase.assertEquals(1, iKeyMappings.size)
    TestCase.assertEquals(injector.parser.parseKeys("L"), iKeyMappings.first().first)
  }

  fun `test packadd`() {
    assertFalse(injector.optionService.isSet(OptionScope.GLOBAL, "matchit"))
    executeLikeFimrc(
      "packadd matchit",
    )

    assertTrue(injector.optionService.isSet(OptionScope.GLOBAL, "matchit"))
  }

  fun `test packadd ex`() {
    assertFalse(injector.optionService.isSet(OptionScope.GLOBAL, "matchit"))
    executeLikeFimrc(
      "packadd! matchit",
    )

    assertTrue(injector.optionService.isSet(OptionScope.GLOBAL, "matchit"))
  }

  private fun executeLikeFimrc(vararg text: String) {
    injector.fimscriptExecutor.executingFimscript = true
    injector.fimscriptExecutor.execute(text.joinToString("\n"), false)
    injector.fimscriptExecutor.executingFimscript = false
    FimExtensionRegistrar.enableDelayedExtensions()
  }
}

private val ExtensionBeanClass.ext: TestExtension
  get() = this.instance as TestExtension

private class TestExtension : com.flop.idea.fim.extension.FimExtension {

  var initialized = false
  var disposed = false

  override fun getName(): String = "TestExtension"

  override fun init() {
    initialized = true
    putExtensionHandlerMapping(
      MappingMode.O,
      injector.parser.parseKeys("<Plug>TestExtensionEmulateInclusive"),
      owner,
      MoveEmulateInclusive(),
      false
    )
    putExtensionHandlerMapping(
      MappingMode.O,
      injector.parser.parseKeys("<Plug>TestExtensionBackwardsCharacter"),
      owner,
      MoveBackwards(),
      false
    )
    putExtensionHandlerMapping(MappingMode.O, injector.parser.parseKeys("<Plug>TestExtensionCharacter"), owner, Move(), false)
    putExtensionHandlerMapping(MappingMode.O, injector.parser.parseKeys("<Plug>TestExtensionLinewise"), owner, MoveLinewise(), false)
    putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys("<Plug>TestMotion"), owner, MoveLinewiseInNormal(), false)
    putExtensionHandlerMapping(MappingMode.N, injector.parser.parseKeys("<Plug>TestMissing"), owner, MoveLinewiseInNormal(), false)
    putExtensionHandlerMapping(MappingMode.NO, injector.parser.parseKeys("<Plug>TestDelayed"), owner, DelayedAction(), false)
    putExtensionHandlerMapping(MappingMode.NO, injector.parser.parseKeys("<Plug>TestIncorrectDelayed"), owner, DelayedIncorrectAction(), false)

    putKeyMapping(MappingMode.O, injector.parser.parseKeys("U"), owner, injector.parser.parseKeys("<Plug>TestExtensionEmulateInclusive"), true)
    putKeyMapping(MappingMode.O, injector.parser.parseKeys("P"), owner, injector.parser.parseKeys("<Plug>TestExtensionBackwardsCharacter"), true)
    putKeyMapping(MappingMode.O, injector.parser.parseKeys("I"), owner, injector.parser.parseKeys("<Plug>TestExtensionCharacter"), true)
    putKeyMapping(MappingMode.O, injector.parser.parseKeys("O"), owner, injector.parser.parseKeys("<Plug>TestExtensionLinewise"), true)
    putKeyMapping(MappingMode.N, injector.parser.parseKeys("Q"), owner, injector.parser.parseKeys("<Plug>TestMotion"), true)
    putKeyMapping(MappingMode.NO, injector.parser.parseKeys("R"), owner, injector.parser.parseKeys("<Plug>TestDelayed"), true)
    putKeyMapping(MappingMode.NO, injector.parser.parseKeys("E"), owner, injector.parser.parseKeys("<Plug>TestIncorrectDelayed"), true)

    putKeyMappingIfMissing(MappingMode.N, injector.parser.parseKeys("Z"), owner, injector.parser.parseKeys("<Plug>TestMissing"), true)
    putKeyMappingIfMissing(MappingMode.I, injector.parser.parseKeys("L"), owner, injector.parser.parseKeys("<Plug>TestMissing"), true)
  }

  override fun dispose() {
    disposed = true
    super.dispose()
  }

  private class MoveEmulateInclusive : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      com.flop.idea.fim.FimPlugin.getVisualMotion().enterVisualMode(editor, FimStateMachine.SubMode.VISUAL_CHARACTER)
      val caret = editor.ij.caretModel.currentCaret
      val newOffset = com.flop.idea.fim.FimPlugin.getMotion().getOffsetOfHorizontalMotion(editor, caret.fim, 5, editor.isEndAllowed)
      com.flop.idea.fim.group.MotionGroup.moveCaret(editor.ij, caret, newOffset)
    }
  }

  private class MoveBackwards : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      editor.ij.caretModel.allCarets.forEach { it.moveToOffset(it.offset - 5) }
    }
  }

  private class Move : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      editor.ij.caretModel.allCarets.forEach { it.moveToOffset(it.offset + 5) }
    }
  }

  private class MoveLinewise : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      com.flop.idea.fim.FimPlugin.getVisualMotion().enterVisualMode(editor, FimStateMachine.SubMode.VISUAL_LINE)
      val caret = editor.ij.caretModel.currentCaret
      val newOffset = com.flop.idea.fim.FimPlugin.getMotion().getVerticalMotionOffset(editor, caret.fim, 1)
      com.flop.idea.fim.group.MotionGroup.moveCaret(editor.ij, caret, newOffset)
    }
  }

  private class MoveLinewiseInNormal : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      val caret = editor.ij.caretModel.currentCaret
      val newOffset = com.flop.idea.fim.FimPlugin.getMotion().getOffsetOfHorizontalMotion(editor, caret.fim, 1, true)
      com.flop.idea.fim.group.MotionGroup.moveCaret(editor.ij, caret, newOffset)
    }
  }

  private class DelayedAction : ExtensionHandler.WithCallback() {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      invokeLater {
        invokeLater {
          editor.ij.caretModel.allCarets.forEach { it.moveToOffset(it.offset + 5) }
          continueFimExecution()
        }
      }
    }
  }

  // This action should be registered with WithCallback, but we intentionally made it incorrectly for tests
  private class DelayedIncorrectAction : ExtensionHandler {
    override fun execute(editor: FimEditor, context: ExecutionContext) {
      invokeLater {
        invokeLater {
          editor.ij.caretModel.allCarets.forEach { it.moveToOffset(it.offset + 5) }
        }
      }
    }
  }

  companion object {
    fun createBean(): ExtensionBeanClass {
      val beanClass = ExtensionBeanClass()
      beanClass.implementation = TestExtension::class.java.canonicalName
      beanClass.aliases = listOf(Alias().also { it.name = "MyTest" })
      beanClass.pluginDescriptor = PluginManagerCore.getPlugin(com.flop.idea.fim.FimPlugin.getPluginId())!!
      return beanClass
    }
  }
}
