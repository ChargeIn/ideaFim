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

package com.flop.idea.fim.extension.textobjindent;

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
import static com.flop.idea.fim.extension.FimExtensionFacade.putKeyMapping;
import static com.flop.idea.fim.group.visual.VisualGroupKt.fimSetSelection;

/**
 * Port of fim-indent-object:
 * https://github.com/michaeljsmith/fim-indent-object
 *
 * <p>
 * fim-indent-object provides these text objects based on the cursor line's indentation:
 * <ul>
 *   <li><code>ai</code> <b>A</b>n <b>I</b>ndentation level and line above.</li>
 *   <li><code>ii</code> <b>I</b>nner <b>I</b>ndentation level (no line above).</li>
 *   <li><code>aI</code> <b>A</b>n <b>I</b>ndentation level and lines above and below.</li>
 *   <li><code>iI</code> <b>I</b>nner <b>I</b>ndentation level (no lines above and below). Synonym of <code>ii</code></li>
 * </ul>
 *
 * See also the reference manual for more details at:
 * https://github.com/michaeljsmith/fim-indent-object/blob/master/doc/indent-object.txt
 *
 * @author Shrikant Kandula (@sharat87)
 */
public class FimIndentObject implements FimExtension {

  @Override
  public @NotNull
  String getName() {
    return "textobj-indent";
  }

