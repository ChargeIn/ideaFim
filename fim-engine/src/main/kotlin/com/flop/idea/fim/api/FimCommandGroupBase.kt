package com.flop.idea.fim.api

import com.flop.idea.fim.api.FimCommandGroup.Companion.BLACKLISTED_ALIASES
import com.flop.idea.fim.common.CommandAlias
import com.flop.idea.fim.common.GoalCommand

abstract class FimCommandGroupBase : FimCommandGroup {
  companion object {
    private val overridePrefix = "!"
  }
  private var aliases = HashMap<String, CommandAlias>()

  override fun isAlias(command: String): Boolean {
    val name = this.getAliasName(command)
    // If the first letter is not uppercase then it cannot be an alias
    // and reject immediately.
    if (name.isEmpty() || !name[0].isUpperCase()) {
      return false
    }

    // If the input is blacklisted, then it is not an alias.
    if (name in BLACKLISTED_ALIASES) {
      return false
    }

    return this.hasAlias(name)
  }

  override fun hasAlias(name: String): Boolean {
    return name in this.aliases
  }

  private fun getAlias(name: String): CommandAlias {
    return this.aliases[name]!!
  }

  override fun getAliasCommand(command: String, count: Int): GoalCommand {
    return this.getAlias(this.getAliasName(command)).getCommand(command, count)
  }

  override fun setAlias(name: String, commandAlias: CommandAlias) {
    this.aliases[name] = commandAlias
  }

  override fun removeAlias(name: String) {
    this.aliases.remove(name)
  }

  override fun listAliases(): Set<Map.Entry<String, CommandAlias>> {
    return this.aliases.entries
  }

  override fun resetAliases() {
    this.aliases.clear()
  }

  private fun getAliasName(command: String): String {
    val items = command.split(" ")
    if (items.count() > 1) {
      return items[0].removeSuffix(overridePrefix)
    }
    return command.removeSuffix(overridePrefix)
  }
}
