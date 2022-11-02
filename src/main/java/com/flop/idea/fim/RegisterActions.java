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
package com.flop.idea.fim;

import com.flop.idea.fim.group.KeyGroup;
import com.flop.idea.fim.handler.ActionBeanClass;
import com.flop.idea.fim.handler.EditorActionHandlerBase;
import com.flop.idea.fim.key.MappingOwner;
import com.flop.idea.fim.newapi.IjFimActionsInitiator;
import com.intellij.openapi.extensions.ExtensionPointName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class RegisterActions {

  public static final ExtensionPointName<ActionBeanClass> FIM_ACTIONS_EP =
    ExtensionPointName.create("IdeaFIM.fimAction");

  /**
   * Register all the key/action mappings for the plugin.
   */
  public static void registerActions() {
    registerFimCommandActions();
    registerEmptyShortcuts();
    registerEpListener();
  }

  private static void registerEpListener() {
    // IdeaFim doesn't support contribution to VIM_ACTIONS_EP extension point, so technically we can skip this update,
    //   but let's support dynamic plugins in a more classic way and reload actions on every EP change.
    FIM_ACTIONS_EP.addChangeListener(() -> {
      unregisterActions();
      registerActions();
    }, FimPlugin.getInstance());
  }

  public static @Nullable EditorActionHandlerBase findAction(@NotNull String id) {
    return FIM_ACTIONS_EP.extensions().filter(fimActionBean -> fimActionBean.getActionId().equals(id)).findFirst()
      .map(ActionBeanClass::getInstance).orElse(null);
  }

  public static @NotNull EditorActionHandlerBase findActionOrDie(@NotNull String id) {
    EditorActionHandlerBase action = findAction(id);
    if (action == null) throw new RuntimeException("Action " + id + " is not registered");
    return action;
  }

  public static void unregisterActions() {
    KeyGroup keyGroup = FimPlugin.getKeyIfCreated();
    if (keyGroup != null) {
      keyGroup.unregisterCommandActions();
    }
  }

  private static void registerFimCommandActions() {
    KeyGroup parser = FimPlugin.getKey();
    FIM_ACTIONS_EP.extensions().map(IjFimActionsInitiator::new).forEach(parser::registerCommandAction);
  }

  private static void registerEmptyShortcuts() {
    final KeyGroup parser = FimPlugin.getKey();

    // The {char1} <BS> {char2} shortcut is handled directly by KeyHandler#handleKey, so doesn't have an action. But we
    // still need to register the shortcut, to make sure the editor doesn't swallow it.
    parser
      .registerShortcutWithoutAction(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), MappingOwner.IdeaFim.System.INSTANCE);
  }
}
