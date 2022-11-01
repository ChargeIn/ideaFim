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

import com.flop.idea.fim.KeyHandler;
import com.flop.idea.fim.api.FimEditor;
import com.flop.idea.fim.api.FimEditorGroup;
import com.flop.idea.fim.helper.DocumentManager;
import com.flop.idea.fim.helper.EditorDataContext;
import com.flop.idea.fim.helper.EditorHelper;
import com.flop.idea.fim.newapi.IjExecutionContext;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.LocalOptionChangeListener;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType;
import com.flop.idea.fim.fimscript.services.IjFimOptionService;
import com.intellij.find.EditorSearchSession;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorGutter;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.LineNumberConverter;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.Project;
import com.flop.idea.fim.KeyHandler;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.FimEditor;
import com.flop.idea.fim.api.FimEditorGroup;
import com.flop.idea.fim.helper.*;
import com.flop.idea.fim.newapi.IjExecutionContext;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.LocalOptionChangeListener;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import com.flop.idea.fim.fimscript.model.datatypes.FimDataType;
import com.flop.idea.fim.fimscript.services.IjFimOptionService;
import org.jdom.Element;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.flop.idea.fim.helper.CaretVisualAttributesHelperKt.updateCaretsVisualAttributes;

/**
 * @author vlan
 */
@State(name = "FimEditorSettings", storages = {@Storage(value = "$APP_CONFIG$/fim_settings.xml")})
public class EditorGroup implements PersistentStateComponent<Element>, FimEditorGroup {
  public static final @NonNls String EDITOR_STORE_ELEMENT = "editor";

  private Boolean isKeyRepeat = null;

  private final CaretListener myLineNumbersCaretListener = new CaretListener() {
    @Override
    public void caretPositionChanged(@NotNull CaretEvent e) {
      final boolean requiresRepaint = e.getNewPosition().line != e.getOldPosition().line;
      if (requiresRepaint && FimPlugin.getOptionService().isSet(new OptionScope.LOCAL(new IjFimEditor(e.getEditor())), OptionConstants.relativenumberName, OptionConstants.relativenumberName)) {
        repaintRelativeLineNumbers(e.getEditor());
      }
    }
  };

  private void initLineNumbers(final @NotNull Editor editor) {
    if (!supportsFimLineNumbers(editor) || UserDataManager.getFimEditorGroup(editor)) {
      return;
    }

    editor.getCaretModel().addCaretListener(myLineNumbersCaretListener);
    UserDataManager.setFimEditorGroup(editor, true);

    UserDataManager.setFimLineNumbersInitialState(editor, editor.getSettings().isLineNumbersShown());
    updateLineNumbers(editor);
  }

  private void deinitLineNumbers(@NotNull Editor editor, boolean isReleasing) {
    if (isProjectDisposed(editor) || !supportsFimLineNumbers(editor) || !UserDataManager.getFimEditorGroup(editor)) {
      return;
    }

    editor.getCaretModel().removeCaretListener(myLineNumbersCaretListener);
    UserDataManager.setFimEditorGroup(editor, false);

    removeRelativeLineNumbers(editor);

    // Don't reset the built in line numbers if we're releasing the editor. If we do, EditorSettings.setLineNumbersShown
    // can cause the editor to refresh settings and can call into FileManagerImpl.getCachedPsiFile AFTER FileManagerImpl
    // has been disposed (Closing the project with a Find Usages result showing a preview panel is a good repro case).
    // See IDEA-184351 and VIM-1671
    if (!isReleasing) {
      setBuiltinLineNumbers(editor, UserDataManager.getFimLineNumbersInitialState(editor));
    }
  }

  private static boolean supportsFimLineNumbers(final @NotNull Editor editor) {
    // We only support line numbers in editors that are file based, and that aren't for diffs, which control their
    // own line numbers, often using EditorGutter#setLineNumberConverter
    return EditorHelper.isFileEditor(editor) && !EditorHelper.isDiffEditor(editor);
  }

  private static boolean isProjectDisposed(final @NotNull Editor editor) {
    return editor.getProject() == null || editor.getProject().isDisposed();
  }

