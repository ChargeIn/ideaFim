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

import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimInt
import com.flop.idea.fim.fimscript.model.datatypes.FimString

/**
 * @author Alex Plate
 *
 * This test case helps you to test IdeaFim options
 *
 * While inheriting from this class you should specify (via constructor), which options you are going to test.
 *   After that each test method in this class should contains [FimOptionTestConfiguration] annotation with
 *   description of which values of option should be set before starting test.
 *
 * e.g.
 * ```
 * @FimOptionTestConfiguration(FimTestOption("keymodel", LIST, ["startsel"]), FimTestOption("selectmode", LIST, ["key"]))
 * ```
 *
 * If you want to keep default configuration, you can put [FimOptionDefaultAll] annotation
 */
abstract class FimOptionTestCase(option: String, vararg otherOptions: String) : FimTestCase() {
  private val options: Set<String> = setOf(option, *otherOptions)

  override fun setUp() {
    super.setUp()
    val testMethod = this.javaClass.getMethod(this.name)
    if (!testMethod.isAnnotationPresent(FimOptionDefaultAll::class.java)) {
      if (!testMethod.isAnnotationPresent(FimOptionTestConfiguration::class.java)) kotlin.test.fail("You should add FimOptionTestConfiguration with options for this method")

      val annotationValues = testMethod.getDeclaredAnnotation(FimOptionTestConfiguration::class.java) ?: run {
        kotlin.test.fail("You should have at least one FimOptionTestConfiguration annotation. Or you can use FimOptionDefaultAll")
      }
      val defaultOptions = testMethod.getDeclaredAnnotation(FimOptionDefault::class.java)?.values ?: emptyArray()

      val annotationsValueList = annotationValues.value.map { it.optionName } + defaultOptions
      val annotationsValueSet = annotationsValueList.toSet()
      if (annotationsValueSet.size < annotationsValueList.size) kotlin.test.fail("You have duplicated options")
      if (annotationsValueSet != options) kotlin.test.fail("You should present all options in annotations")

      annotationValues.value.forEach {
        when (it.valueType) {
          OptionValueType.STRING -> com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, it.optionName, FimString(it.value))
          OptionValueType.NUMBER -> com.flop.idea.fim.FimPlugin.getOptionService().setOptionValue(OptionScope.GLOBAL, it.optionName, FimInt(it.value))
        }
      }
    }
  }
}

@Target(AnnotationTarget.FUNCTION)
annotation class FimOptionDefaultAll

@Target(AnnotationTarget.FUNCTION)
annotation class FimOptionDefault(vararg val values: String)

@Target(AnnotationTarget.FUNCTION)
annotation class FimOptionTestConfiguration(vararg val value: FimTestOption)

@Target(AnnotationTarget.PROPERTY)
annotation class FimTestOption(
  val optionName: String,
  val valueType: OptionValueType,
  val value: String,
)

enum class OptionValueType {
  STRING,
  NUMBER,
}