  @Override
  public void init() {
    putExtensionHandlerMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-indent-ai"), getOwner(),
                               new IndentObject(true, false), false);
    putExtensionHandlerMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-indent-aI"), getOwner(),
      new IndentObject(true, true), false);
    putExtensionHandlerMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-indent-ii"), getOwner(),
      new IndentObject(false, false), false);

    putKeyMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("ai"), getOwner(), FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-indent-ai"), true);
    putKeyMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("aI"), getOwner(), FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-indent-aI"), true);
    putKeyMapping(MappingMode.XO, FimInjectorKt.getInjector().getParser().parseKeys("ii"), getOwner(), FimInjectorKt.getInjector().getParser().parseKeys("<Plug>textobj-indent-ii"), true);
  }

  static class IndentObject implements ExtensionHandler {
    final boolean includeAbove;
    final boolean includeBelow;

    IndentObject(boolean includeAbove, boolean includeBelow) {
      this.includeAbove = includeAbove;
      this.includeBelow = includeBelow;
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    static class IndentObjectHandler extends TextObjectActionHandler {
      final boolean includeAbove;
      final boolean includeBelow;

      IndentObjectHandler(boolean includeAbove, boolean includeBelow) {
        this.includeAbove = includeAbove;
        this.includeBelow = includeBelow;
      }

      @Nullable
      @Override
      public TextRange getRange(@NotNull FimEditor editor,
                                @NotNull FimCaret caret,
                                @NotNull ExecutionContext context,
                                int count,
                                int rawCount,
                                @Nullable Argument argument) {
        final CharSequence charSequence = ((IjFimEditor)editor).getEditor().getDocument().getCharsSequence();
        final int caretOffset = ((IjFimCaret)caret).getCaret().getOffset();

        // Part 1: Find the start of the caret line.
        int caretLineStartOffset = caretOffset;
        int accumulatedWhitespace = 0;
        while (--caretLineStartOffset >= 0) {
          final char ch = charSequence.charAt(caretLineStartOffset);
          if (ch == ' ' || ch == '\t') {
            ++accumulatedWhitespace;
          } else if (ch == '\n') {
            ++caretLineStartOffset;
            break;
          } else {
            accumulatedWhitespace = 0;
          }
        }
        if (caretLineStartOffset < 0) {
          caretLineStartOffset = 0;
        }

        // `caretLineStartOffset` points to the first character in the line where the caret is located.

        // Part 2: Compute the indentation level of the caret line.
        // This is done as a separate step so that it works even when the caret is inside the indentation.
        int offset = caretLineStartOffset;
        int indentSize = 0;
        while (++offset < charSequence.length()) {
          final char ch = charSequence.charAt(offset);
          if (ch == ' ' || ch == '\t') {
            ++indentSize;
          } else {
            break;
          }
        }

        // `indentSize` contains the amount of indent to be used for the text object range to be returned.

        Integer upperBoundaryOffset = null;
        // Part 3: Find a line above the caret line, that has an indentation lower than `indentSize`.
        int pos1 = caretLineStartOffset - 1;
        boolean isUpperBoundaryFound = false;
        while (upperBoundaryOffset == null) {
          // 3.1: Going backwards from `caretLineStartOffset`, find the first non-whitespace character.
          while (--pos1 >= 0) {
            final char ch = charSequence.charAt(pos1);
            if (ch != ' ' && ch != '\t' && ch != '\n') {
              break;
            }
          }
          // 3.2: Find the indent size of the line with this non-whitespace character and check against `indentSize`.
          accumulatedWhitespace = 0;
          while (--pos1 >= 0) {
            final char ch = charSequence.charAt(pos1);
            if (ch == ' ' || ch == '\t') {
              ++accumulatedWhitespace;
            } else if (ch == '\n') {
              if (accumulatedWhitespace < indentSize) {
                upperBoundaryOffset = pos1 + 1;
                isUpperBoundaryFound = true;
              }
              break;
            } else {
              accumulatedWhitespace = 0;
            }
          }
          if (pos1 < 0) {
            // Reached start of the buffer.
            upperBoundaryOffset = 0;
            isUpperBoundaryFound = accumulatedWhitespace < indentSize;
          }
        }

        // Now `upperBoundaryOffset` marks the beginning of an `ai` text object.
        if (isUpperBoundaryFound && !includeAbove) {
          while (++upperBoundaryOffset < charSequence.length()) {
            final char ch = charSequence.charAt(upperBoundaryOffset);
            if (ch == '\n') {
              ++upperBoundaryOffset;
              break;
            }
          }
          while (charSequence.charAt(upperBoundaryOffset) == '\n') {
            ++upperBoundaryOffset;
          }
        }

        // Part 4: Find the start of the caret line.
        int caretLineEndOffset = caretOffset;
        while (++caretLineEndOffset < charSequence.length()) {
          final char ch = charSequence.charAt(caretLineEndOffset);
          if (ch == '\n') {
            ++caretLineEndOffset;
            break;
          }
        }

        // `caretLineEndOffset` points to the first charater in the line below caret line.

        Integer lowerBoundaryOffset = null;
        // Part 5: Find a line below the caret line, that has an indentation lower than `indentSize`.
        int pos2 = caretLineEndOffset - 1;
        boolean isLowerBoundaryFound = false;
        while (lowerBoundaryOffset == null) {
          int accumulatedWhitespace2 = 0;
          int lastNewlinePos = caretLineEndOffset - 1;
          boolean isInIndent = true;
          while (++pos2 < charSequence.length()) {
            final char ch = charSequence.charAt(pos2);
            if (isIndentChar(ch) && isInIndent) {
              ++accumulatedWhitespace2;
            } else if (ch == '\n') {
              accumulatedWhitespace2 = 0;
              lastNewlinePos = pos2;
              isInIndent = true;
            } else {
              if (isInIndent && accumulatedWhitespace2 < indentSize) {
                lowerBoundaryOffset = lastNewlinePos;
                isLowerBoundaryFound = true;
                break;
              }
              isInIndent = false;
            }
          }
          if (pos2 >= charSequence.length()) {
            // Reached end of the buffer.
            lowerBoundaryOffset = charSequence.length() - 1;
          }
        }

        // Now `lowerBoundaryOffset` marks the end of an `ii` text object.
        if (isLowerBoundaryFound && includeBelow) {
          while (++lowerBoundaryOffset < charSequence.length()) {
            final char ch = charSequence.charAt(lowerBoundaryOffset);
            if (ch == '\n') {
              break;
            }
          }
        }

        return new TextRange(upperBoundaryOffset, lowerBoundaryOffset);
      }

      @NotNull
      @Override
      public TextObjectVisualType getVisualType() {
        return TextObjectVisualType.LINE_WISE;
      }

      private boolean isIndentChar(char ch) {
        return ch == ' ' || ch == '\t';
      }

    }

    @Override
    public void execute(@NotNull FimEditor editor, @NotNull ExecutionContext context) {
      IjFimEditor fimEditor = (IjFimEditor)editor;
      @NotNull FimStateMachine fimStateMachine = FimStateMachine.getInstance(fimEditor);
      int count = Math.max(1, fimStateMachine.getCommandBuilder().getCount());

      final IndentObjectHandler textObjectHandler = new IndentObjectHandler(includeAbove, includeBelow);

      if (!fimStateMachine.isOperatorPending()) {
        ((IjFimEditor)editor).getEditor().getCaretModel().runForEachCaret((Caret caret) -> {
          final TextRange range = textObjectHandler.getRange(fimEditor, new IjFimCaret(caret), context, count, 0, null);
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
