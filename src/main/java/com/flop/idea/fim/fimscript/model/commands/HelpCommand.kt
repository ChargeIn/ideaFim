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

package com.flop.idea.fim.fimscript.model.commands

import com.intellij.ide.BrowserUtil
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.command.OperatorArguments
import com.flop.idea.fim.ex.ranges.Ranges
import com.flop.idea.fim.fimscript.model.ExecutionResult
import org.jetbrains.annotations.NonNls
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

/**
 * @author vlan
 * see "h :help"
 */
data class HelpCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_OPTIONAL, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)
  override fun processCommand(editor: FimEditor, context: ExecutionContext, operatorArguments: OperatorArguments): ExecutionResult {
    BrowserUtil.browse(helpTopicUrl(argument))
    return ExecutionResult.Success
  }

  @NonNls
  private fun helpTopicUrl(topic: String): String {
    if (topic.isBlank()) return HELP_ROOT_URL

    return try {
      String.format("%s?docs=help&search=%s", HELP_QUERY_URL, URLEncoder.encode(topic, "UTF-8"))
    } catch (e: UnsupportedEncodingException) {
      HELP_ROOT_URL
    }
  }

  companion object {
    private const val HELP_BASE_URL = "http://fimdoc.sourceforge.net"
    private const val HELP_ROOT_URL = "$HELP_BASE_URL/htmldoc/"
    private const val HELP_QUERY_URL = "$HELP_BASE_URL/search.php"
  }
}
