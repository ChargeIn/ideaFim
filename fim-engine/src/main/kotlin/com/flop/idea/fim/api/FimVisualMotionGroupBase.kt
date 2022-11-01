package com.flop.idea.fim.api

import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.group.visual.VisualChange
import com.flop.idea.fim.group.visual.VisualOperation
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.pushSelectMode
import com.flop.idea.fim.helper.pushVisualMode
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.fimscript.model.datatypes.FimString

abstract class FimVisualMotionGroupBase : FimVisualMotionGroup {
  override val exclusiveSelection: Boolean
    get() = (
      injector.optionService.getOptionValue(
        OptionScope.GLOBAL,
        OptionConstants.selectionName
      ) as FimString
      ).value == "exclusive"
  override val selectionAdj: Int
    get() = if (exclusiveSelection) 0 else 1

  override fun enterSelectMode(editor: FimEditor, subMode: FimStateMachine.SubMode): Boolean {
    editor.fimStateMachine.pushSelectMode(subMode)
    editor.forEachCaret { it.fimSelectionStart = it.fimLeadSelectionOffset }
    return true
  }

  /**
   * This function toggles visual mode.
   *
   * If visual mode is disabled, enable it
   * If visual mode is enabled, but [subMode] differs, update visual according to new [subMode]
   * If visual mode is enabled with the same [subMode], disable it
   */
  override fun toggleVisual(editor: FimEditor, count: Int, rawCount: Int, subMode: FimStateMachine.SubMode): Boolean {
    if (!editor.inVisualMode) {
      // Enable visual subMode
      if (rawCount > 0) {
        val primarySubMode = editor.primaryCaret().fimLastVisualOperatorRange?.type?.toSubMode() ?: subMode
        editor.fimStateMachine.pushVisualMode(primarySubMode)

        editor.forEachCaret {
          val range = it.fimLastVisualOperatorRange ?: VisualChange.default(subMode)
          val end = VisualOperation.calculateRange(editor, range, count, it)
          val lastColumn =
            if (range.columns == FimMotionGroupBase.LAST_COLUMN) FimMotionGroupBase.LAST_COLUMN else editor.offsetToLogicalPosition(
              end
            ).column
          it.fimLastColumn = lastColumn
          it.fimSetSelection(it.offset.point, end, true)
        }
      } else {
        editor.fimStateMachine.pushVisualMode(subMode)
        editor.forEachCaret { it.fimSetSelection(it.offset.point) }
      }
      return true
    }

    if (subMode == editor.subMode) {
      // Disable visual subMode
      editor.exitVisualModeNative()
      return true
    }

    // Update visual subMode with new sub subMode
    editor.subMode = subMode
    for (caret in editor.carets()) {
      caret.updateEditorSelection()
    }

    return true
  }

  protected fun seemsLikeBlockMode(editor: FimEditor): Boolean {
    val selections = editor.nativeCarets().map {
      val adj = if (editor.offsetToLogicalPosition(it.selectionEnd).column == 0) 1 else 0
      it.selectionStart to (it.selectionEnd - adj).coerceAtLeast(0)
    }.sortedBy { it.first }
    val selectionStartColumn = editor.offsetToLogicalPosition(selections.first().first).column
    val selectionStartLine = editor.offsetToLogicalPosition(selections.first().first).line

    val maxColumn = selections.maxOfOrNull { editor.offsetToLogicalPosition(it.second).column } ?: return false
    selections.forEachIndexed { i, it ->
      if (editor.offsetToLogicalPosition(it.first).line != editor.offsetToLogicalPosition(it.second).line) {
        return false
      }
      if (editor.offsetToLogicalPosition(it.first).column != selectionStartColumn) {
        return false
      }
      val lineEnd =
        editor.offsetToLogicalPosition(injector.engineEditorHelper.getLineEndForOffset(editor, it.second)).column
      if (editor.offsetToLogicalPosition(it.second).column != maxColumn.coerceAtMost(lineEnd)) {
        return false
      }
      if (editor.offsetToLogicalPosition(it.first).line != selectionStartLine + i) {
        return false
      }
    }
    return true
  }

  override fun autodetectVisualSubmode(editor: FimEditor): FimStateMachine.SubMode {
    if (editor.carets().size > 1 && seemsLikeBlockMode(editor)) {
      return FimStateMachine.SubMode.VISUAL_BLOCK
    }
    val all = editor.nativeCarets().all { caret ->
      // Detect if visual mode is character wise or line wise
      val selectionStart = caret.selectionStart
      val selectionEnd = caret.selectionEnd
      val logicalStartLine = editor.offsetToLogicalPosition(selectionStart).line
      val logicalEnd = editor.offsetToLogicalPosition(selectionEnd)
      val logicalEndLine = if (logicalEnd.column == 0) (logicalEnd.line - 1).coerceAtLeast(0) else logicalEnd.line
      val lineStartOfSelectionStart = injector.engineEditorHelper.getLineStartOffset(editor, logicalStartLine)
      val lineEndOfSelectionEnd = injector.engineEditorHelper.getLineEndOffset(editor, logicalEndLine, true)
      lineStartOfSelectionStart == selectionStart && (lineEndOfSelectionEnd + 1 == selectionEnd || lineEndOfSelectionEnd == selectionEnd)
    }
    if (all) return FimStateMachine.SubMode.VISUAL_LINE
    return FimStateMachine.SubMode.VISUAL_CHARACTER
  }

  /**
   * Enters visual mode based on current editor state.
   * If [subMode] is null, subMode will be detected automatically
   *
   * it:
   * - Updates command state
   * - Updates [fimSelectionStart] property
   * - Updates caret colors
   * - Updates care shape
   *
   * - DOES NOT change selection
   * - DOES NOT move caret
   * - DOES NOT check if carets actually have any selection
   */
  override fun enterVisualMode(editor: FimEditor, subMode: FimStateMachine.SubMode?): Boolean {
    val autodetectedSubMode = subMode ?: autodetectVisualSubmode(editor)
    editor.fimStateMachine.pushModes(FimStateMachine.Mode.VISUAL, autodetectedSubMode)
    if (autodetectedSubMode == FimStateMachine.SubMode.VISUAL_BLOCK) {
      editor.primaryCaret().run { fimSelectionStart = fimLeadSelectionOffset }
    } else {
      editor.nativeCarets().forEach { it.fimSelectionStart = it.fimLeadSelectionOffset }
    }
    return true
  }
}
