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
import com.flop.idea.fim.api.FimProcessGroupBase;
import com.flop.idea.fim.command.Command;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.ex.ExException;
import com.flop.idea.fim.ex.InvalidCommandException;
import com.flop.idea.fim.newapi.IjExecutionContext;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import com.flop.idea.fim.fimscript.model.CommandLineFimLContext;
import com.flop.idea.fim.fimscript.model.datatypes.FimString;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.util.text.CharSequenceReader;
import com.flop.idea.fim.KeyHandler;
import com.flop.idea.fim.FimPlugin;
import com.flop.idea.fim.api.*;
import com.flop.idea.fim.command.Command;
import com.flop.idea.fim.command.FimStateMachine;
import com.flop.idea.fim.ex.ExException;
import com.flop.idea.fim.ex.InvalidCommandException;
import com.flop.idea.fim.helper.UiHelper;
import com.flop.idea.fim.newapi.IjExecutionContext;
import com.flop.idea.fim.newapi.IjFimEditor;
import com.flop.idea.fim.options.OptionConstants;
import com.flop.idea.fim.options.OptionScope;
import com.flop.idea.fim.ui.ex.ExEntryPanel;
import com.flop.idea.fim.fimscript.model.CommandLineFimLContext;
import com.flop.idea.fim.fimscript.model.datatypes.FimString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

import static com.flop.idea.fim.api.FimInjectorKt.injector;


public class ProcessGroup extends FimProcessGroupBase {
  public String getLastCommand() {
    return lastCommand;
  }

  @Override
  public void startSearchCommand(@NotNull FimEditor editor, ExecutionContext context, int count, char leader) {
    if (((IjFimEditor)editor).getEditor().isOneLineMode()) // Don't allow searching in one line editors
    {
      return;
    }

    String initText = "";
    String label = String.valueOf(leader);

    com.flop.idea.fim.ui.ex.ExEntryPanel panel = com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance();
    panel.activate(((IjFimEditor)editor).getEditor(), ((DataContext)context.getContext()), label, initText, count);
  }

  @Override
  public @NotNull String endSearchCommand() {
    com.flop.idea.fim.ui.ex.ExEntryPanel panel = com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance();
    panel.deactivate(true);

    return panel.getText();
  }

  public void startExCommand(@NotNull FimEditor editor, ExecutionContext context, @NotNull Command cmd) {
    // Don't allow ex commands in one line editors
    if (editor.isOneLineMode()) return;

    String initText = getRange(((IjFimEditor) editor).getEditor(), cmd);
    FimStateMachine.getInstance(editor).pushModes(FimStateMachine.Mode.CMD_LINE, FimStateMachine.SubMode.NONE);
    com.flop.idea.fim.ui.ex.ExEntryPanel panel = com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance();
    panel.activate(((IjFimEditor) editor).getEditor(), ((IjExecutionContext) context).getContext(), ":", initText, 1);
  }

  @Override
  public boolean processExKey(@NotNull FimEditor editor, @NotNull KeyStroke stroke) {
    // This will only get called if somehow the key focus ended up in the editor while the ex entry window
    // is open. So I'll put focus back in the editor and process the key.

    com.flop.idea.fim.ui.ex.ExEntryPanel panel = com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance();
    if (panel.isActive()) {
      UiHelper.requestFocus(panel.getEntry());
      panel.handleKey(stroke);

      return true;
    }
    else {
      FimStateMachine.getInstance(editor).popModes();
      KeyHandler.getInstance().reset(editor);
      return false;
    }
  }

  public boolean processExEntry(final @NotNull FimEditor editor, final @NotNull ExecutionContext context) {
    com.flop.idea.fim.ui.ex.ExEntryPanel panel = com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance();
    panel.deactivate(true);
    boolean res = true;
    try {
      FimStateMachine.getInstance(editor).popModes();

      logger.debug("processing command");

      final String text = panel.getText();

      if (!panel.getLabel().equals(":")) {
        // Search is handled via Argument.Type.EX_STRING. Although ProcessExEntryAction is registered as the handler for
        // <CR> in both command and search modes, it's only invoked for command mode (see KeyHandler.handleCommandNode).
        // We should never be invoked for anything other than an actual ex command.
        throw new InvalidCommandException("Expected ':' command. Got '" + panel.getLabel() + "'", text);
      }

      if (logger.isDebugEnabled()) logger.debug("swing=" + SwingUtilities.isEventDispatchThread());

      FimInjectorKt.getInjector().getFimscriptExecutor().execute(text, editor, context, skipHistory(editor), true, CommandLineFimLContext.INSTANCE);
    }
    catch (ExException e) {
      FimPlugin.showMessage(e.getMessage());
      FimPlugin.indicateError();
      res = false;
    }
    catch (Exception bad) {
      ProcessGroup.logger.error(bad);
      FimPlugin.indicateError();
      res = false;
    }

    return res;
  }

  // commands executed from map command / macro should not be added to history
  private boolean skipHistory(FimEditor editor) {
    return FimStateMachine.getInstance(editor).getMappingState().isExecutingMap() || injector.getMacro().isExecutingMacro();
  }

  public void cancelExEntry(final @NotNull FimEditor editor, boolean resetCaret) {
    FimStateMachine.getInstance(editor).popModes();
    KeyHandler.getInstance().reset(editor);
    com.flop.idea.fim.ui.ex.ExEntryPanel panel = com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance();
    panel.deactivate(true, resetCaret);
  }

