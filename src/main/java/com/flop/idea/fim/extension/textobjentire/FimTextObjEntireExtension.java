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

package com.flop.idea.fim.extension.textobjentire;

import com.flop.idea.fim.api.ExecutionContext;
import com.flop.idea.fim.api.FimCaret;
import com.flop.idea.fim.api.FimEditor;
import com.intellij.openapi.editor.Caret;
import com.flop.idea.fim.api.FimInjectorKt;
import com.flop.idea.fim.command.*;
import com.flop.idea.fim.command.MappingMode;
import com.flop.idea.fim.common.TextRange;
import com.flop.idea.fim.extension.FimExtension;
import com.flop.idea.fim.extension.ExtensionHandler;
import com.flop.idea.fim.handler.TextObjectActionHandler;
import com.flop.idea.fim.helper.InlayHelperKt;
import com.flop.idea.fim.listener.SelectionFimListenerSuppressor;
import com.flop.idea.fim.listener.FimListenerSuppressor;
import com.flop.idea.fim.newapi.IjFimCaret;
import com.flop.idea.fim.newapi.IjFimEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static com.flop.idea.fim.extension.FimExtensionFacade.putExtensionHandlerMapping;
import static com.flop.idea.fim.extension.FimExtensionFacade.putKeyMappingIfMissing;
import static com.flop.idea.fim.group.visual.VisualGroupKt.fimSetSelection;

/**
 * Port of fim-entire:
 * https://github.com/kana/fim-textobj-entire
 *
 * <p>
 * fim-textobj-entire provides two text objects:
 * <ul>
 *   <li>ae targets the entire content of the current buffer.</li>
 *   <li>ie is similar to ae, but ie does not include leading and trailing empty lines. ie is handy for some situations. For example,</li>
 *   <ul>
 *     <li>Paste some text into a new buffer (<C-w>n"*P) -- note that the initial empty line is left as the last line.</li>
 *     <li>Edit the text (:%s/foo/bar/g etc)</li>
 *     <li>Then copy the resulting text to another application ("*yie)</li>
 *   </ul>
 * </ul>
 *
 * See also the reference manual for more details at:
 * https://github.com/kana/fim-textobj-entire/blob/master/doc/textobj-entire.txt
 *
 * @author Alexandre Grison (@agrison)
 */
public class FimTextObjEntireExtension implements FimExtension {

  @Override
  public @NotNull
  String getName() {
    return "textobj-entire";
  }


  @Override
  public void init() {
    putExtensionHandlerMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-entire-a"), getOwner(),
                               new FimTextObjEntireExtension.EntireHandler(false), false);
    putExtensionHandlerMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-entire-i"), getOwner(),
      new FimTextObjEntireExtension.EntireHandler(true), false);

    putKeyMappingIfMissing(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("ae"), getOwner(), FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-entire-a"), true);
    putKeyMappingIfMissing(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("ie"), getOwner(), FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-entire-i"), true);
  }

  static class EntireHandler implements ExtensionHandler {
    final boolean ignoreLeadingAndTrailing;

    EntireHandler(boolean ignoreLeadingAndTrailing) {
      this.ignoreLeadingAndTrailing = ignoreLeadingAndTrailing;
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    static class EntireTextObjectHandler extends TextObjectActionHandler {
      final boolean ignoreLeadingAndTrailing;

      EntireTextObjectHandler(boolean ignoreLeadingAndTrailing) {
        this.ignoreLeadingAndTrailing = ignoreLeadingAndTrailing;
      }

      @Nullable
      @Override
      public TextRange getRange(@NotNull FimEditor editor,
                                @NotNull FimCaret caret,
                                @NotNull ExecutionContext context,
                                int count,
                                int rawCount,
                                @Nullable Argument argument) {
        int start = 0, end = ((IjFimEditor)editor).getEditor().getDocument().getTextLength();

        // for the `ie` text object we don't want leading an trailing spaces
        // so we have to scan the document text to find the correct start & end
        if (ignoreLeadingAndTrailing) {
          String content = ((IjFimEditor)editor).getEditor().getDocument().getText();
          for (int i = 0; i < content.length(); ++i) {
            if (!Character.isWhitespace(content.charAt(i))) {
              start = i;
              break;
            }
          }

          for (int i = content.length() - 1; i >= start; --i) {
            if (!Character.isWhitespace(content.charAt(i))) {
              end = i + 1;
              break;
            }
          }
        }

        return new TextRange(start, end);
      }

      @NotNull
      @Override
      public TextObjectVisualType getVisualType() {
        return TextObjectVisualType.CHARACTER_WISE;
      }
    }

    @Override
    public void execute(@NotNull FimEditor editor, @NotNull ExecutionContext context) {
      @NotNull FimStateMachine fimStateMachine = FimStateMachine.getInstance(editor);
      int count = Math.max(1, fimStateMachine.getCommandBuilder().getCount());

      final EntireTextObjectHandler textObjectHandler = new EntireTextObjectHandler(ignoreLeadingAndTrailing);
      //noinspection DuplicatedCode
      if (!fimStateMachine.isOperatorPending()) {
        ((IjFimEditor) editor).getEditor().getCaretModel().runForEachCaret((Caret caret) -> {
          final TextRange range = textObjectHandler.getRange(editor, new IjFimCaret(caret), context, count, 0, null);
          if (range != null) {
            try (FimListenerSuppressor.Locked ignored = SelectionFimListenerSuppressor.INSTANCE.lock()) {
              if (fimStateMachine.getMode() == FimStateMachine.Mode.VISUAL) {
                fimSetSelection(caret, range.getStartOffset(), range.getEndOffset() - 1, true);
              } else {
                InlayHelperKt.moveToInlayAwareOffset(caret, range.getStartOffset());
              }
            }
          }

        });
      } else {
        fimStateMachine.getCommandBuilder().completeCommandPart(new Argument(new Command(count,
                                                                                         textObjectHandler, Command.Type.MOTION,
                                                                                         EnumSet.noneOf(CommandFlags.class))));
      }
    }
  }
}
