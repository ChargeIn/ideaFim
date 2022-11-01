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

@file:Suppress("RemoveCurlyBracesFromTemplate")

package org.jetbrains.plugins.ideafim.group.visual

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.ide.DataManager
import com.intellij.injected.editor.EditorWindow
import com.intellij.openapi.editor.Editor
import com.intellij.refactoring.rename.inplace.VariableInplaceRenameHandler
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestUtil.doInlineRename
import com.flop.idea.fim.FimPlugin
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.helper.inInsertMode
import com.flop.idea.fim.helper.inNormalMode
import com.flop.idea.fim.helper.inSelectMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.listener.FimListenerManager
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString
import com.flop.idea.fim.fimscript.services.IjFimOptionService
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimOptionDefaultAll
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption
import org.jetbrains.plugins.ideafim.assertDoesntChange
import org.jetbrains.plugins.ideafim.waitAndAssertMode

/**
 * @author Alex Plate
 */
class TemplateTest : FimOptionTestCase(IjFimOptionService.idearefactormodeName) {

  override fun setUp() {
    super.setUp()
    TemplateManagerImpl.setTemplateTesting(myFixture.testRootDisposable)
  }

  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  @FimOptionDefaultAll
  fun `test simple rename`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    doInlineRename(VariableInplaceRenameHandler(), "myNewVar", myFixture)
    assertState(
      """
            class Hello {
                public static void main() {
                    int my${c}NewVar = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test type rename`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    typeText(injector.parser.parseKeys("myNewVar" + "<CR>"))

    assertState(FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int myNewVar${c} = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test selectmode without template`() {
    com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(
      OptionScope.GLOBAL, IjFimOptionService.idearefactormodeName,
      FimString(
        IjFimOptionService.idearefactormode_visual
      )
    )
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertState(FimStateMachine.Mode.VISUAL, FimStateMachine.SubMode.VISUAL_CHARACTER)
    // Disable template
    typeText(injector.parser.parseKeys("<CR>"))
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test prepend`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    LookupManager.hideActiveLookup(myFixture.project)
    typeText(injector.parser.parseKeys("<Left>"))
    assertState(FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    typeText(injector.parser.parseKeys("pre" + "<CR>"))

    assertState(FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int pre${c}myVar = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test motion right`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    LookupManager.hideActiveLookup(myFixture.project)
    typeText(injector.parser.parseKeys("<Right>"))
    assertState(FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int myVar${c} = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test motion left on age`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int ${c}myVar = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    LookupManager.hideActiveLookup(myFixture.project)
    typeText(injector.parser.parseKeys("<Left>"))
    assertState(FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int ${c}myVar = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test motion right on age`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int myVa${c}r = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    LookupManager.hideActiveLookup(myFixture.project)
    typeText(injector.parser.parseKeys("<Right>"))
    assertState(FimStateMachine.Mode.INSERT, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int myVar${c} = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test escape`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    typeText(injector.parser.parseKeys("<ESC>"))

    assertState(FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test escape after typing`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
    assertState(FimStateMachine.Mode.SELECT, FimStateMachine.SubMode.VISUAL_CHARACTER)

    typeText(injector.parser.parseKeys("Hello" + "<ESC>"))

    assertState(FimStateMachine.Mode.COMMAND, FimStateMachine.SubMode.NONE)
    assertState(
      """
            class Hello {
                public static void main() {
                    int Hell${c}o = 5;
                }
            }
      """.trimIndent()
    )
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_keep))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template in normal mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inNormalMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_keep))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test save mode for insert mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("i"))
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inInsertMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_keep))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test save mode for visual mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("vll"))
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inVisualMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_select))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to select in normal mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_select))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to select in insert mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("i"))
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_select))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to select in visual mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("vll"))
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inVisualMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_select))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to select in select mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("vll<C-G>"))
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inSelectMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_visual))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to visual in normal mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_visual))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to visual in insert mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("i"))
    startRenaming(VariableInplaceRenameHandler())
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_visual))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to visual in visual mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("vll"))
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inVisualMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_visual))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template to visual in select mode`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("vll<C-G>"))
    startRenaming(VariableInplaceRenameHandler())
    assertDoesntChange { myFixture.editor.inSelectMode }
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_keep))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template with multiple times`() {
    configureByJavaText(c)
    val manager = TemplateManager.getInstance(myFixture.project)
    val template = manager.createTemplate("vn", "user", "\$V1$ var = \$V2$;")
    template.addVariable("V1", "", "\"123\"", true)
    template.addVariable("V2", "", "\"239\"", true)

    manager.startTemplate(myFixture.editor, template)
    PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue()

    assertMode(FimStateMachine.Mode.COMMAND)
    assertOffset(2)
    typeText(injector.parser.parseKeys("<CR>"))
    assertMode(FimStateMachine.Mode.COMMAND)
    assertOffset(12)
    typeText(injector.parser.parseKeys("<CR>"))
    assertNull(TemplateManagerImpl.getTemplateState(myFixture.editor))
  }

  @FimOptionTestConfiguration(FimTestOption(IjFimOptionService.idearefactormodeName, OptionValueType.STRING, IjFimOptionService.idearefactormode_keep))
  @TestWithoutNeofim(reason = SkipNeofimReason.TEMPLATES)
  fun `test template with lookup`() {
    configureByJavaText(
      """
            class Hello {
                public static void main() {
                    int my${c}Var = 5;
                }
            }
      """.trimIndent()
    )
    startRenaming(VariableInplaceRenameHandler())
    val lookupValue = myFixture.lookupElementStrings?.get(0) ?: kotlin.test.fail()
    myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
    assertState(
      """
            class Hello {
                public static void main() {
                    int $lookupValue = 5;
                }
            }
      """.trimIndent()
    )
  }

  private fun startRenaming(handler: VariableInplaceRenameHandler): Editor {
    val editor = if (myFixture.editor is EditorWindow) (myFixture.editor as EditorWindow).delegate else myFixture.editor
    FimListenerManager.EditorListeners.add(editor)

    handler.doRename(myFixture.elementAtCaret, editor, dataContext)
    return editor
  }

  private val dataContext
    get() = DataManager.getInstance().getDataContext(myFixture.editor.component)
}
