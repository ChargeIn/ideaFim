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

package com.flop.idea.fim.group;

import com.flop.idea.fim.action.ComplicatedKeysAction;
import com.flop.idea.fim.action.FimShortcutKeyAction;
import com.flop.idea.fim.api.NativeAction;
import com.flop.idea.fim.api.FimActionsInitiator;
import com.flop.idea.fim.api.FimEditor;
import com.flop.idea.fim.api.FimKeyGroupBase;
import com.flop.idea.fim.command.MappingMode;
import com.flop.idea.fim.handler.EditorActionHandlerBase;
import com.flop.idea.fim.key.*;
import com.flop.idea.fim.newapi.IjNativeAction;
import com.flop.idea.fim.newapi.IjFimActionsInitiator;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.google.common.collect.ImmutableList;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.ex.KeymapManagerEx;
import com.flop.idea.fim.EventFacade;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.action.ComplicatedKeysAction;
import com.flop.idea.fim.action.FimShortcutKeyAction;
import com.flop.idea.fim.api.*;
import com.flop.idea.fim.command.MappingMode;
import com.flop.idea.fim.key.Node;
import com.flop.idea.fim.ex.ExOutputModel;
import com.flop.idea.fim.handler.EditorActionHandlerBase;
import com.flop.idea.fim.helper.HelperKt;
import com.flop.idea.fim.key.*;
import com.flop.idea.fim.newapi.IjNativeAction;
import com.flop.idea.fim.newapi.IjFimActionsInitiator;
import com.flop.idea.fim.newapi.IjFimEditor;
import kotlin.Pair;
import kotlin.text.StringsKt;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * @author vlan
 */
@State(name = "FimKeySettings", storages = {@Storage(value = "$APP_CONFIG$/fim_settings.xml")})
public class KeyGroup extends FimKeyGroupBase implements PersistentStateComponent<Element>{
  public static final @NonNls String SHORTCUT_CONFLICTS_ELEMENT = "shortcut-conflicts";
  private static final @NonNls String SHORTCUT_CONFLICT_ELEMENT = "shortcut-conflict";
  private static final @NonNls String OWNER_ATTRIBUTE = "owner";
  private static final String TEXT_ELEMENT = "text";

  private static final Logger logger = Logger.getInstance(KeyGroup.class);

  public void registerRequiredShortcutKeys(@NotNull FimEditor editor) {
    EventFacade.getInstance()
      .registerCustomShortcutSet(FimShortcutKeyAction.getInstance(), toShortcutSet(getRequiredShortcutKeys()),
                                 ((IjFimEditor)editor).getEditor().getComponent());
  }

  public void registerShortcutsForLookup(@NotNull LookupImpl lookup) {
    EventFacade.getInstance()
      .registerCustomShortcutSet(FimShortcutKeyAction.getInstance(), toShortcutSet(getRequiredShortcutKeys()),
                                 lookup.getComponent(), lookup);
  }

  void unregisterShortcutKeys(@NotNull FimEditor editor) {
    EventFacade.getInstance().unregisterCustomShortcutSet(FimShortcutKeyAction.getInstance(),
                                                          ((IjFimEditor)editor).getEditor().getComponent());
  }

  public boolean showKeyMappings(@NotNull Set<? extends MappingMode> modes, @NotNull Editor editor) {
    List<Pair<EnumSet<MappingMode>, MappingInfo>> rows = getKeyMappingRows(modes);
    final StringBuilder builder = new StringBuilder();
    for (Pair<EnumSet<MappingMode>, MappingInfo> row : rows) {
      MappingInfo mappingInfo = row.getSecond();
      builder.append(StringsKt.padEnd(getModesStringCode(row.getFirst()), 2, ' '));
      builder.append(" ");
      builder.append(StringsKt.padEnd(FimInjectorKt.getInjector().getParser().toKeyNotation(mappingInfo.getFromKeys()), 11, ' '));
      builder.append(" ");
      builder.append(mappingInfo.isRecursive() ? " " : "*");
      builder.append(" ");
      builder.append(mappingInfo.getPresentableString());
      builder.append("\n");
    }
    ExOutputModel.getInstance(editor).output(builder.toString());
    return true;
  }


