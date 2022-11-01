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

package org.jetbrains.plugins.ideafim;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.flop.idea.fim.KeyHandler;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.group.visual.FimVisualTimer;
import com.flop.idea.fim.helper.EditorDataContext;
import com.flop.idea.fim.helper.RunnableHelper;
import com.flop.idea.fim.helper.TestInputModel;
import com.flop.idea.fim.newapi.IjExecutionContext;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.OptionScope;
import com.flop.idea.fim.ui.ex.ExEntryPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * NB: We need to extend from JavaCodeInsightFixtureTestCase so we
 * can create PsiFiles with proper Java Language type
 *
 * @author dhleong
 */
public abstract class JavaFimTestCase extends JavaCodeInsightFixtureTestCase {
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    Editor editor = myFixture.getEditor();
    if (editor != null) {
      KeyHandler.getInstance().fullReset(new IjFimEditor(editor));
    }
    FimPlugin.getOptionService().resetAllOptions();
    FimPlugin.getKey().resetKeyMappings();
    FimPlugin.clearError();
  }

  @Override
  protected void tearDown() throws Exception {
    ExEntryPanel.getInstance().deactivate(false);
    FimPlugin.getVariableService().clear();
    Timer swingTimer = FimVisualTimer.INSTANCE.getSwingTimer();
    if (swingTimer != null) {
      swingTimer.stop();
    }
    super.tearDown();
  }

  protected void enableExtensions(@NotNull String... extensionNames) {
    for (String name : extensionNames) {
      FimPlugin.getOptionService().setOption(OptionScope.GLOBAL.INSTANCE, name, name);
    }
  }

  public void doTest(final List<KeyStroke> keys, String before, String after) {
    //noinspection IdeaFimAssertState
    myFixture.configureByText(JavaFileType.INSTANCE, before);
    typeText(keys);
    //noinspection IdeaFimAssertState
    myFixture.checkResult(after);
  }

  @NotNull
  protected Editor typeText(@NotNull List<KeyStroke> keys) {
    final Editor editor = myFixture.getEditor();
    final KeyHandler keyHandler = KeyHandler.getInstance();
    final EditorDataContext dataContext = EditorDataContext.init(editor, null);
    final Project project = myFixture.getProject();
    TestInputModel.getInstance(editor).setKeyStrokes(keys);
    RunnableHelper.runWriteCommand(project, () -> {
      final TestInputModel inputModel = TestInputModel.getInstance(editor);
      for (KeyStroke key = inputModel.nextKeyStroke(); key != null; key = inputModel.nextKeyStroke()) {
        final ExEntryPanel exEntryPanel = ExEntryPanel.getInstance();
        if (exEntryPanel.isActive()) {
          exEntryPanel.handleKey(key);
        }
        else {
          keyHandler.handleKey(new IjFimEditor(editor), key, new IjExecutionContext(dataContext));
        }
      }
    }, null, null);
    return editor;
  }

  public void assertMode(@NotNull FimStateMachine.Mode expectedMode) {
    final FimStateMachine.Mode mode = FimStateMachine.getInstance(new IjFimEditor(myFixture.getEditor())).getMode();
    assertEquals(expectedMode, mode);
  }

  public void assertSelection(@Nullable String expected) {
    final String selected = myFixture.getEditor().getSelectionModel().getSelectedText();
    assertEquals(expected, selected);
  }

}
