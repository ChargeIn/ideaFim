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
package com.flop.idea.fim.extension

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.ExtensionPointListener
import com.intellij.openapi.extensions.PluginDescriptor
import com.flop.idea.fim.api.FimExtensionRegistrator
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.ex.ExException
import com.flop.idea.fim.key.MappingOwner.Plugin.Companion.remove
import com.flop.idea.fim.option.ToggleOption
import com.flop.idea.fim.options.OptionChangeListener
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.statistic.PluginState
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType

object FimExtensionRegistrar : FimExtensionRegistrator {
  internal val registeredExtensions: MutableSet<String> = HashSet()
  internal val extensionAliases = HashMap<String, String>()
  private var extensionRegistered = false
  private val logger = logger<FimExtensionRegistrar>()

  private val delayedExtensionEnabling = mutableListOf<ExtensionBeanClass>()

  @JvmStatic
  fun registerExtensions() {
    if (extensionRegistered) return
    extensionRegistered = true

    com.flop.idea.fim.extension.FimExtension.EP_NAME.extensions.forEach(this::registerExtension)

    com.flop.idea.fim.extension.FimExtension.EP_NAME.point.addExtensionPointListener(
      object : ExtensionPointListener<ExtensionBeanClass> {
        override fun extensionAdded(extension: ExtensionBeanClass, pluginDescriptor: PluginDescriptor) {
          registerExtension(extension)
        }

        override fun extensionRemoved(extension: ExtensionBeanClass, pluginDescriptor: PluginDescriptor) {
          unregisterExtension(extension)
        }
      },
      false, com.flop.idea.fim.FimPlugin.getInstance()
    )
  }

  @Synchronized
  private fun registerExtension(extensionBean: ExtensionBeanClass) {
    val name = extensionBean.name ?: extensionBean.instance.name
    if (name in registeredExtensions) return

    registeredExtensions.add(name)
    registerAliases(extensionBean)
    com.flop.idea.fim.FimPlugin.getOptionServiceImpl().addOption(ToggleOption(name, getAbbrev(name), false))
    com.flop.idea.fim.FimPlugin.getOptionService().addListener(
      name,
      object : OptionChangeListener<FimDataType> {
        override fun processGlobalValueChange(oldValue: FimDataType?) {
          if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, name)) {
            initExtension(extensionBean, name)
            PluginState.enabledExtensions.add(name)
          } else {
            extensionBean.instance.dispose()
          }
        }
      }
    )
  }

  private fun getAbbrev(name: String): String {
    return if (name == "NERDTree") "nerdtree" else name
  }

  private fun initExtension(extensionBean: ExtensionBeanClass, name: String) {
    if (injector.fimscriptExecutor.executingFimscript) {
      delayedExtensionEnabling += extensionBean
    } else {
      extensionBean.instance.init()
      logger.info("IdeaFim extension '$name' initialized")
    }
  }

  @JvmStatic
  fun enableDelayedExtensions() {
    delayedExtensionEnabling.forEach {
      it.instance.init()
      logger.info("IdeaFim extension '${it.name}' initialized")
    }
    delayedExtensionEnabling.clear()
  }

  @Synchronized
  private fun unregisterExtension(extension: ExtensionBeanClass) {
    val name = extension.name ?: extension.instance.name
    if (name !in registeredExtensions) return
    registeredExtensions.remove(name)
    removeAliases(extension)
    extension.instance.dispose()
    com.flop.idea.fim.FimPlugin.getOptionService().removeOption(name)
    remove(name)
    logger.info("IdeaFim extension '$name' disposed")
  }

  override fun setOptionByPluginAlias(alias: String): Boolean {
    val name = extensionAliases[alias] ?: return false
    try {
      com.flop.idea.fim.FimPlugin.getOptionService().setOption(OptionScope.GLOBAL, name)
    } catch (e: ExException) {
      return false
    }
    return true
  }

  override fun getExtensionNameByAlias(alias: String): String? {
    return extensionAliases[alias]
  }

  private fun registerAliases(extension: ExtensionBeanClass) {
    extension.aliases
      ?.mapNotNull { it.name }
      ?.forEach { alias -> extensionAliases[alias] = extension.name ?: extension.instance.name }
  }

  private fun removeAliases(extension: ExtensionBeanClass) {
    extension.aliases?.mapNotNull { it.name }?.forEach { extensionAliases.remove(it) }
  }
}
