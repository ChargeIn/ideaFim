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

import com.flop.idea.fim.api.ExecutionContext;
import com.flop.idea.fim.helper.MessageHelper;
import com.flop.idea.fim.helper.RWLockLabel;
import com.flop.idea.fim.newapi.IjExecutionContext;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorComposite;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.ExecutionContext;
import com.flop.idea.fim.helper.MessageHelper;
import com.flop.idea.fim.helper.RWLockLabel;
import com.flop.idea.fim.newapi.IjExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class WindowGroup extends WindowGroupBase {
  @Override
  public void closeCurrentWindow(@NotNull ExecutionContext context) {
    final FileEditorManagerEx fileEditorManager = getFileEditorManager((DataContext)context.getContext());
    final EditorWindow window = fileEditorManager.getSplitters().getCurrentWindow();
    if (window != null) {
      window.closeAllExcept(null);
    }
  }

  @Override
  public void closeAllExceptCurrent(@NotNull ExecutionContext context) {
    final FileEditorManagerEx fileEditorManager = getFileEditorManager(((DataContext)context.getContext()));
    final EditorWindow current = fileEditorManager.getCurrentWindow();
    for (final EditorWindow window : fileEditorManager.getWindows()) {
      if (window != current) {
        window.closeAllExcept(null);
      }
    }
  }

  public void closeAllExceptCurrentTab(@NotNull DataContext context) {
    final EditorWindow currentWindow = getFileEditorManager(context).getCurrentWindow();
    currentWindow.closeAllExcept(currentWindow.getSelectedFile());
  }

  public void closeAll(@NotNull ExecutionContext context) {
    getFileEditorManager(((IjExecutionContext) context).getContext()).closeAllFiles();
  }

  @Override
  public void selectNextWindow(@NotNull ExecutionContext context) {
    final FileEditorManagerEx fileEditorManager = getFileEditorManager(((DataContext)context.getContext()));
    final EditorWindow current = fileEditorManager.getCurrentWindow();
    if (current != null) {
      fileEditorManager.getNextWindow(current).setAsCurrentWindow(true);
    }
  }

  @Override
  public void selectPreviousWindow(@NotNull ExecutionContext context) {
    final FileEditorManagerEx fileEditorManager = getFileEditorManager(((DataContext)context.getContext()));
    final EditorWindow current = fileEditorManager.getCurrentWindow();
    if (current != null) {
      fileEditorManager.getPrevWindow(current).setAsCurrentWindow(true);
    }
  }

  @Override
  public void selectWindow(@NotNull ExecutionContext context, int index) {
    final FileEditorManagerEx fileEditorManager = getFileEditorManager(((DataContext)context.getContext()));
    final EditorWindow[] windows = fileEditorManager.getWindows();
    if (index - 1 < windows.length) {
      windows[index - 1].setAsCurrentWindow(true);
    }
  }

  @Override
  public void splitWindowHorizontal(@NotNull ExecutionContext context, @NotNull String filename) {
    splitWindow(SwingConstants.HORIZONTAL, (DataContext)context.getContext(), filename);
  }

  @Override
  public void splitWindowVertical(@NotNull ExecutionContext context, @NotNull String filename) {
    splitWindow(SwingConstants.VERTICAL, (DataContext)context.getContext(), filename);
  }

  @Override
  @RWLockLabel.Readonly
  @RequiresReadLock
  public void selectWindowInRow(@NotNull ExecutionContext context, int relativePosition, boolean vertical) {
    final FileEditorManagerEx fileEditorManager = getFileEditorManager(((DataContext)context.getContext()));
    final EditorWindow currentWindow = fileEditorManager.getCurrentWindow();
    if (currentWindow != null) {
      final EditorWindow[] windows = fileEditorManager.getWindows();
      final List<EditorWindow> row = findWindowsInRow(currentWindow, Arrays.asList(windows), vertical);
      selectWindow(currentWindow, row, relativePosition);
    }
  }

  private void selectWindow(@NotNull EditorWindow currentWindow, @NotNull List<EditorWindow> windows,
                            int relativePosition) {
    final int pos = windows.indexOf(currentWindow);
    final int selected = pos + relativePosition;
    final int normalized = Math.max(0, Math.min(selected, windows.size() - 1));
    windows.get(normalized).setAsCurrentWindow(true);
  }

  private static @NotNull List<EditorWindow> findWindowsInRow(@NotNull EditorWindow anchor,
                                                              @NotNull List<EditorWindow> windows, final boolean vertical) {
    final Rectangle anchorRect = getEditorWindowRectangle(anchor);
    if (anchorRect != null) {
      final List<EditorWindow> result = new ArrayList<>();
      final double coord = vertical ? anchorRect.getX() : anchorRect.getY();
      for (EditorWindow window : windows) {
        final Rectangle rect = getEditorWindowRectangle(window);
        if (rect != null) {
          final double min = vertical ? rect.getX() : rect.getY();
          final double max = min + (vertical ? rect.getWidth() : rect.getHeight());
          if (coord >= min && coord <= max) {
            result.add(window);
          }
        }
      }
      result.sort((window1, window2) -> {
        final Rectangle rect1 = getEditorWindowRectangle(window1);
        final Rectangle rect2 = getEditorWindowRectangle(window2);
        if (rect1 != null && rect2 != null) {
          final double diff = vertical ? (rect1.getY() - rect2.getY()) : (rect1.getX() - rect2.getX());
          return diff < 0 ? -1 : diff > 0 ? 1 : 0;
        }
        return 0;
      });
      return result;
    }
    return Collections.singletonList(anchor);
  }

  private static @NotNull FileEditorManagerEx getFileEditorManager(@NotNull DataContext context) {
    final Project project = PlatformDataKeys.PROJECT.getData(context);
    return FileEditorManagerEx.getInstanceEx(Objects.requireNonNull(project));
  }

  private void splitWindow(int orientation, @NotNull DataContext context, @NotNull String filename) {
    final Project project = PlatformDataKeys.PROJECT.getData(context);
    if (project == null) return;
    final FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(project);

    VirtualFile virtualFile = null;
    if (filename.length() > 0) {
      virtualFile = FimPlugin.getFile().findFile(filename, project);
      if (virtualFile == null) {
        FimPlugin.showMessage(MessageHelper.message("could.not.find.file.0", filename));
        return;
      }
    }

    final EditorWindow editorWindow = fileEditorManager.getSplitters().getCurrentWindow();
    if (editorWindow != null) {
      editorWindow.split(orientation, true, virtualFile, true);
    }
  }

  private static @Nullable Rectangle getEditorWindowRectangle(@NotNull EditorWindow window) {
    final EditorComposite editor = window.getSelectedComposite();
    if (editor != null) {
      final Point point = editor.getComponent().getLocationOnScreen();
      final Dimension dimension = editor.getComponent().getSize();
      return new Rectangle(point, dimension);
    }
    return null;
  }
}
