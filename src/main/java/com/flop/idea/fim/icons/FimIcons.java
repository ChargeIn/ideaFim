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

package com.flop.idea.fim.icons;

import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class FimIcons {
  public static final @NotNull Icon IDEAVIM = load("/icons/ideafim.svg");
  public static final @NotNull Icon IDEAVIM_DISABLED = load("/icons/ideafim_disabled.svg");
  public static final @NotNull Icon TWITTER = load("/icons/twitter.svg");
  public static final @NotNull Icon YOUTRACK = load("/icons/youtrack.svg");

  private static @NotNull Icon load(@NotNull @NonNls String path) {
    return IconManager.getInstance().getIcon(path, FimIcons.class);
  }
}
