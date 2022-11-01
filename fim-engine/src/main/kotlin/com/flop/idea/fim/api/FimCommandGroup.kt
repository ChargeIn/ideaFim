package com.flop.idea.fim.api

import com.flop.idea.fim.common.CommandAlias
import com.flop.idea.fim.common.GoalCommand
import org.jetbrains.annotations.NonNls

interface FimCommandGroup {
  fun isAlias(command: String): Boolean
  fun hasAlias(name: String): Boolean
  fun getAliasCommand(command: String, count: Int): GoalCommand
  fun setAlias(name: String, commandAlias: CommandAlias)
  fun removeAlias(name: String)
  fun listAliases(): Set<Map.Entry<String, CommandAlias>>
  fun resetAliases()

  companion object {
    @NonNls
    val BLACKLISTED_ALIASES = arrayOf("X", "Next", "Print")
  }
}