  private static void updateLineNumbers(final @NotNull Editor editor) {
    final boolean relativeNumber = FimPlugin.getOptionService().isSet(new OptionScope.LOCAL(new IjFimEditor(editor)), OptionConstants.relativenumberName, OptionConstants.relativenumberName);
    final boolean number = FimPlugin.getOptionService().isSet(new OptionScope.LOCAL(new IjFimEditor(editor)), OptionConstants.numberName, OptionConstants.numberName);

    final boolean showBuiltinEditorLineNumbers = shouldShowBuiltinLineNumbers(editor, number, relativeNumber);

    final EditorSettings settings = editor.getSettings();
    if (settings.isLineNumbersShown() ^ showBuiltinEditorLineNumbers) {
      // Update line numbers later since it may be called from a caret listener
      // on the caret move and it may move the caret internally
      ApplicationManager.getApplication().invokeLater(() -> {
        if (editor.isDisposed()) return;
        setBuiltinLineNumbers(editor, showBuiltinEditorLineNumbers);
      });
    }

    if (relativeNumber) {
      if (!hasRelativeLineNumbersInstalled(editor)) {
        installRelativeLineNumbers(editor);
      }
    }
    else if (hasRelativeLineNumbersInstalled(editor)) {
      removeRelativeLineNumbers(editor);
    }
  }

  private static boolean shouldShowBuiltinLineNumbers(final @NotNull Editor editor, boolean number, boolean relativeNumber) {
    final boolean initialState = UserDataManager.getFimLineNumbersInitialState(editor);
    return initialState || number || relativeNumber;
  }

  private static void setBuiltinLineNumbers(final @NotNull Editor editor, boolean show) {
    editor.getSettings().setLineNumbersShown(show);
  }

  private static boolean hasRelativeLineNumbersInstalled(final @NotNull Editor editor) {
    return UserDataManager.getFimHasRelativeLineNumbersInstalled(editor);
  }

  private static void installRelativeLineNumbers(final @NotNull Editor editor) {
    if (!hasRelativeLineNumbersInstalled(editor)) {
      final EditorGutter gutter = editor.getGutter();
      gutter.setLineNumberConverter(new RelativeLineNumberConverter());
      UserDataManager.setFimHasRelativeLineNumbersInstalled(editor, true);
    }
  }

  private static void removeRelativeLineNumbers(final @NotNull Editor editor) {
    if (hasRelativeLineNumbersInstalled(editor)) {
      final EditorGutter gutter = editor.getGutter();
      gutter.setLineNumberConverter(LineNumberConverter.DEFAULT);
      UserDataManager.setFimHasRelativeLineNumbersInstalled(editor, false);
    }
  }

  private static void repaintRelativeLineNumbers(final @NotNull Editor editor) {
    final EditorGutter gutter = editor.getGutter();
    final EditorGutterComponentEx gutterComponent = gutter instanceof EditorGutterComponentEx ? (EditorGutterComponentEx) gutter : null;
    if (gutterComponent != null) {
      gutterComponent.repaint();
    }
  }

  public void saveData(@NotNull Element element) {
    final Element editor = new Element("editor");
    element.addContent(editor);

    if (isKeyRepeat != null) {
      final Element keyRepeat = new Element("key-repeat");
      keyRepeat.setAttribute("enabled", Boolean.toString(isKeyRepeat));
      editor.addContent(keyRepeat);
    }
  }

  public void readData(@NotNull Element element) {
    final Element editor = element.getChild(EDITOR_STORE_ELEMENT);
    if (editor != null) {
      final Element keyRepeat = editor.getChild("key-repeat");
      if (keyRepeat != null) {
        final String enabled = keyRepeat.getAttributeValue("enabled");
        if (enabled != null) {
          isKeyRepeat = Boolean.valueOf(enabled);
        }
      }
    }
  }

  public @Nullable Boolean isKeyRepeat() {
    return isKeyRepeat;
  }

  public void setKeyRepeat(@Nullable Boolean value) {
    this.isKeyRepeat = value;
  }

  public void closeEditorSearchSession(@NotNull Editor editor) {
    final EditorSearchSession editorSearchSession = EditorSearchSession.get(editor);
    if (editorSearchSession != null) {
      editorSearchSession.close();
    }
  }

