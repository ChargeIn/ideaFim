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

package com.flop.idea.fim.helper;

import com.google.common.collect.Lists;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author vlan
 */
public class TestInputModel {
  private final @NotNull List<KeyStroke> myKeyStrokes = Lists.newArrayList();

  private TestInputModel() {
  }

  public static TestInputModel getInstance(@NotNull Editor editor) {
    TestInputModel model = UserDataManager.getFimTestInputModel(editor);
    if (model == null) {
      model = new TestInputModel();
      UserDataManager.setFimTestInputModel(editor, model);
    }
    return model;
  }

  public void setKeyStrokes(@NotNull List<KeyStroke> keyStrokes) {
    myKeyStrokes.clear();
    myKeyStrokes.addAll(keyStrokes);
  }

  public @Nullable KeyStroke nextKeyStroke() {

    // Return key from the unfinished mapping
    /*
    MappingStack mappingStack = KeyHandler.getInstance().getMappingStack();
    if (mappingStack.hasStroke()) {
      return mappingStack.feedStroke();
    }
    */

    if (!myKeyStrokes.isEmpty()) {
      return myKeyStrokes.remove(0);
    }
    return null;
  }
}
