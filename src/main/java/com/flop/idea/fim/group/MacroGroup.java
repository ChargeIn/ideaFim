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
import com.flop.idea.fim.api.ExecutionContext;
import com.flop.idea.fim.api.FimEditor;
import com.flop.idea.fim.helper.MessageHelper;
import com.flop.idea.fim.key.KeyStack;
import com.flop.idea.fim.macro.FimMacroBase;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.util.PotemkinProgress;
import com.intellij.openapi.project.Project;
import com.flop.idea.fim.KeyHandler;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.ExecutionContext;
import com.flop.idea.fim.api.FimEditor;
import com.flop.idea.fim.helper.MessageHelper;
import com.flop.idea.fim.key.KeyStack;
import com.flop.idea.fim.macro.FimMacroBase;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Used to handle playback of macros
 */
public class MacroGroup extends FimMacroBase {
  private static final Logger logger = Logger.getInstance(MacroGroup.class.getName());

  /**
   * This puts a single keystroke at the end of the event queue for playback
   */
  @Override
  public void playbackKeys(@NotNull final FimEditor editor,
                           @NotNull final ExecutionContext context, final int cnt,
                           final int total) {
    Project project = ((IjFimEditor)editor).getEditor().getProject();
    KeyStack keyStack = KeyHandler.getInstance().getKeyStack();
    if (!keyStack.hasStroke() || cnt >= total) {
      logger.debug("done");
      keyStack.removeFirst();

      return;
    }

    if (FimPlugin.getOptionService().isSet(OptionScope.GLOBAL.INSTANCE, OptionConstants.ideadelaymacroName, OptionConstants.ideadelaymacroName)) {
      // This took a while to get just right. The original approach has a loop that made a runnable for each
      // character. It worked except for one case - if the macro had a complete ex command, the editor did not
      // end up with the focus and I couldn't find anyway to get it to have focus. This approach was the only
      // solution. This makes the most sense now (of course it took hours of trial and error to come up with
      // this one). Each key gets added, one at a time, to the event queue. If a given key results in other
      // events getting queued, they get queued before the next key, just what would happen if the user was typing
      // the keys one at a time. With the old loop approach, all the keys got queued, then any events they caused
      // were queued - after the keys. This is what caused the problem.
      final Runnable run = () -> {
        // Handle one keystroke then queue up the next key
        if (keyStack.hasStroke()) {
          KeyHandler.getInstance().handleKey(editor, keyStack.feedStroke(), context);
        }
        if (keyStack.hasStroke()) {
          playbackKeys(editor, context, cnt, total);
        }
        else {
          keyStack.resetFirst();
          playbackKeys(editor, context, cnt + 1, total);
        }
      };

      ApplicationManager.getApplication().invokeLater(() -> CommandProcessor.getInstance()
        .executeCommand(project, run, MessageHelper.message("command.name.fim.macro.playback"), null));
    } else {
      PotemkinProgress potemkinProgress =
        new PotemkinProgress(MessageHelper.message("progress.title.macro.execution"), project, null,
                             MessageHelper.message("stop"));
      potemkinProgress.setIndeterminate(false);
      potemkinProgress.setFraction(0);
      potemkinProgress.runInSwingThread(() -> {
        // Handle one keystroke then queue up the next key
        for (int i = 0; i < total; ++i) {
          potemkinProgress.setFraction((double)(i + 1) / total);
          while (keyStack.hasStroke()) {
            KeyStroke key = keyStack.feedStroke();
            try {
              potemkinProgress.checkCanceled();
            }
            catch (ProcessCanceledException e) {
              return;
            }
            ProgressManager.getInstance().executeNonCancelableSection(() -> {
              KeyHandler.getInstance().handleKey(editor, key, context);
            });
          }
          keyStack.resetFirst();
        }
        keyStack.removeFirst();
      });
    }
  }
}
