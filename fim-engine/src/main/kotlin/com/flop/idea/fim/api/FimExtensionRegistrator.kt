package com.flop.idea.fim.api

interface FimExtensionRegistrator {
  fun setOptionByPluginAlias(alias: String): Boolean
  fun getExtensionNameByAlias(alias: String): String?
}
