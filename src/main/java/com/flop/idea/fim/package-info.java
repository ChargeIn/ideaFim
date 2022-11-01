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

/**
 * IdeaFim command index.
 *
 *
 * 1. Insert mode
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |i_CTRL-@|             {@link com.flop.idea.fim.action.change.insert.InsertPreviousInsertExitAction}
 * |i_CTRL-A|             {@link com.flop.idea.fim.action.change.insert.InsertPreviousInsertAction}
 * |i_CTRL-C|             {@link com.flop.idea.fim.action.change.insert.InsertExitModeAction}
 * |i_CTRL-D|             {@link com.flop.idea.fim.action.change.shift.ShiftLeftLinesAction}
 * |i_CTRL-E|             {@link com.flop.idea.fim.action.change.insert.InsertCharacterBelowCursorAction}
 * |i_CTRL-G_j|           TO BE IMPLEMENTED
 * |i_CTRL-G_k|           TO BE IMPLEMENTED
 * |i_CTRL-G_u|           TO BE IMPLEMENTED
 * |i_<BS>|               {@link com.flop.idea.fim.action.editor.FimEditorBackSpace}
 * |i_digraph|            IdeaFim enter digraph
 * |i_CTRL-H|             IntelliJ editor backspace
 * |i_<Tab>|              {@link com.flop.idea.fim.action.editor.FimEditorTab}
 * |i_CTRL-I|             IntelliJ editor tab
 * |i_<NL>|               {@link com.flop.idea.fim.action.change.insert.InsertEnterAction}
 * |i_CTRL-J|             TO BE IMPLEMENTED
 * |i_CTRL-K|             {@link com.flop.idea.fim.action.change.insert.InsertCompletedDigraphAction}
 * |i_CTRL-L|             TO BE IMPLEMENTED
 * |i_<CR>|               {@link com.flop.idea.fim.action.change.insert.InsertEnterAction}
 * |i_CTRL-M|             {@link com.flop.idea.fim.action.change.insert.InsertEnterAction}
 * |i_CTRL-N|             {@link com.flop.idea.fim.action.window.LookupDownAction}
 * |i_CTRL-O|             {@link com.flop.idea.fim.action.change.insert.InsertSingleCommandAction}
 * |i_CTRL-P|             {@link com.flop.idea.fim.action.window.LookupUpAction}
 * |i_CTRL-Q|             TO BE IMPLEMENTED
 * |i_CTRL-R|             {@link com.flop.idea.fim.action.change.insert.InsertRegisterAction}
 * |i_CTRL-R_CTRL-R|      TO BE IMPLEMENTED
 * |i_CTRL-R_CTRL-O|      TO BE IMPLEMENTED
 * |i_CTRL-R_CTRL-P|      TO BE IMPLEMENTED
 * |i_CTRL-T|             {@link com.flop.idea.fim.action.change.shift.ShiftRightLinesAction}
 * |i_CTRL-U|             {@link com.flop.idea.fim.action.change.insert.InsertDeleteInsertedTextAction}
 * |i_CTRL-V|             {@link com.flop.idea.fim.action.change.insert.InsertCompletedLiteralAction}
 * |i_CTRL-V_digit|       {@link com.flop.idea.fim.action.change.insert.InsertCompletedLiteralAction}
 * |i_CTRL-W|             {@link com.flop.idea.fim.action.change.insert.InsertDeletePreviousWordAction}
 * |i_CTRL-X|             TO BE IMPLEMENTED
 * |i_CTRL-Y|             {@link com.flop.idea.fim.action.change.insert.InsertCharacterAboveCursorAction}
 * |i_CTRL-Z|             TO BE IMPLEMENTED
 * |i_<Esc>|              {@link com.flop.idea.fim.action.change.insert.InsertExitModeAction}
 * |i_CTRL-[|             {@link com.flop.idea.fim.action.change.insert.InsertExitModeAction}
 * |i_CTRL-\_CTRL-N|      {@link com.flop.idea.fim.action.ResetModeAction}
 * |i_CTRL-\_CTRL-G|      TO BE IMPLEMENTED
 * |i_CTRL-]}             TO BE IMPLEMENTED
 * |i_CTRL-^|             TO BE IMPLEMENTED
 * |i_CTRL-_|             TO BE IMPLEMENTED
 * |i_0_CTRL-D|           TO BE IMPLEMENTED
 * |i_^_CTRL-D|           TO BE IMPLEMENTED
 * |i_<Del>|              {@link com.flop.idea.fim.action.editor.FimEditorDelete}
 * |i_<Left>|             {@link com.flop.idea.fim.action.motion.leftright.MotionLeftInsertModeAction}
 * |i_<S-Left>|           {@link com.flop.idea.fim.action.motion.text.MotionWordLeftInsertAction}
 * |i_<C-Left>|           {@link com.flop.idea.fim.action.motion.text.MotionWordLeftInsertAction}
 * |i_<Right>|            {@link com.flop.idea.fim.action.motion.leftright.MotionRightInsertAction}
 * |i_<S-Right>|          {@link com.flop.idea.fim.action.motion.text.MotionWordRightInsertAction}
 * |i_<C-Right>|          {@link com.flop.idea.fim.action.motion.text.MotionWordRightInsertAction}
 * |i_<Up>|               {@link com.flop.idea.fim.action.editor.FimEditorUp}
 * |i_<S-Up>|             {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageUpInsertModeAction}
 * |i_<Down>|             {@link com.flop.idea.fim.action.editor.FimEditorDown}
 * |i_<S-Down>|           {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageDownInsertModeAction}
 * |i_<Home>|             {@link com.flop.idea.fim.action.motion.leftright.MotionFirstColumnInsertModeAction}
 * |i_<C-Home>|           {@link com.flop.idea.fim.action.motion.updown.MotionGotoLineFirstInsertAction}
 * |i_<End>|              {@link com.flop.idea.fim.action.motion.leftright.MotionLastColumnInsertAction}
 * |i_<C-End>|            {@link com.flop.idea.fim.action.motion.updown.MotionGotoLineLastEndInsertAction}
 * |i_<Insert>|           {@link com.flop.idea.fim.action.change.insert.InsertInsertAction}
 * |i_<PageUp>|           {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageUpInsertModeAction}
 * |i_<PageDown>|         {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageDownInsertModeAction}
 * |i_<F1>|               IntelliJ help
 * |i_<Insert>|           IntelliJ editor toggle insert/replace
 * |i_CTRL-X_index|       TO BE IMPLEMENTED
 *
 *
 * 2. Normal mode
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |CTRL-A|               {@link com.flop.idea.fim.action.change.change.number.ChangeNumberIncAction}
 * |CTRL-B|               {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageUpAction}
 * |CTRL-C|               TO BE IMPLEMENTED
 * |CTRL-D|               {@link com.flop.idea.fim.action.motion.scroll.MotionScrollHalfPageDownAction}
 * |CTRL-E|               {@link com.flop.idea.fim.action.motion.scroll.MotionScrollLineDownAction}
 * |CTRL-F|               {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageDownAction}
 * |CTRL-G|               {@link com.flop.idea.fim.action.file.FileGetFileInfoAction}
 * |<BS>|                 {@link com.flop.idea.fim.action.motion.leftright.MotionLeftWrapAction}
 * |CTRL-H|               {@link com.flop.idea.fim.action.motion.leftright.MotionLeftWrapAction}
 * |<Tab>|                TO BE IMPLEMENTED
 * |CTRL-I|               {@link com.flop.idea.fim.action.motion.mark.MotionJumpNextAction}
 * |<NL>|                 {@link com.flop.idea.fim.action.motion.updown.MotionDownNotLineWiseAction}
 * |CTRL-J|               TO BE IMPLEMENTED
 * |CTRL-L|               not applicable
 * |<CR>|                 {@link com.flop.idea.fim.action.motion.updown.EnterNormalAction}
 * |CTRL-M|               {@link com.flop.idea.fim.action.motion.updown.MotionDownFirstNonSpaceAction}
 * |CTRL-N|               {@link com.flop.idea.fim.action.motion.updown.MotionDownCtrlNAction}
 * |CTRL-O|               {@link com.flop.idea.fim.action.motion.mark.MotionJumpPreviousAction}
 * |CTRL-P|               {@link com.flop.idea.fim.action.motion.updown.MotionUpCtrlPAction}
 * |CTRL-R|               {@link com.flop.idea.fim.action.change.RedoAction}
 * |CTRL-T|               TO BE IMPLEMENTED
 * |CTRL-U|               {@link com.flop.idea.fim.action.motion.scroll.MotionScrollHalfPageUpAction}
 * |CTRL-V|               {@link com.flop.idea.fim.action.motion.visual.VisualToggleBlockModeAction}
 * |CTRL-W|               see window commands
 * |CTRL-X|               {@link com.flop.idea.fim.action.change.change.number.ChangeNumberDecAction}
 * |CTRL-Y|               {@link com.flop.idea.fim.action.motion.scroll.MotionScrollLineUpAction}
 * |CTRL-Z|               TO BE IMPLEMENTED
 * |CTRL-]|               {@link com.flop.idea.fim.action.motion.search.GotoDeclarationAction}
 * |CTRL-6|               {@link com.flop.idea.fim.action.file.FilePreviousAction}
 * |CTRL-\CTRL-N|         {@link com.flop.idea.fim.action.ResetModeAction}
 * |<Space>|              {@link com.flop.idea.fim.action.motion.leftright.MotionRightWrapAction}
 * |!|                    {@link com.flop.idea.fim.action.change.change.FilterMotionAction}
 * |!!|                   translated to !_
 * |quote|                handled by command key parser
 * |#|                    {@link com.flop.idea.fim.action.motion.search.SearchWholeWordBackwardAction}
 * |$|                    {@link com.flop.idea.fim.action.motion.leftright.MotionLastColumnAction}
 * |%|                    {@link com.flop.idea.fim.action.motion.updown.MotionPercentOrMatchAction}
 * |&|                    {@link com.flop.idea.fim.action.change.change.ChangeLastSearchReplaceAction}
 * |'|                    {@link com.flop.idea.fim.action.motion.mark.MotionGotoMarkLineAction}
 * |''|                   ?
 * ...
 * |(|                    {@link com.flop.idea.fim.action.motion.text.MotionSentencePreviousStartAction}
 * |)|                    {@link com.flop.idea.fim.action.motion.text.MotionSentenceNextStartAction}
 * |star|                 {@link com.flop.idea.fim.action.motion.search.SearchWholeWordForwardAction}
 * |+|                    {@link com.flop.idea.fim.action.motion.updown.MotionDownFirstNonSpaceAction}
 * |,|                    {@link com.flop.idea.fim.action.motion.leftright.MotionLastMatchCharReverseAction}
 * |-|                    {@link com.flop.idea.fim.action.motion.updown.MotionUpFirstNonSpaceAction}
 * |.|                    {@link com.flop.idea.fim.action.change.RepeatChangeAction}
 * |/|                    {@link com.flop.idea.fim.action.motion.search.SearchEntryFwdAction}
 * |:|                    {@link com.flop.idea.fim.action.ExEntryAction}
 * |;|                    {@link com.flop.idea.fim.action.motion.leftright.MotionLastMatchCharAction}
 * |<|                    {@link com.flop.idea.fim.action.change.shift.ShiftLeftMotionAction}
 * |<<|                   translated to <_
 * |=|                    {@link com.flop.idea.fim.action.change.shift.AutoIndentMotionAction}
 * |==|                   translated to =_
 * |>|                    {@link com.flop.idea.fim.action.change.shift.ShiftRightMotionAction}
 * |>>|                   translated to >_
 * |?|                    {@link com.flop.idea.fim.action.motion.search.SearchEntryRevAction}
 * |@|                    {@link com.flop.idea.fim.action.macro.PlaybackRegisterAction}
 * |A|                    {@link com.flop.idea.fim.action.change.insert.InsertAfterLineEndAction}
 * |B|                    {@link com.flop.idea.fim.action.motion.text.MotionBigWordLeftAction}
 * |C|                    {@link com.flop.idea.fim.action.change.change.ChangeEndOfLineAction}
 * |D|                    {@link com.flop.idea.fim.action.change.delete.DeleteEndOfLineAction}
 * |E|                    {@link com.flop.idea.fim.action.motion.text.MotionBigWordEndRightAction}
 * |F|                    {@link com.flop.idea.fim.action.motion.leftright.MotionLeftMatchCharAction}
 * |G|                    {@link com.flop.idea.fim.action.motion.updown.MotionGotoLineLastAction}
 * |H|                    {@link com.flop.idea.fim.action.motion.screen.MotionFirstScreenLineAction}
 * |H|                    {@link com.flop.idea.fim.action.motion.screen.MotionOpPendingFirstScreenLineAction}
 * |I|                    {@link com.flop.idea.fim.action.change.insert.InsertBeforeFirstNonBlankAction}
 * |J|                    {@link com.flop.idea.fim.action.change.delete.DeleteJoinLinesSpacesAction}
 * |K|                    {@link com.flop.idea.fim.action.editor.FimQuickJavaDoc}
 * |L|                    {@link com.flop.idea.fim.action.motion.screen.MotionLastScreenLineAction}
 * |L|                    {@link com.flop.idea.fim.action.motion.screen.MotionOpPendingLastScreenLineAction}
 * |M|                    {@link com.flop.idea.fim.action.motion.screen.MotionMiddleScreenLineAction}
 * |N|                    {@link com.flop.idea.fim.action.motion.search.SearchAgainPreviousAction}
 * |O|                    {@link com.flop.idea.fim.action.change.insert.InsertNewLineAboveAction}
 * |P|                    {@link com.flop.idea.fim.action.copy.PutTextBeforeCursorAction}
 * |Q|                    TO BE IMPLEMENTED
 * |R|                    {@link com.flop.idea.fim.action.change.change.ChangeReplaceAction}
 * |S|                    {@link com.flop.idea.fim.action.change.change.ChangeLineAction}
 * |T|                    {@link com.flop.idea.fim.action.motion.leftright.MotionLeftTillMatchCharAction}
 * |U|                    ?
 * |V|                    {@link com.flop.idea.fim.action.motion.visual.VisualToggleLineModeAction}
 * |W|                    {@link com.flop.idea.fim.action.motion.text.MotionBigWordRightAction}
 * |X|                    {@link com.flop.idea.fim.action.change.delete.DeleteCharacterLeftAction}
 * |Y|                    {@link com.flop.idea.fim.action.copy.YankLineAction}
 * |ZZ|                   {@link com.flop.idea.fim.action.file.FileSaveCloseAction}
 * |ZQ|                   {@link com.flop.idea.fim.action.file.FileSaveCloseAction}
 * |[|                    see bracket commands
 * |]|                    see bracket commands
 * |^|                    {@link com.flop.idea.fim.action.motion.leftright.MotionFirstNonSpaceAction}
 * |_|                    {@link com.flop.idea.fim.action.motion.updown.MotionDownLess1FirstNonSpaceAction}
 * |`|                    {@link com.flop.idea.fim.action.motion.mark.MotionGotoMarkAction}
 * |``|                   ?
 * ...
 * |0|                    {@link com.flop.idea.fim.action.motion.leftright.MotionFirstColumnAction}
 * |a|                    {@link com.flop.idea.fim.action.change.insert.InsertAfterCursorAction}
 * |b|                    {@link com.flop.idea.fim.action.motion.text.MotionWordLeftAction}
 * |c|                    {@link com.flop.idea.fim.action.change.change.ChangeMotionAction}
 * |cc|                   translated to c_
 * |d|                    {@link com.flop.idea.fim.action.change.delete.DeleteMotionAction}
 * |dd|                   translated to d_
 * |do|                   TO BE IMPLEMENTED
 * |dp|                   TO BE IMPLEMENTED
 * |e|                    {@link com.flop.idea.fim.action.motion.text.MotionWordEndRightAction}
 * |f|                    {@link com.flop.idea.fim.action.motion.leftright.MotionRightMatchCharAction}
 * |g|                    see commands starting with 'g'
 * |h|                    {@link com.flop.idea.fim.action.motion.leftright.MotionLeftAction}
 * |i|                    {@link com.flop.idea.fim.action.change.insert.InsertBeforeCursorAction}
 * |j|                    {@link com.flop.idea.fim.action.motion.updown.MotionDownAction}
 * |k|                    {@link com.flop.idea.fim.action.motion.updown.MotionUpAction}
 * |l|                    {@link com.flop.idea.fim.action.motion.leftright.MotionRightAction}
 * |m|                    {@link com.flop.idea.fim.action.motion.mark.MotionMarkAction}
 * |n|                    {@link com.flop.idea.fim.action.motion.search.SearchAgainNextAction}
 * |o|                    {@link com.flop.idea.fim.action.change.insert.InsertNewLineBelowAction}
 * |p|                    {@link com.flop.idea.fim.action.copy.PutTextAfterCursorAction}
 * |q|                    {@link com.flop.idea.fim.action.macro.ToggleRecordingAction}
 * |r|                    {@link com.flop.idea.fim.action.change.change.ChangeCharacterAction}
 * |s|                    {@link com.flop.idea.fim.action.change.change.ChangeCharactersAction}
 * |t|                    {@link com.flop.idea.fim.action.motion.leftright.MotionRightTillMatchCharAction}
 * |u|                    {@link com.flop.idea.fim.action.change.UndoAction}
 * |v|                    {@link com.flop.idea.fim.action.motion.visual.VisualToggleCharacterModeAction}
 * |w|                    {@link com.flop.idea.fim.action.motion.text.MotionWordRightAction}
 * |x|                    {@link com.flop.idea.fim.action.change.delete.DeleteCharacterRightAction}
 * |y|                    {@link com.flop.idea.fim.action.copy.YankMotionAction}
 * |yy|                   translated to y_
 * |z|                    see commands starting with 'z'
 * |{|                    {@link com.flop.idea.fim.action.motion.text.MotionParagraphPreviousAction}
 * |bar|                  {@link com.flop.idea.fim.action.motion.leftright.MotionColumnAction}
 * |}|                    {@link com.flop.idea.fim.action.motion.text.MotionParagraphNextAction}
 * |~|                    {@link com.flop.idea.fim.action.change.change.ChangeCaseToggleCharacterAction}
 * |<C-End>|              {@link com.flop.idea.fim.action.motion.updown.MotionGotoLineLastEndAction}
 * |<C-Home>|             {@link com.flop.idea.fim.action.motion.updown.MotionGotoLineFirstAction}
 * |<C-Left>|             {@link com.flop.idea.fim.action.motion.text.MotionWordLeftAction}
 * |<C-Right>|            {@link com.flop.idea.fim.action.motion.text.MotionWordRightAction}
 * |<C-Down>|             {@link com.flop.idea.fim.action.motion.scroll.CtrlDownAction}
 * |<C-Up>|               {@link com.flop.idea.fim.action.motion.scroll.CtrlUpAction}
 * |<Del>|                {@link com.flop.idea.fim.action.change.delete.DeleteCharacterAction}
 * |<Down>|               {@link com.flop.idea.fim.action.motion.updown.MotionArrowDownAction}
 * |<End>|                {@link com.flop.idea.fim.action.motion.leftright.MotionEndAction}
 * |<F1>|                 IntelliJ help
 * |<Home>|               {@link com.flop.idea.fim.action.motion.leftright.MotionHomeAction}
 * |<Insert>|             {@link com.flop.idea.fim.action.change.insert.InsertBeforeCursorAction}
 * |<Left>|               {@link com.flop.idea.fim.action.motion.leftright.MotionArrowLeftAction}
 * |<PageDown>|           {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageDownAction}
 * |<PageUp>|             {@link com.flop.idea.fim.action.motion.scroll.MotionScrollPageUpAction}
 * |<Right>|              {@link com.flop.idea.fim.action.motion.leftright.MotionArrowRightAction}
 * |<S-Down>|             {@link com.flop.idea.fim.action.motion.updown.MotionShiftDownAction}
 * |<S-Left>|             {@link com.flop.idea.fim.action.motion.leftright.MotionShiftLeftAction}
 * |<S-Right>|            {@link com.flop.idea.fim.action.motion.leftright.MotionShiftRightAction}
 * |<S-Up>|               {@link com.flop.idea.fim.action.motion.updown.MotionShiftUpAction}
 * |<S-Home>|             {@link com.flop.idea.fim.action.motion.leftright.MotionShiftHomeAction}
 * |<S-End>|              {@link com.flop.idea.fim.action.motion.leftright.MotionShiftEndAction}
 * |<Up>|                 {@link com.flop.idea.fim.action.motion.updown.MotionArrowUpAction}
 *
 *
 * 2.1. Text objects
 *
 * Text object commands are listed in the visual mode section.
 *
 *
 * 2.2. Window commands
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |CTRL-W_+|             TO BE IMPLEMENTED
 * |CTRL-W_-|             TO BE IMPLEMENTED
 * |CTRL-W_<|             TO BE IMPLEMENTED
 * |CTRL-W_=|             TO BE IMPLEMENTED
 * |CTRL-W_>|             TO BE IMPLEMENTED
 * |CTRL-W_H|             TO BE IMPLEMENTED
 * |CTRL-W_J|             TO BE IMPLEMENTED
 * |CTRL-W_K|             TO BE IMPLEMENTED
 * |CTRL-W_L|             TO BE IMPLEMENTED
 * |CTRL-W_P|             TO BE IMPLEMENTED
 * |CTRL-W_R|             TO BE IMPLEMENTED
 * |CTRL-W_S|             {@link com.flop.idea.fim.action.window.HorizontalSplitAction}
 * |CTRL-W_T|             TO BE IMPLEMENTED
 * |CTRL-W_W|             {@link com.flop.idea.fim.action.window.WindowPrevAction}
 * |CTRL-W_]|             TO BE IMPLEMENTED
 * |CTRL-W_^|             TO BE IMPLEMENTED
 * |CTRL-W__|             TO BE IMPLEMENTED
 * |CTRL-W_b|             TO BE IMPLEMENTED
 * |CTRL-W_c|             {@link com.flop.idea.fim.action.window.CloseWindowAction}
 * |CTRL-W_d|             TO BE IMPLEMENTED
 * |CTRL-W_f|             TO BE IMPLEMENTED
 * |CTRL-W-F|             TO BE IMPLEMENTED
 * |CTRL-W-g]|            TO BE IMPLEMENTED
 * |CTRL-W-g}|            TO BE IMPLEMENTED
 * |CTRL-W-gf|            TO BE IMPLEMENTED
 * |CTRL-W-gF|            TO BE IMPLEMENTED
 * |CTRL-W_h|             {@link com.flop.idea.fim.action.window.WindowLeftAction}
 * |CTRL-W_i|             TO BE IMPLEMENTED
 * |CTRL-W_j|             {@link com.flop.idea.fim.action.window.WindowDownAction}
 * |CTRL-W_k|             {@link com.flop.idea.fim.action.window.WindowUpAction}
 * |CTRL-W_l|             {@link com.flop.idea.fim.action.window.WindowRightAction}
 * |CTRL-W_n|             TO BE IMPLEMENTED
 * |CTRL-W_o|             {@link com.flop.idea.fim.action.window.WindowOnlyAction}
 * |CTRL-W_p|             TO BE IMPLEMENTED
 * |CTRL-W_q|             TO BE IMPLEMENTED
 * |CTRL-W_r|             TO BE IMPLEMENTED
 * |CTRL-W_s|             {@link com.flop.idea.fim.action.window.HorizontalSplitAction}
 * |CTRL-W_t|             TO BE IMPLEMENTED
 * |CTRL-W_v|             {@link com.flop.idea.fim.action.window.VerticalSplitAction}
 * |CTRL-W_w|             {@link com.flop.idea.fim.action.window.WindowNextAction}
 * |CTRL-W_x|             TO BE IMPLEMENTED
 * |CTRL-W_z|             TO BE IMPLEMENTED
 * |CTRL-W_bar|           TO BE IMPLEMENTED
 * |CTRL-W_}|             TO BE IMPLEMENTED
 * |CTRL-W_<Down>|        {@link com.flop.idea.fim.action.window.WindowDownAction}
 * |CTRL-W_<Up>|          {@link com.flop.idea.fim.action.window.WindowUpAction}
 * |CTRL-W_<Left>|        {@link com.flop.idea.fim.action.window.WindowLeftAction}
 * |CTRL-W_<Right>|       {@link com.flop.idea.fim.action.window.WindowRightAction}
 * |CTRL-W_CTRL-H|        {@link com.flop.idea.fim.action.window.WindowLeftAction}
 * |CTRL-W_CTRL-J|        {@link com.flop.idea.fim.action.window.WindowDownAction}
 * |CTRL-W_CTRL-K|        {@link com.flop.idea.fim.action.window.WindowUpAction}
 * |CTRL-W_CTRL-L|        {@link com.flop.idea.fim.action.window.WindowRightAction}
 *
 *
 * 2.3. Square bracket commands
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 * |[_CTRL-D|             TO BE IMPLEMENTED
 * |[_CTRL-I|             TO BE IMPLEMENTED
 * |[#|                   TO BE IMPLEMENTED
 * |['|                   TO BE IMPLEMENTED
 * |[(|                   {@link com.flop.idea.fim.action.motion.text.MotionUnmatchedParenOpenAction}
 * |[star|                TO BE IMPLEMENTED
 * |[`|                   TO BE IMPLEMENTED
 * |[/|                   TO BE IMPLEMENTED
 * |[D|                   TO BE IMPLEMENTED
 * |[I|                   TO BE IMPLEMENTED
 * |[M|                   {@link com.flop.idea.fim.action.motion.text.MotionMethodPreviousEndAction}
 * |[P|                   {@link com.flop.idea.fim.action.copy.PutVisualTextBeforeCursorNoIndentAction}
 * |[P|                   {@link com.flop.idea.fim.action.copy.PutTextBeforeCursorNoIndentAction}
 * |[[|                   {@link com.flop.idea.fim.action.motion.text.MotionSectionBackwardStartAction}
 * |[]|                   {@link com.flop.idea.fim.action.motion.text.MotionSectionBackwardEndAction}
 * |[c|                   TO BE IMPLEMENTED
 * |[d|                   TO BE IMPLEMENTED
 * |[f|                   TO BE IMPLEMENTED
 * |[i|                   TO BE IMPLEMENTED
 * |[m|                   {@link com.flop.idea.fim.action.motion.text.MotionMethodPreviousStartAction}
 * |[p|                   {@link com.flop.idea.fim.action.copy.PutVisualTextAfterCursorNoIndentAction}
 * |[p|                   {@link com.flop.idea.fim.action.copy.PutTextAfterCursorNoIndentAction}
 * |[s|                   TO BE IMPLEMENTED
 * |[z|                   TO BE IMPLEMENTED
 * |[{|                   {@link com.flop.idea.fim.action.motion.text.MotionUnmatchedBraceOpenAction}
 * |]_CTRL-D|             TO BE IMPLEMENTED
 * |]_CTRL-I|             TO BE IMPLEMENTED
 * |]#|                   TO BE IMPLEMENTED
 * |]'|                   TO BE IMPLEMENTED
 * |])|                   {@link com.flop.idea.fim.action.motion.text.MotionUnmatchedParenCloseAction}
 * |]star|                TO BE IMPLEMENTED
 * |]`|                   TO BE IMPLEMENTED
 * |]/|                   TO BE IMPLEMENTED
 * |]D|                   TO BE IMPLEMENTED
 * |]I|                   TO BE IMPLEMENTED
 * |]M|                   {@link com.flop.idea.fim.action.motion.text.MotionMethodNextEndAction}
 * |]P|                   {@link com.flop.idea.fim.action.copy.PutVisualTextBeforeCursorNoIndentAction}
 * |]P|                   {@link com.flop.idea.fim.action.copy.PutTextBeforeCursorNoIndentAction}
 * |][|                   {@link com.flop.idea.fim.action.motion.text.MotionSectionForwardEndAction}
 * |]]|                   {@link com.flop.idea.fim.action.motion.text.MotionSectionForwardStartAction}
 * |]c|                   TO BE IMPLEMENTED
 * |]d|                   TO BE IMPLEMENTED
 * |]f|                   TO BE IMPLEMENTED
 * |]i|                   TO BE IMPLEMENTED
 * |]m|                   {@link com.flop.idea.fim.action.motion.text.MotionMethodNextStartAction}
 * |]p|                   {@link com.flop.idea.fim.action.copy.PutVisualTextAfterCursorNoIndentAction}
 * |]p|                   {@link com.flop.idea.fim.action.copy.PutTextAfterCursorNoIndentAction}
 * |]s|                   TO BE IMPLEMENTED
 * |]z|                   TO BE IMPLEMENTED
 * |]}|                   {@link com.flop.idea.fim.action.motion.text.MotionUnmatchedBraceCloseAction}
 *
 *
 * 2.4. Commands starting with 'g'
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |g_CTRL-A|             not applicable
 * |g_CTRL-G|             {@link com.flop.idea.fim.action.file.FileGetLocationInfoAction}
 * |g_CTRL-H|             {@link com.flop.idea.fim.action.motion.select.SelectEnableBlockModeAction}
 * |g_CTRL-]|             TO BE IMPLEMENTED
 * |g#|                   {@link com.flop.idea.fim.action.motion.search.SearchWordBackwardAction}
 * |g$|                   {@link com.flop.idea.fim.action.motion.leftright.MotionLastScreenColumnAction}
 * |g&|                   {@link com.flop.idea.fim.action.change.change.ChangeLastGlobalSearchReplaceAction}
 * |v_g'|                 {@link com.flop.idea.fim.action.motion.mark.MotionGotoFileMarkLineNoSaveJumpAction}
 * |g'|                   {@link com.flop.idea.fim.action.motion.mark.MotionGotoMarkLineNoSaveJumpAction}
 * |g`|                   {@link com.flop.idea.fim.action.motion.mark.MotionGotoMarkNoSaveJumpAction}
 * |gstar|                {@link com.flop.idea.fim.action.motion.search.SearchWordForwardAction}
 * |g+|                   TO BE IMPLEMENTED
 * |g,|                   TO BE IMPLEMENTED
 * |g-|                   TO BE IMPLEMENTED
 * |g0|                   {@link com.flop.idea.fim.action.motion.leftright.MotionFirstScreenColumnAction}
 * |g8|                   {@link com.flop.idea.fim.action.file.FileGetHexAction}
 * |g;|                   TO BE IMPLEMENTED
 * |g<|                   TO BE IMPLEMENTED
 * |g?|                   TO BE IMPLEMENTED
 * |g?g?|                 TO BE IMPLEMENTED
 * |gD|                   {@link com.flop.idea.fim.action.motion.search.GotoDeclarationAction}
 * |gE|                   {@link com.flop.idea.fim.action.motion.text.MotionBigWordEndLeftAction}
 * |gF|                   TO BE IMPLEMENTED
 * |gH|                   {@link com.flop.idea.fim.action.motion.select.SelectEnableLineModeAction}
 * |gI|                   {@link com.flop.idea.fim.action.change.insert.InsertLineStartAction}
 * |gJ|                   {@link com.flop.idea.fim.action.change.delete.DeleteJoinLinesAction}
 * |gN|                   {@link com.flop.idea.fim.action.motion.gn.VisualSelectPreviousSearch}
 * |gN|                   {@link com.flop.idea.fim.action.motion.gn.GnPreviousTextObject}
 * |gP|                   {@link com.flop.idea.fim.action.copy.PutVisualTextBeforeCursorMoveCursorAction}
 * |gP|                   {@link com.flop.idea.fim.action.copy.PutTextBeforeCursorActionMoveCursor}
 * |gQ|                   TO BE IMPLEMENTED
 * |gR|                   TO BE IMPLEMENTED
 * |gT|                   {@link com.flop.idea.fim.action.window.tabs.PreviousTabAction}
 * |gU|                   {@link com.flop.idea.fim.action.change.change.ChangeCaseUpperMotionAction}
 * |gV|                   TO BE IMPLEMENTED
 * |g]|                   TO BE IMPLEMENTED
 * |g^|                   {@link com.flop.idea.fim.action.motion.leftright.MotionFirstScreenNonSpaceAction}
 * |g_|                   {@link com.flop.idea.fim.action.motion.leftright.MotionLastNonSpaceAction}
 * |ga|                   {@link com.flop.idea.fim.action.file.FileGetAsciiAction}
 * |gd|                   {@link com.flop.idea.fim.action.motion.search.GotoDeclarationAction}
 * |ge|                   {@link com.flop.idea.fim.action.motion.text.MotionWordEndLeftAction}
 * |gf|                   TO BE IMPLEMENTED
 * |gg|                   {@link com.flop.idea.fim.action.motion.updown.MotionGotoLineFirstAction}
 * |gh|                   {@link com.flop.idea.fim.action.motion.select.SelectEnableCharacterModeAction}
 * |gi|                   {@link com.flop.idea.fim.action.change.insert.InsertAtPreviousInsertAction}
 * |gj|                   TO BE IMPLEMENTED
 * |gk|                   {@link com.flop.idea.fim.action.motion.updown.MotionUpNotLineWiseAction}
 * |gn|                   {@link com.flop.idea.fim.action.motion.gn.VisualSelectNextSearch}
 * |gn|                   {@link com.flop.idea.fim.action.motion.gn.GnNextTextObject}
 * |gm|                   {@link com.flop.idea.fim.action.macro.MotionMiddleColumnAction}
 * |go|                   {@link com.flop.idea.fim.action.motion.text.MotionNthCharacterAction}
 * |gp|                   {@link com.flop.idea.fim.action.copy.PutVisualTextAfterCursorMoveCursorAction}
 * |gp|                   {@link com.flop.idea.fim.action.copy.PutTextAfterCursorActionMoveCursor}
 * |gq|                   {@link com.flop.idea.fim.action.change.change.ReformatCodeMotionAction}
 * |gr|                   TO BE IMPLEMENTED
 * |gs|                   TO BE IMPLEMENTED
 * |gt|                   {@link com.flop.idea.fim.action.window.tabs.NextTabAction}
 * |gu|                   {@link com.flop.idea.fim.action.change.change.ChangeCaseLowerMotionAction}
 * |gv|                   {@link com.flop.idea.fim.action.motion.visual.VisualSelectPreviousAction}
 * |gw|                   TO BE IMPLEMENTED
 * |g@|                   {@link com.flop.idea.fim.action.change.OperatorAction}
 * |g~|                   {@link com.flop.idea.fim.action.change.change.ChangeCaseToggleMotionAction}
 * |g<Down>|              TO BE IMPLEMENTED
 * |g<End>|               {@link com.flop.idea.fim.action.motion.leftright.MotionLastScreenColumnAction}
 * |g<Home>|              {@link com.flop.idea.fim.action.motion.leftright.MotionFirstScreenColumnAction}
 * |g<Up>|                {@link com.flop.idea.fim.action.motion.updown.MotionUpNotLineWiseAction}
 *
 *
 * 2.5. Commands starting with 'z'
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 * |z<CR>|                {@link com.flop.idea.fim.action.motion.scroll.MotionScrollFirstScreenLineStartAction}
 * |z+|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollFirstScreenLinePageStartAction}
 * |z-|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollLastScreenLineStartAction}
 * |z.|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollMiddleScreenLineStartAction}
 * |z=|                   TO BE IMPLEMENTED
 * |zA|                   TO BE IMPLEMENTED
 * |zC|                   {@link com.flop.idea.fim.action.fold.FimCollapseRegionRecursively}
 * |zD|                   TO BE IMPLEMENTED
 * |zE|                   TO BE IMPLEMENTED
 * |zF|                   TO BE IMPLEMENTED
 * |zG|                   TO BE IMPLEMENTED
 * |zH|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollHalfWidthLeftAction}
 * |zL|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollHalfWidthRightAction}
 * |zM|                   {@link com.flop.idea.fim.action.fold.FimCollapseAllRegions}
 * |zN|                   TO BE IMPLEMENTED
 * |zO|                   {@link com.flop.idea.fim.action.fold.FimExpandRegionRecursively}
 * |zR|                   {@link com.flop.idea.fim.action.fold.FimExpandAllRegions}
 * |zW|                   TO BE IMPLEMENTED
 * |zX|                   TO BE IMPLEMENTED
 * |z^|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollLastScreenLinePageStartAction}
 * |za|                   TO BE IMPLEMENTED
 * |zb|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollLastScreenLineAction}
 * |zc|                   {@link com.flop.idea.fim.action.fold.FimCollapseRegion}
 * |zd|                   not applicable
 * |ze|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollLastScreenColumnAction}
 * |zf|                   not applicable
 * |zg|                   TO BE IMPLEMENTED
 * |zh|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollColumnRightAction}
 * |zi|                   TO BE IMPLEMENTED
 * |zj|                   TO BE IMPLEMENTED
 * |zk|                   TO BE IMPLEMENTED
 * |zl|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollColumnLeftAction}
 * |zm|                   TO BE IMPLEMENTED
 * |zn|                   TO BE IMPLEMENTED
 * |zo|                   {@link com.flop.idea.fim.action.fold.FimExpandRegion}
 * |zr|                   TO BE IMPLEMENTED
 * |zs|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollFirstScreenColumnAction}
 * |zt|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollFirstScreenLineAction}
 * |zv|                   TO BE IMPLEMENTED
 * |zw|                   TO BE IMPLEMENTED
 * |zx|                   TO BE IMPLEMENTED
 * |zz|                   {@link com.flop.idea.fim.action.motion.scroll.MotionScrollMiddleScreenLineAction}
 * |z<Left>|              {@link com.flop.idea.fim.action.motion.scroll.MotionScrollColumnRightAction}
 * |z<Right>|             {@link com.flop.idea.fim.action.motion.scroll.MotionScrollColumnLeftAction}
 *
 *
 * 3. Visual mode
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |v_CTRL-\_CTRL-N|      {@link com.flop.idea.fim.action.motion.visual.VisualExitModeAction}
 * |v_CTRL-\_CTRL-G|      TO BE IMPLEMENTED
 * |v_CTRL-A|             {@link com.flop.idea.fim.action.change.change.number.ChangeVisualNumberIncAction}
 * |v_CTRL-C|             {@link com.flop.idea.fim.action.motion.visual.VisualExitModeAction}
 * |v_CTRL-G|             {@link com.flop.idea.fim.action.motion.select.SelectToggleVisualMode}
 * |v_<BS>|               NVO mapping
 * |v_CTRL-H|             NVO mapping
 * |v_CTRL-O|             TO BE IMPLEMENTED
 * |v_CTRL-V|             NVO mapping
 * |v_<Esc>|              {@link com.flop.idea.fim.action.motion.visual.VisualExitModeAction}
 * |v_CTRL-X|             {@link com.flop.idea.fim.action.change.change.number.ChangeVisualNumberDecAction}
 * |v_CTRL-]|             TO BE IMPLEMENTED
 * |v_!|                  {@link com.flop.idea.fim.action.change.change.FilterVisualLinesAction}
 * |v_:|                  NVO mapping
 * |v_<|                  {@link com.flop.idea.fim.action.change.shift.ShiftLeftVisualAction}
 * |v_=|                  {@link com.flop.idea.fim.action.change.change.AutoIndentLinesVisualAction}
 * |v_>|                  {@link com.flop.idea.fim.action.change.shift.ShiftRightVisualAction}
 * |v_b_A|                {@link com.flop.idea.fim.action.change.insert.VisualBlockAppendAction}
 * |v_C|                  {@link com.flop.idea.fim.action.change.change.ChangeVisualLinesEndAction}
 * |v_D|                  {@link com.flop.idea.fim.action.change.delete.DeleteVisualLinesEndAction}
 * |v_b_I|                {@link com.flop.idea.fim.action.change.insert.VisualBlockInsertAction}
 * |v_J|                  {@link com.flop.idea.fim.action.change.delete.DeleteJoinVisualLinesSpacesAction}
 * |v_K|                  TO BE IMPLEMENTED
 * |v_O|                  {@link com.flop.idea.fim.action.motion.visual.VisualSwapEndsBlockAction}
 * |v_P|                  {@link com.flop.idea.fim.action.copy.PutVisualTextBeforeCursorAction}
 * |v_R|                  {@link com.flop.idea.fim.action.change.change.ChangeVisualLinesAction}
 * |v_S|                  {@link com.flop.idea.fim.action.change.change.ChangeVisualLinesAction}
 * |v_U|                  {@link com.flop.idea.fim.action.change.change.ChangeCaseUpperVisualAction}
 * |v_V|                  NV mapping
 * |v_X|                  {@link com.flop.idea.fim.action.change.delete.DeleteVisualLinesAction}
 * |v_Y|                  {@link com.flop.idea.fim.action.copy.YankVisualLinesAction}
 * |v_aquote|             {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockDoubleQuoteAction}
 * |v_a'|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockSingleQuoteAction}
 * |v_a(|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockParenAction}
 * |v_a)|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockParenAction}
 * |v_a<|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockAngleAction}
 * |v_a>|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockAngleAction}
 * |v_aB|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockBraceAction}
 * |v_aW|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBigWordAction}
 * |v_a[|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockBracketAction}
 * |v_a]|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockBracketAction}
 * |v_a`|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockBackQuoteAction}
 * |v_ab|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockParenAction}
 * |v_ap|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterParagraphAction}
 * |v_as|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterSentenceAction}
 * |v_at|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockTagAction}
 * |v_aw|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterWordAction}
 * |v_a{|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockBraceAction}
 * |v_a}|                 {@link com.flop.idea.fim.action.motion.object.MotionOuterBlockBraceAction}
 * |v_c|                  {@link com.flop.idea.fim.action.change.change.ChangeVisualAction}
 * |v_d|                  {@link com.flop.idea.fim.action.change.delete.DeleteVisualAction}
 * |v_gCTRL-A|            {@link com.flop.idea.fim.action.change.change.number.ChangeVisualNumberAvalancheIncAction}
 * |v_gCTRL-X|            {@link com.flop.idea.fim.action.change.change.number.ChangeVisualNumberAvalancheDecAction}
 * |v_gJ|                 {@link com.flop.idea.fim.action.change.delete.DeleteJoinVisualLinesAction}
 * |v_gq|                 {@link com.flop.idea.fim.action.change.change.ReformatCodeVisualAction}
 * |v_gv|                 {@link com.flop.idea.fim.action.motion.visual.VisualSwapSelectionsAction}
 * |v_g`|                 {@link com.flop.idea.fim.action.motion.mark.MotionGotoFileMarkNoSaveJumpAction}
 * |v_g@|                 {@link com.flop.idea.fim.action.change.VisualOperatorAction}
 * |v_iquote|             {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockDoubleQuoteAction}
 * |v_i'|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockSingleQuoteAction}
 * |v_i(|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockParenAction}
 * |v_i)|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockParenAction}
 * |v_i<|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockAngleAction}
 * |v_i>|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockAngleAction}
 * |v_iB|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockBraceAction}
 * |v_iW|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBigWordAction}
 * |v_i[|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockBracketAction}
 * |v_i]|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockBracketAction}
 * |v_i`|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockBackQuoteAction}
 * |v_ib|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockParenAction}
 * |v_ip|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerParagraphAction}
 * |v_is|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerSentenceAction}
 * |v_it|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockTagAction}
 * |v_iw|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerWordAction}
 * |v_i{|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockBraceAction}
 * |v_i}|                 {@link com.flop.idea.fim.action.motion.object.MotionInnerBlockBraceAction}
 * |v_o|                  {@link com.flop.idea.fim.action.motion.visual.VisualSwapEndsAction}
 * |v_p|                  {@link com.flop.idea.fim.action.copy.PutVisualTextAfterCursorAction}
 * |v_r|                  {@link com.flop.idea.fim.action.change.change.ChangeVisualCharacterAction}
 * |v_s|                  {@link com.flop.idea.fim.action.change.change.ChangeVisualAction}
 * |v_u|                  {@link com.flop.idea.fim.action.change.change.ChangeCaseLowerVisualAction}
 * |v_v|                  NV mapping
 * |v_x|                  {@link com.flop.idea.fim.action.change.delete.DeleteVisualAction}
 * |v_y|                  {@link com.flop.idea.fim.action.copy.YankVisualAction}
 * |v_~|                  {@link com.flop.idea.fim.action.change.change.ChangeCaseToggleVisualAction}
 * |v_`|                  {@link com.flop.idea.fim.action.motion.mark.MotionGotoFileMarkAction}
 * |v_'|                  {@link com.flop.idea.fim.action.motion.mark.MotionGotoFileMarkLineAction}
 *
 *
 * 4. Select mode
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 * |<BS>|                 {@link com.flop.idea.fim.action.motion.select.SelectDeleteAction}
 * |<CR>|                 {@link com.flop.idea.fim.action.motion.select.SelectEnterAction}
 * |<DEL>|                {@link com.flop.idea.fim.action.motion.select.SelectDeleteAction}
 * |<ESC>|                {@link com.flop.idea.fim.action.motion.select.SelectEscapeAction}
 * |<C-G>|                {@link com.flop.idea.fim.action.motion.select.SelectToggleVisualMode}
 * |<S-Down>|             {@link com.flop.idea.fim.action.motion.updown.MotionShiftDownAction}
 * |<S-Left>|             {@link com.flop.idea.fim.action.motion.leftright.MotionShiftLeftAction}
 * |<S-Right>|            {@link com.flop.idea.fim.action.motion.leftright.MotionShiftRightAction}
 * |<S-Up>|               {@link com.flop.idea.fim.action.motion.updown.MotionShiftUpAction}
 * |<Down>|               {@link com.flop.idea.fim.action.motion.updown.MotionArrowDownAction}
 * |<Left>|               {@link com.flop.idea.fim.action.motion.select.motion.SelectMotionLeftAction}
 * |<Right>|              {@link com.flop.idea.fim.action.motion.select.motion.SelectMotionRightAction}
 * |<Up>|                 {@link com.flop.idea.fim.action.motion.updown.MotionArrowUpAction}
 *
 * 5. Command line editing
 *
 * tag                    action
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |c_CTRL-A|             TO BE IMPLEMENTED
 * |c_CTRL-B|             {@link javax.swing.text.DefaultEditorKit#beginLineAction}
 * |c_CTRL-C|             {@link com.flop.idea.fim.ui.ex.CancelEntryAction}
 * |c_CTRL-D|             TO BE IMPLEMENTED
 * |c_CTRL-E|             {@link javax.swing.text.DefaultEditorKit#endLineAction}
 * |c_CTRL-G|             TO BE IMPLEMENTED
 * |c_CTRL-H|             {@link com.flop.idea.fim.ui.ex.DeletePreviousCharAction}
 * |c_CTRL-I|             TO BE IMPLEMENTED
 * |c_CTRL-J|             {@link com.flop.idea.fim.ui.ex.CompleteEntryAction}
 * |c_CTRL-K|             Handled by KeyHandler
 * |c_CTRL-L|             TO BE IMPLEMENTED
 * |c_CTRL-M|             {@link com.flop.idea.fim.action.ex.ProcessExEntryAction}
 * |c_CTRL-N|             {@link com.flop.idea.fim.ui.ex.HistoryDownAction}
 * |c_CTRL-P|             {@link com.flop.idea.fim.ui.ex.HistoryUpAction}
 * |c_CTRL-Q|             Handled by KeyHandler
 * |c_CTRL-R|             {@link com.flop.idea.fim.ui.ex.InsertRegisterAction}
 * |c_CTRL-R_CTRL-A|      TO BE IMPLEMENTED
 * |c_CTRL-R_CTRL-F|      TO BE IMPLEMENTED
 * |c_CTRL-R_CTRL-L|      TO BE IMPLEMENTED
 * |c_CTRL-R_CTRL-O|      TO BE IMPLEMENTED
 * |c_CTRL-R_CTRL-P|      TO BE IMPLEMENTED
 * |c_CTRL-R_CTRL-R|      TO BE IMPLEMENTED
 * |c_CTRL-R_CTRL-W|      TO BE IMPLEMENTED
 * |c_CTRL-T|             TO BE IMPLEMENTED
 * |c_CTRL-U|             {@link com.flop.idea.fim.ui.ex.DeleteToCursorAction}
 * |c_CTRL-V|             Handled by KeyHandler
 * |c_CTRL-W|             {@link com.flop.idea.fim.ui.ex.DeletePreviousWordAction}
 * |c_CTRL-Y|             TO BE IMPLEMENTED
 * |c_CTRL-\_e|           TO BE IMPLEMENTED
 * |c_CTRL-\_CTRL-G|      TO BE IMPLEMENTED
 * |c_CTRL-\_CTRL-N|      TO BE IMPLEMENTED
 * |c_CTRL-_|             not applicable
 * |c_CTRL-^|             not applicable
 * |c_CTRL-]|             TO BE IMPLEMENTED
 * |c_CTRL-[|             {@link com.flop.idea.fim.ui.ex.EscapeCharAction}
 * |c_<BS>|               {@link com.flop.idea.fim.ui.ex.DeletePreviousCharAction}
 * |c_<CR>|               {@link com.flop.idea.fim.ui.ex.CompleteEntryAction}
 * |c_<C-Left>|           {@link javax.swing.text.DefaultEditorKit#previousWordAction}
 * |c_<C-Right>|          {@link javax.swing.text.DefaultEditorKit#nextWordAction}
 * |c_<Del>|              {@link javax.swing.text.DefaultEditorKit#deleteNextCharAction}
 * |c_<Down>|             {@link com.flop.idea.fim.ui.ex.HistoryDownFilterAction}
 * |c_<End>|              {@link javax.swing.text.DefaultEditorKit#endLineAction}
 * |c_<Esc>|              {@link com.flop.idea.fim.ui.ex.EscapeCharAction}
 * |c_<Home>|             {@link javax.swing.text.DefaultEditorKit#beginLineAction}
 * |c_<Insert>|           {@link com.flop.idea.fim.ui.ex.ToggleInsertReplaceAction}
 * |c_<Left>|             {@link javax.swing.text.DefaultEditorKit#backwardAction}
 * |c_<LeftMouse>|        not applicable
 * |c_<MiddleMouse>|      TO BE IMPLEMENTED
 * |c_<NL>|               {@link com.flop.idea.fim.ui.ex.CompleteEntryAction}
 * |c_<PageUp>|           {@link com.flop.idea.fim.ui.ex.HistoryUpAction}
 * |c_<PageDown>|         {@link com.flop.idea.fim.ui.ex.HistoryDownAction}
 * |c_<Right>|            {@link javax.swing.text.DefaultEditorKit#forwardAction}
 * |c_<S-Down>|           {@link com.flop.idea.fim.ui.ex.HistoryDownAction}
 * |c_<S-Left>|           {@link javax.swing.text.DefaultEditorKit#previousWordAction}
 * |c_<S-Right>|          {@link javax.swing.text.DefaultEditorKit#nextWordAction}
 * |c_<S-Tab>|            TO BE IMPLEMENTED
 * |c_<S-Up>|             {@link com.flop.idea.fim.ui.ex.HistoryUpAction}
 * |c_<Tab>|              TO BE IMPLEMENTED
 * |c_<Up>|               {@link com.flop.idea.fim.ui.ex.HistoryUpFilterAction}
 * |c_digraph|            {char1} <BS> {char2}
 * |c_wildchar|           TO BE IMPLEMENTED
 * |'cedit'|              TO BE IMPLEMENTED
 *
 *
 * 6. Ex commands
 *
 * tag                    handler
 * -------------------------------------------------------------------------------------------------------------------
 *
 * |:map|                 {@link com.flop.idea.fim.fimscript.model.commands.mapping.MapCommand}
 * |:nmap|                ...
 * |:vmap|                ...
 * |:omap|                ...
 * |:imap|                ...
 * |:cmap|                ...
 * |:noremap|             ...
 * |:nnoremap|            ...
 * |:vnoremap|            ...
 * |:onoremap|            ...
 * |:inoremap|            ...
 * |:cnoremap|            ...
 * |:shell|               {@link com.flop.idea.fim.fimscript.model.commands.ShellCommand}
 * |:sort|                {@link com.flop.idea.fim.fimscript.model.commands.SortCommand}
 * |:source|              {@link com.flop.idea.fim.fimscript.model.commands.SourceCommand}
 * |:qall|                {@link com.flop.idea.fim.fimscript.model.commands.ExitCommand}
 * |:quitall|             {@link com.flop.idea.fim.fimscript.model.commands.ExitCommand}
 * |:quitall|             {@link com.flop.idea.fim.fimscript.model.commands.ExitCommand}
 * |:wqall|               {@link com.flop.idea.fim.fimscript.model.commands.ExitCommand}
 * |:xall|                {@link com.flop.idea.fim.fimscript.model.commands.ExitCommand}
 * |:command|             {@link com.flop.idea.fim.fimscript.model.commands.CmdCommand}
 * |:delcommand|          {@link com.flop.idea.fim.fimscript.model.commands.DelCmdCommand}
 * |:comclear|            {@link com.flop.idea.fim.fimscript.model.commands.CmdClearCommand}
 * ...
 *
 * The list of supported Ex commands is incomplete.
 *
 *
 * A. Misc commands
 *
 * tag                    handler
 * -------------------------------------------------------------------------------------------------------------------
 * |]b|                   {@link com.flop.idea.fim.action.motion.text.MotionCamelEndLeftAction}
 * |]w|                   {@link com.flop.idea.fim.action.motion.text.MotionCamelEndRightAction}
 * |[b|                   {@link com.flop.idea.fim.action.motion.text.MotionCamelLeftAction}
 * |[w|                   {@link com.flop.idea.fim.action.motion.text.MotionCamelRightAction}
 * |g(|                   {@link com.flop.idea.fim.action.motion.text.MotionSentencePreviousEndAction}
 * |g)|                   {@link com.flop.idea.fim.action.motion.text.MotionSentenceNextEndAction}
 *
 *
 * See also :help index.
 *
 * @author vlan
 */
package com.flop.idea.fim;
