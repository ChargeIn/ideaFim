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

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.group.visual.IdeaSelectionControl
import com.flop.idea.fim.helper.FimBehaviorDiffers
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.listener.FimListenerManager
import com.flop.idea.fim.options.OptionConstants
import org.jetbrains.plugins.ideafim.OptionValueType
import org.jetbrains.plugins.ideafim.SkipNeofimReason
import org.jetbrains.plugins.ideafim.TestWithoutNeofim
import org.jetbrains.plugins.ideafim.FimOptionDefaultAll
import org.jetbrains.plugins.ideafim.FimOptionTestCase
import org.jetbrains.plugins.ideafim.FimOptionTestConfiguration
import org.jetbrains.plugins.ideafim.FimTestOption
import org.jetbrains.plugins.ideafim.waitAndAssert
import org.jetbrains.plugins.ideafim.waitAndAssertMode

/**
 * @author Alex Plate
 */
class IdeaVisualControlTest : FimOptionTestCase(OptionConstants.selectmodeName) {
  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection no selection`() {
    configureByText(
      """
            A Discovery

            I $s$c${se}found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    assertMode(FimStateMachine.Mode.COMMAND)
    assertSubMode(FimStateMachine.SubMode.NONE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection cursor in the middle`() {
    configureByText(
      """
            A Discovery

            I ${s}found$c it$se in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I ${s}found ${c}i${se}t in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimBehaviorDiffers(
    originalFimAfter = """
            A Discovery

            I ${s}found i${c}t$se in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
    """
  )
  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection cursor on end`() {
    configureByText(
      """
            A Discovery

            I ${s}found it$c$se in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I ${s}found it ${c}i${se}n a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection cursor on start`() {
    configureByText(
      """
            A Discovery

            I $s${c}found it$se in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I f${s}${c}ound it$se in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection lineend`() {
    configureByText(
      """
            A Discovery

            I ${s}found ${c}it in a legendary land$se
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I ${s}found i${c}t${se} in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection next line`() {
    configureByText(
      """
            A Discovery

            I ${s}found ${c}it in a legendary land
            ${se}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I ${s}found i${c}t${se} in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection start on line start`() {
    configureByText(
      """
            A Discovery

            ${s}I found ${c}it ${se}in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            ${s}I found i${c}t${se} in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection start on line end`() {
    configureByText(
      """
            A Discovery
            $s
            I found ${c}it ${se}in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery
            $s
            I found i${c}t${se} in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection multicaret`() {
    configureByText(
      """
            A Discovery
            $s
            I found ${c}it ${se}in a legendary land
            all rocks $s$c${se}and lavender and tufted grass,
            where it was $s${c}settled$se on some sodden sand
            hard by the torrent of a mountain ${s}pass.$c$se
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery
            $s
            I found i${c}t${se} in a legendary land
            all rocks ${s}a${c}n${se}d lavender and tufted grass,
            where it was s$s${c}ettled$se on some sodden sand
            hard by the torrent of a mountain ${s}pass.$c$se
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable line selection`() {
    configureByText(
      """
            A Discovery

            ${s}I found ${c}it in a legendary land$se
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            ${s}I found i${c}t in a legendary land
            ${se}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("j"))
    assertState(
      """
            A Discovery

            ${s}I found it in a legendary land
            all rocks$c and lavender and tufted grass,
            ${se}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable line selection next line`() {
    configureByText(
      """
            A Discovery

            ${s}I found ${c}it in a legendary land
            ${se}all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("j"))
    assertState(
      """
            A Discovery

            ${s}I found it in a legendary land
            all rock${c}s and lavender and tufted grass,
            ${se}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable line selection cursor on last line`() {
    configureByText(
      """
            A Discovery

            ${s}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled ${c}on some sodden sand$se
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("j"))
    assertState(
      """
            A Discovery

            ${s}I found it in a legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand
            hard by the torrent o${c}f a mountain pass.$se
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable line selection cursor on first line`() {
    configureByText(
      """
            A Discovery

            ${s}I found it in a ${c}legendary land
            all rocks and lavender and tufted grass,
            where it was settled on some sodden sand$se
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("j"))
    assertState(
      """
            A Discovery

            I found it in a legendary land
            ${s}all rocks and la${c}vender and tufted grass,
            where it was settled on some sodden sand
            ${se}hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable line selection multicaret`() {
    configureByText(
      """
            A Discovery

            ${s}I found it in a ${c}legendary land$se
            all rocks and lavender and tufted grass,
            ${s}where it was settled ${c}on some sodden sand
            hard by the torrent of a mountain pass.$se
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("j"))
    assertState(
      """
            A Discovery

            ${s}I found it in a legendary land
            all rocks and la${c}vender and tufted grass,
            ${se}where it was settled on some sodden sand
            ${s}hard by the torrent o${c}f a mountain pass.$se
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable line selection motion up`() {
    configureByText(
      """
            A Discovery

            I found it in a legendary land
            ${s}all rocks and lavender ${c}and tufted grass,$se
            where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("k"))
    assertState(
      """
            A Discovery

            ${s}I found it in a legenda${c}ry land
            all rocks and lavender and tufted grass,
            ${se}where it was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection looks like block`() {
    configureByText(
      """
            A Discovery

            I ${s}found$c$se it in a legendary land
            al${s}l roc$c${se}ks and lavender and tufted grass,
            wh${s}ere i$c${se}t was settled on some sodden sand
            ha${s}rd by $c${se}the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection`() {
    configureByText(
      """
            A Discovery

            I ${s}found$c$se it in a legendary land
            al${s}l roc$c${se}ks and lavender and tufted grass,
            wh${s}ere i$c${se}t was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_BLOCK)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I ${s}found ${c}i${se}t in a legendary land
            al${s}l rock${c}s${se} and lavender and tufted grass,
            wh${s}ere it${c} ${se}was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_BLOCK)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection with longer line`() {
    configureByText(
      """
            A Discovery

            I ${s}found it in a legendary land$c$se
            al${s}l rocks and lavender and tufted grass,$c$se
            wh${s}ere it was settled on some sodden sand$c$se
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_BLOCK)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("j"))
    assertState(
      """
            A Discovery

            I ${s}found it in a legendary lan${c}d$se
            al${s}l rocks and lavender and tufted gras${c}s${se},
            wh${s}ere it was settled on some sodden sa${c}n${se}d
            ha${s}rd by the torrent of a mountain pass.${c}$se
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_BLOCK)
    assertCaretsVisualAttributes()
  }

  @FimOptionDefaultAll
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test enable character selection caret to the left`() {
    configureByText(
      """
            A Discovery

            I $s${c}found$se it in a legendary land
            al$s${c}l roc${se}ks and lavender and tufted grass,
            wh$s${c}ere i${se}t was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.COMMAND)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)
    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_BLOCK)
    assertCaretsVisualAttributes()

    typeText(injector.parser.parseKeys("l"))
    assertState(
      """
            A Discovery

            I f$s${c}ound$se it in a legendary land
            all$s$c roc${se}ks and lavender and tufted grass,
            whe$s${c}re i${se}t was settled on some sodden sand
            hard by the torrent of a mountain pass.
      """.trimIndent()
    )
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_BLOCK)
    assertCaretsVisualAttributes()
  }

  @FimOptionTestConfiguration(
    FimTestOption(
      OptionConstants.selectmodeName,
      OptionValueType.STRING,
      OptionConstants.selectmode_ideaselection
    )
  )
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test control selection`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )
    FimListenerManager.EditorListeners.addAll()
    assertMode(FimStateMachine.Mode.COMMAND)

    myFixture.editor.selectionModel.setSelection(5, 10)

    waitAndAssertMode(myFixture, FimStateMachine.Mode.SELECT)
  }

  @FimOptionTestConfiguration(FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, ""))
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test control selection to visual mode`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )
    FimListenerManager.EditorListeners.addAll()
    assertMode(FimStateMachine.Mode.COMMAND)

    myFixture.editor.selectionModel.setSelection(5, 10)

    waitAndAssertMode(myFixture, FimStateMachine.Mode.VISUAL)
  }

  @FimOptionTestConfiguration(FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, ""))
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test control selection from line to char visual modes`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )
    typeText(injector.parser.parseKeys("V"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)

    myFixture.editor.selectionModel.setSelection(2, 5)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)

    waitAndAssert { myFixture.editor.subMode == FimStateMachine.SubMode.VISUAL_CHARACTER }
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  @FimOptionTestConfiguration(FimTestOption(OptionConstants.selectmodeName, OptionValueType.STRING, ""))
  @TestWithoutNeofim(reason = SkipNeofimReason.NOT_VIM_TESTING)
  fun `test control selection from line to char visual modes in keep mode`() {
    configureByText(
      """
            A Discovery

            I ${c}found it in a legendary land
            all rocks and lavender and tufted grass,
      """.trimIndent()
    )

    startDummyTemplate()

    typeText(injector.parser.parseKeys("V"))
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_LINE)

    myFixture.editor.selectionModel.setSelection(2, 5)
    IdeaSelectionControl.controlNonFimSelectionChange(myFixture.editor)

    waitAndAssert { myFixture.editor.subMode == FimStateMachine.SubMode.VISUAL_CHARACTER }
    assertMode(FimStateMachine.Mode.VISUAL)
    assertSubMode(FimStateMachine.SubMode.VISUAL_CHARACTER)
    assertCaretsVisualAttributes()
  }

  private fun startDummyTemplate() {
    TemplateManagerImpl.setTemplateTesting(myFixture.testRootDisposable)
    val templateManager = TemplateManager.getInstance(myFixture.project)
    val createdTemplate = templateManager.createTemplate("", "")
    createdTemplate.addVariable(ConstantNode("1"), true)
    templateManager.startTemplate(myFixture.editor, createdTemplate)
  }
}