  @Override
  public void updateShortcutKeysRegistration() {
    for (Editor editor : HelperKt.localEditors()) {
      unregisterShortcutKeys(new IjFimEditor(editor));
      registerRequiredShortcutKeys(new IjFimEditor(editor));
    }
  }

  public void saveData(@NotNull Element element) {
    final Element conflictsElement = new Element(SHORTCUT_CONFLICTS_ELEMENT);
    for (Map.Entry<KeyStroke, ShortcutOwnerInfo> entry : myShortcutConflicts.entrySet()) {
      final ShortcutOwner owner;
      ShortcutOwnerInfo myValue = entry.getValue();
      if (myValue instanceof ShortcutOwnerInfo.AllModes) {
        owner = ((ShortcutOwnerInfo.AllModes)myValue).getOwner();
      }
      else if (myValue instanceof ShortcutOwnerInfo.PerMode) {
        owner = null;
      }
      else {
        throw new RuntimeException();
      }
      if (owner != null && owner != ShortcutOwner.UNDEFINED) {
        final Element conflictElement = new Element(SHORTCUT_CONFLICT_ELEMENT);
        conflictElement.setAttribute(OWNER_ATTRIBUTE, owner.getOwnerName());
        final Element textElement = new Element(TEXT_ELEMENT);
        FimPlugin.getXML().setSafeXmlText(textElement, entry.getKey().toString());
        conflictElement.addContent(textElement);
        conflictsElement.addContent(conflictElement);
      }
    }
    element.addContent(conflictsElement);
  }

  public void readData(@NotNull Element element) {
    final Element conflictsElement = element.getChild(SHORTCUT_CONFLICTS_ELEMENT);
    if (conflictsElement != null) {
      final java.util.List<Element> conflictElements = conflictsElement.getChildren(SHORTCUT_CONFLICT_ELEMENT);
      for (Element conflictElement : conflictElements) {
        final String ownerValue = conflictElement.getAttributeValue(OWNER_ATTRIBUTE);
        ShortcutOwner owner = ShortcutOwner.UNDEFINED;
        try {
          owner = ShortcutOwner.fromString(ownerValue);
        }
        catch (IllegalArgumentException ignored) {
        }
        final Element textElement = conflictElement.getChild(TEXT_ELEMENT);
        if (textElement != null) {
          final String text = FimPlugin.getXML().getSafeXmlText(textElement);
          if (text != null) {
            final KeyStroke keyStroke = KeyStroke.getKeyStroke(text);
            if (keyStroke != null) {
              myShortcutConflicts.put(keyStroke, new ShortcutOwnerInfo.AllModes(owner));
            }
          }
        }
      }
    }
  }

  @Override
  public @NotNull List<NativeAction> getKeymapConflicts(@NotNull KeyStroke keyStroke) {
    final KeymapManagerEx keymapManager = KeymapManagerEx.getInstanceEx();
    final Keymap keymap = keymapManager.getActiveKeymap();
    final KeyboardShortcut shortcut = new KeyboardShortcut(keyStroke, null);
    final Map<String, ? extends List<KeyboardShortcut>> conflicts = keymap.getConflicts("", shortcut);
    final List<AnAction> actions = new ArrayList<>();
    for (String actionId : conflicts.keySet()) {
      final AnAction action = ActionManagerEx.getInstanceEx().getAction(actionId);
      if (action != null) {
        actions.add(action);
      }
    }
    return actions.stream().map(IjNativeAction::new).collect(toList());
  }