  @Override
  public void startFilterCommand(@NotNull FimEditor editor, ExecutionContext context, @NotNull Command cmd) {
    String initText = getRange(((IjFimEditor) editor).getEditor(), cmd) + "!";
    FimStateMachine.getInstance(editor).pushModes(FimStateMachine.Mode.CMD_LINE, FimStateMachine.SubMode.NONE);
    com.flop.idea.fim.ui.ex.ExEntryPanel panel = ExEntryPanel.getInstance();
    panel.activate(((IjFimEditor) editor).getEditor(), ((IjExecutionContext) context).getContext(), ":", initText, 1);
  }

  private @NotNull String getRange(Editor editor, @NotNull Command cmd) {
    String initText = "";
    if (FimStateMachine.getInstance(new IjFimEditor(editor)).getMode() == FimStateMachine.Mode.VISUAL) {
      initText = "'<,'>";
    }
    else if (cmd.getRawCount() > 0) {
      if (cmd.getCount() == 1) {
        initText = ".";
      }
      else {
        initText = ".,.+" + (cmd.getCount() - 1);
      }
    }

    return initText;
  }

  public @Nullable String executeCommand(@NotNull FimEditor editor, @NotNull String command, @Nullable CharSequence input, @Nullable String currentDirectoryPath)
    throws ExecutionException, ProcessCanceledException {

    // This is a much simplified version of how Fim does this. We're using stdin/stdout directly, while Fim will
    // redirect to temp files ('shellredir' and 'shelltemp') or use pipes. We don't support 'shellquote', because we're
    // not handling redirection, but we do use 'shellxquote' and 'shellxescape', because these have defaults that work
    // better with Windows. We also don't bother using ShellExecute for Windows commands beginning with `start`.
    // Finally, we're also not bothering with the crazy space and backslash handling of the 'shell' options content.
    return ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {

      final String shell = ((FimString) FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.shellName, OptionConstants.shellName)).getValue();
      final String shellcmdflag = ((FimString) FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.shellcmdflagName, OptionConstants.shellcmdflagName)).getValue();
      final String shellxescape = ((FimString) FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.shellxescapeName, OptionConstants.shellxescapeName)).getValue();
      final String shellxquote = ((FimString) FimPlugin.getOptionService().getOptionValue(OptionScope.GLOBAL.INSTANCE, OptionConstants.shellxquoteName, OptionConstants.shellxquoteName)).getValue();

      // For Win32. See :help 'shellxescape'
      final String escapedCommand = shellxquote.equals("(")
                                    ? doEscape(command, shellxescape, "^")
                                    : command;
      // Required for Win32+cmd.exe, defaults to "(". See :help 'shellxquote'
      final String quotedCommand = shellxquote.equals("(")
                                   ? "(" + escapedCommand + ")"
                                   : (shellxquote.equals("\"(")
                                      ? "\"(" + escapedCommand + ")\""
                                      : shellxquote + escapedCommand + shellxquote);

      final ArrayList<String> commands = new ArrayList<>();
      commands.add(shell);
      if (!shellcmdflag.isEmpty()) {
        // Note that Fim also does a simple whitespace split for multiple parameters
        commands.addAll(ParametersListUtil.parse(shellcmdflag));
      }
      commands.add(quotedCommand);

      if (logger.isDebugEnabled()) {
        logger.debug(String.format("shell=%s shellcmdflag=%s command=%s", shell, shellcmdflag, quotedCommand));
      }

      final GeneralCommandLine commandLine = new GeneralCommandLine(commands);
      if (currentDirectoryPath != null) {
        commandLine.setWorkDirectory(currentDirectoryPath);
      }
      final CapturingProcessHandler handler = new CapturingProcessHandler(commandLine);
      if (input != null) {
        handler.addProcessListener(new ProcessAdapter() {
          @Override
          public void startNotified(@NotNull ProcessEvent event) {
            try {
              final CharSequenceReader charSequenceReader = new CharSequenceReader(input);
              final BufferedWriter outputStreamWriter = new BufferedWriter(new OutputStreamWriter(handler.getProcessInput()));
              copy(charSequenceReader, outputStreamWriter);
              outputStreamWriter.close();
            }
            catch (IOException e) {
              logger.error(e);
            }
          }
        });
      }

      final ProgressIndicator progressIndicator = ProgressIndicatorProvider.getInstance().getProgressIndicator();
      final ProcessOutput output = handler.runProcessWithProgressIndicator(progressIndicator);

      lastCommand = command;

      if (output.isCancelled()) {
        // TODO: Fim will use whatever text has already been written to stdout
        // For whatever reason, we're not getting any here, so just throw an exception
        throw new ProcessCanceledException();
      }

      final Integer exitCode = handler.getExitCode();
      if (exitCode != null && exitCode != 0) {
        FimPlugin.showMessage("shell returned " + exitCode);
        FimPlugin.indicateError();
      }

      // Get stderr; stdout and strip colors, which are not handles properly.
      return (output.getStderr() + output.getStdout()).replaceAll("\u001B\\[[;\\d]*m", "");
    }, "IdeaFim - !" + command, true, ((IjFimEditor) editor).getEditor().getProject());
  }

  private String doEscape(String original, String charsToEscape, String escapeChar) {
    String result = original;
    for (char c : charsToEscape.toCharArray()) {
      result = result.replace("" + c, escapeChar + c);
    }
    return result;
  }

  // TODO: Java 10 has a transferTo method we could use instead
  private void copy(@NotNull Reader from, @NotNull Writer to) throws IOException {
    char[] buf = new char[2048];
    int cnt;
    while ((cnt = from.read(buf)) != -1) {
      to.write(buf, 0, cnt);
    }
  }

  private String lastCommand;

  private static final Logger logger = Logger.getInstance(ProcessGroup.class.getName());
}