  public void editorCreated(@NotNull Editor editor) {
    DocumentManager.INSTANCE.addListeners(editor.getDocument());
    FimPlugin.getKey().registerRequiredShortcutKeys(new IjFimEditor(editor));

    initLineNumbers(editor);
    // Turn on insert mode if editor doesn't have any file
    if (!EditorHelper.isFileEditor(editor) &&
        editor.getDocument().isWritable() &&
        !CommandStateHelper.inInsertMode(editor)) {
      FimPlugin.getChange().insertBeforeCursor(new IjFimEditor(editor), new IjExecutionContext(
          EditorDataContext.init(editor, null)));
      KeyHandler.getInstance().reset(new IjFimEditor(editor));
    }
    updateCaretsVisualAttributes(editor);
  }

  public void editorDeinit(@NotNull Editor editor, boolean isReleased) {
    deinitLineNumbers(editor, isReleased);
    UserDataManager.unInitializeEditor(editor);
    FimPlugin.getKey().unregisterShortcutKeys(new IjFimEditor(editor));
    DocumentManager.INSTANCE.removeListeners(editor.getDocument());
    CaretVisualAttributesHelperKt.removeCaretsVisualAttributes(editor);
  }

  public void notifyIdeaJoin(@Nullable Project project) {
    if (FimPlugin.getFimState().isIdeaJoinNotified()
        || FimPlugin.getOptionService().isSet(OptionScope.GLOBAL.INSTANCE, IjFimOptionService.ideajoinName, IjFimOptionService.ideajoinName)) {
      return;
    }

    FimPlugin.getFimState().setIdeaJoinNotified(true);
    FimPlugin.getNotifications(project).notifyAboutIdeaJoin();
  }

  @Nullable
  @Override
  public Element getState() {
    Element element = new Element("editor");
    saveData(element);
    return element;
  }

  @Override
  public void loadState(@NotNull Element state) {
    readData(state);
  }

  @Override
  public void notifyIdeaJoin(@NotNull FimEditor editor) {
    notifyIdeaJoin(((IjFimEditor) editor).getEditor().getProject());
  }

  public static class NumberChangeListener implements LocalOptionChangeListener<FimDataType> {
    public static NumberChangeListener INSTANCE = new NumberChangeListener();

    @Contract(pure = true)
    private NumberChangeListener() {
    }

    @Override
    public void processGlobalValueChange(@Nullable FimDataType oldValue) {
      for (Editor editor : HelperKt.localEditors()) {
        if (UserDataManager.getFimEditorGroup(editor) && supportsFimLineNumbers(editor)) {
          updateLineNumbers(editor);
        }
      }
    }

    @Override
    public void processLocalValueChange(@Nullable FimDataType oldValue, @NotNull FimEditor editor) {
      Editor ijEditor = ((IjFimEditor)editor).getEditor();

      if (UserDataManager.getFimEditorGroup(ijEditor) && supportsFimLineNumbers(ijEditor)) {
        updateLineNumbers(ijEditor);
      }
    }
  }

  private static class RelativeLineNumberConverter implements LineNumberConverter {
    @Override
    public Integer convert(@NotNull Editor editor, int lineNumber) {
      final boolean number = FimPlugin.getOptionService().isSet(new OptionScope.LOCAL(new IjFimEditor(editor)), OptionConstants.numberName, OptionConstants.numberName);
      final int caretLine = editor.getCaretModel().getLogicalPosition().line;

      // lineNumber is 1 based
      if (number && (lineNumber - 1) == caretLine) {
        return lineNumber;
      }
      else {
        final int visualLine = EditorHelper.logicalLineToVisualLine(editor, lineNumber - 1);
        final int currentVisualLine = EditorHelper.logicalLineToVisualLine(editor, caretLine);
        return Math.abs(currentVisualLine - visualLine);
      }
    }

    @Override
    public Integer getMaxLineNumber(@NotNull Editor editor) {
      return editor.getDocument().getLineCount();
    }
  }

  @NotNull
  @Override
  public Collection<FimEditor> localEditors() {
    return HelperKt.localEditors().stream()
      .map(IjFimEditor::new)
      .collect(Collectors.toList());
  }
}
