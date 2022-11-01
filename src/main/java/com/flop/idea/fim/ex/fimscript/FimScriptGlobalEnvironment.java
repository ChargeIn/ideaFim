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

package com.flop.idea.fim.ex.fimscript;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vlan
 */

@Deprecated() // please use VariableService instead
@ApiStatus.ScheduledForRemoval(inVersion = "1.12")
public class FimScriptGlobalEnvironment {
  private static final FimScriptGlobalEnvironment ourInstance = new FimScriptGlobalEnvironment();

  private final Map<String, Object> myVariables = new HashMap<>();

  private FimScriptGlobalEnvironment() {
  }

  public static @NotNull FimScriptGlobalEnvironment getInstance() {
    return ourInstance;
  }

  public @NotNull Map<String, Object> getVariables() {
    return myVariables;
  }
}
