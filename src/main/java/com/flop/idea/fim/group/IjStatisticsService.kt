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

package com.flop.idea.fim.group

import com.flop.idea.fim.api.FimStatistics
import com.flop.idea.fim.statistic.ActionTracker
import com.flop.idea.fim.statistic.FimscriptState

class IjStatisticsService : FimStatistics {

  override fun logTrackedAction(actionId: String) {
    ActionTracker.logTrackedAction(actionId)
  }

  override fun logCopiedAction(actionId: String) {
    ActionTracker.logCopiedAction(actionId)
  }

  override fun setIfLoopUsed(value: Boolean) {
    FimscriptState.isLoopUsed = value
  }

  override fun setIfMapExprUsed(value: Boolean) {
    FimscriptState.isMapExprUsed = value
  }

  override fun setIfFunctionCallUsed(value: Boolean) {
    FimscriptState.isFunctionCallUsed = value
  }

  override fun setIfFunctionDeclarationUsed(value: Boolean) {
    FimscriptState.isFunctionDeclarationUsed = value
  }

  override fun setIfIfUsed(value: Boolean) {
    FimscriptState.isIfUsed = value
  }

  override fun addExtensionEnabledWithPlug(extension: String) {
    FimscriptState.extensionsEnabledWithPlug.add(extension)
  }

  override fun addSourcedFile(path: String) {
    FimscriptState.sourcedFiles.add(path)
  }
}