  public @NotNull Map<KeyStroke, ShortcutOwnerInfo> getShortcutConflicts() {
    final Set<RequiredShortcut> requiredShortcutKeys = this.getRequiredShortcutKeys();
    final Map<KeyStroke, ShortcutOwnerInfo> savedConflicts = getSavedShortcutConflicts();
    final Map<KeyStroke, ShortcutOwnerInfo> results = new HashMap<>();
    for (RequiredShortcut requiredShortcut : requiredShortcutKeys) {
      KeyStroke keyStroke = requiredShortcut.getKeyStroke();
      if (!FimShortcutKeyAction.VIM_ONLY_EDITOR_KEYS.contains(keyStroke)) {
        final List<NativeAction> conflicts = getKeymapConflicts(keyStroke);
        if (!conflicts.isEmpty()) {
          ShortcutOwnerInfo owner = savedConflicts.get(keyStroke);
          if (owner == null) {
            owner = ShortcutOwnerInfo.allUndefined;
          }
          results.put(keyStroke, owner);
        }
      }
    }
    return results;
  }

  /**
   * Registers a shortcut that is handled by KeyHandler#handleKey directly, rather than by an action
   *
   * <p>
   * Digraphs are handled directly by KeyHandler#handleKey instead of via an action, but we need to still make sure the
   * shortcuts are registered, or the key handler won't see them
   * </p>
   *
   * @param keyStroke The shortcut to register
   */
  public void registerShortcutWithoutAction(KeyStroke keyStroke, MappingOwner owner) {
    registerRequiredShortcut(Collections.singletonList(keyStroke), owner);
  }

  public void registerCommandAction(@NotNull FimActionsInitiator actionHolder) {
    IjFimActionsInitiator holder = (IjFimActionsInitiator)actionHolder;

    if (!FimPlugin.getPluginId().equals(holder.getBean().getPluginDescriptor().getPluginId())) {
      logger.error("IdeaFim doesn't accept contributions to `fimActions` extension points. " +
                   "Please create a plugin using `FimExtension`. " +
                   "Plugin to blame: " +
                   holder.getBean().getPluginDescriptor().getPluginId());
      return;
    }

    Set<List<KeyStroke>> actionKeys = holder.getBean().getParsedKeys();
    if (actionKeys == null) {
      final EditorActionHandlerBase action = actionHolder.getInstance();
      if (action instanceof ComplicatedKeysAction) {
        actionKeys = ((ComplicatedKeysAction)action).getKeyStrokesSet();
      }
      else {
        throw new RuntimeException("Cannot register action: " + action.getClass().getName());
      }
    }

    Set<MappingMode> actionModes = holder.getBean().getParsedModes();
    if (actionModes == null) {
      throw new RuntimeException("Cannot register action: " + holder.getBean().getImplementation());
    }

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      initIdentityChecker();
      for (List<KeyStroke> keys : actionKeys) {
        checkCommand(actionModes, actionHolder.getInstance(), keys);
      }
    }

    for (List<KeyStroke> keyStrokes : actionKeys) {
      registerRequiredShortcut(keyStrokes, MappingOwner.IdeaFim.System.INSTANCE);

      for (MappingMode mappingMode : actionModes) {
        Node<FimActionsInitiator> node = getKeyRoot(mappingMode);
        NodesKt.addLeafs(node, keyStrokes, actionHolder);
      }
    }
  }

  private void registerRequiredShortcut(@NotNull List<KeyStroke> keys, MappingOwner owner) {
    for (KeyStroke key : keys) {
      if (key.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
        getRequiredShortcutKeys().add(new RequiredShortcut(key, owner));
      }
    }
  }

  public static @NotNull ShortcutSet toShortcutSet(@NotNull Collection<RequiredShortcut> requiredShortcuts) {
    final List<Shortcut> shortcuts = new ArrayList<>();
    for (RequiredShortcut key : requiredShortcuts) {
      shortcuts.add(new KeyboardShortcut(key.getKeyStroke(), null));
    }
    return new CustomShortcutSet(shortcuts.toArray(new Shortcut[0]));
  }

  private static @NotNull List<Pair<EnumSet<MappingMode>, MappingInfo>> getKeyMappingRows(@NotNull Set<? extends MappingMode> modes) {
    final Map<ImmutableList<KeyStroke>, EnumSet<MappingMode>> actualModes = new HashMap<>();
    for (MappingMode mode : modes) {
      final KeyMapping mapping = FimPlugin.getKey().getKeyMapping(mode);
      for (List<? extends KeyStroke> fromKeys : mapping) {
        final ImmutableList<KeyStroke> key = ImmutableList.copyOf(fromKeys);
        final EnumSet<MappingMode> value = actualModes.get(key);
        final EnumSet<MappingMode> newValue;
        if (value != null) {
          newValue = value.clone();
          newValue.add(mode);
        }
        else {
          newValue = EnumSet.of(mode);
        }
        actualModes.put(key, newValue);
      }
    }
    final List<Pair<EnumSet<MappingMode>, MappingInfo>> rows = new ArrayList<>();
    for (Map.Entry<ImmutableList<KeyStroke>, EnumSet<MappingMode>> entry : actualModes.entrySet()) {
      final ArrayList<KeyStroke> fromKeys = new ArrayList<>(entry.getKey());
      final EnumSet<MappingMode> mappingModes = entry.getValue();
      if (!mappingModes.isEmpty()) {
        final MappingMode mode = mappingModes.iterator().next();
        final KeyMapping mapping = FimPlugin.getKey().getKeyMapping(mode);
        final MappingInfo mappingInfo = mapping.get(fromKeys);
        if (mappingInfo != null) {
          rows.add(new Pair<>(mappingModes, mappingInfo));
        }
      }
    }
    rows.sort(Comparator.comparing(Pair<EnumSet<MappingMode>, MappingInfo>::getSecond));
    return rows;
  }

  private static @NotNull @NonNls String getModesStringCode(@NotNull Set<MappingMode> modes) {
    if (modes.equals(MappingMode.NVO)) {
      return "";
    }
    else if (modes.contains(MappingMode.INSERT)) {
      return "i";
    }
    else if (modes.contains(MappingMode.NORMAL)) {
      return "n";
    }
    // TODO: Add more codes
    return "";
  }

  private @NotNull List<AnAction> getActions(@NotNull Component component, @NotNull KeyStroke keyStroke) {
    final List<AnAction> results = new ArrayList<>();
    results.addAll(getLocalActions(component, keyStroke));
    results.addAll(getKeymapActions(keyStroke));
    return results;
  }

  @Override
  public @NotNull List<NativeAction> getActions(@NotNull FimEditor editor, @NotNull KeyStroke keyStroke) {
    return getActions(((IjFimEditor)editor).getEditor().getComponent(), keyStroke).stream()
      .map(IjNativeAction::new).collect(toList());
  }

  private static @NotNull List<AnAction> getLocalActions(@NotNull Component component, @NotNull KeyStroke keyStroke) {
    final List<AnAction> results = new ArrayList<>();
    final KeyboardShortcut keyStrokeShortcut = new KeyboardShortcut(keyStroke, null);
    for (Component c = component; c != null; c = c.getParent()) {
      if (c instanceof JComponent) {
        final List<AnAction> actions = ActionUtil.getActions((JComponent)c);
        for (AnAction action : actions) {
          if (action instanceof FimShortcutKeyAction) {
            continue;
          }
          final Shortcut[] shortcuts = action.getShortcutSet().getShortcuts();
          for (Shortcut shortcut : shortcuts) {
            if (shortcut.isKeyboard() && shortcut.startsWith(keyStrokeShortcut) && !results.contains(action)) {
              results.add(action);
            }
          }
        }
      }
    }
    return results;
  }

  private static @NotNull List<AnAction> getKeymapActions(@NotNull KeyStroke keyStroke) {
    final List<AnAction> results = new ArrayList<>();
    final Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
    for (String id : keymap.getActionIds(keyStroke)) {
      final AnAction action = ActionManager.getInstance().getAction(id);
      if (action != null) {
        results.add(action);
      }
    }
    return results;
  }

  @Nullable
  @Override
  public Element getState() {
    @NonNls Element element = new Element("key");
    saveData(element);
    return element;
  }

  @Override
  public void loadState(@NotNull Element state) {
    readData(state);
  }

  @Override
  public boolean showKeyMappings(@NotNull Set<? extends MappingMode> modes, @NotNull FimEditor editor) {
    return showKeyMappings(modes, ((IjFimEditor) editor).getEditor());
  }
}
