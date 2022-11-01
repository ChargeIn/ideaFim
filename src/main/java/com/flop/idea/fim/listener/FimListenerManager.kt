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

package com.flop.idea.fim.listener

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.editor.impl.EditorComponentImpl
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.rd.createLifetime
import com.intellij.openapi.rd.createNestedDisposable
import com.intellij.openapi.util.Disposer
import com.intellij.util.ExceptionUtil
import com.jetbrains.rd.util.lifetime.intersect
import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.FimKeyListener
import com.flop.idea.fim.FimTypedActionHandler
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.ex.ExOutputModel
import com.flop.idea.fim.group.visual.IdeaSelectionControl
import com.flop.idea.fim.group.visual.FimVisualTimer
import com.flop.idea.fim.group.visual.moveCaretOneCharLeftFromSelectionEnd
import com.flop.idea.fim.group.visual.fimSetSystemSelectionSilently
import com.flop.idea.fim.helper.GuicursorChangeListener
import com.flop.idea.fim.helper.UpdatesChecker
import com.flop.idea.fim.helper.exitSelectMode
import com.flop.idea.fim.helper.exitVisualMode
import com.flop.idea.fim.helper.forceBarCursor
import com.flop.idea.fim.helper.inSelectMode
import com.flop.idea.fim.helper.inVisualMode
import com.flop.idea.fim.helper.isEndAllowed
import com.flop.idea.fim.helper.isIdeaFimDisabledHere
import com.flop.idea.fim.helper.localEditors
import com.flop.idea.fim.helper.moveToInlayAwareOffset
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.helper.updateCaretsVisualAttributes
import com.flop.idea.fim.helper.fimDisabled
import com.flop.idea.fim.helper.fimLastColumn
import com.flop.idea.fim.listener.MouseEventsDataHolder.skipEvents
import com.flop.idea.fim.listener.MouseEventsDataHolder.skipNDragEvents
import com.flop.idea.fim.listener.FimListenerManager.EditorListeners.add
import com.flop.idea.fim.newapi.fim
import com.flop.idea.fim.options.OptionConstants
import com.flop.idea.fim.options.OptionScope
import com.flop.idea.fim.options.helpers.KeywordOptionChangeListener
import com.flop.idea.fim.ui.ShowCmdOptionChangeListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

/**
 * @author Alex Plate
 */

object FimListenerManager {

  private val logger = Logger.getInstance(FimListenerManager::class.java)

  fun turnOn() {
    GlobalListeners.enable()
    EditorListeners.addAll()
  }

  fun turnOff() {
    GlobalListeners.disable()
    EditorListeners.removeAll()
  }

  object GlobalListeners {
    fun enable() {
      val typedAction = TypedAction.getInstance()
      if (typedAction.rawHandler !is FimTypedActionHandler) {
        // Actually this if should always be true, but just as protection
        com.flop.idea.fim.EventFacade.getInstance().setupTypedActionHandler(FimTypedActionHandler(typedAction.rawHandler))
      } else {
        if (com.flop.idea.fim.FimPlugin.getOptionService().isSet(OptionScope.GLOBAL, OptionConstants.ideastrictmodeName)) {
          error("typeAction expected to be non-fim.")
        }
      }

      com.flop.idea.fim.FimPlugin.getOptionService().addListener(OptionConstants.numberName, com.flop.idea.fim.group.EditorGroup.NumberChangeListener.INSTANCE)
      com.flop.idea.fim.FimPlugin.getOptionService().addListener(OptionConstants.relativenumberName, com.flop.idea.fim.group.EditorGroup.NumberChangeListener.INSTANCE)
      com.flop.idea.fim.FimPlugin.getOptionService().addListener(OptionConstants.scrolloffName, com.flop.idea.fim.group.MotionGroup.ScrollOptionsChangeListener.INSTANCE)
      com.flop.idea.fim.FimPlugin.getOptionService().addListener(OptionConstants.showcmdName, ShowCmdOptionChangeListener)
      com.flop.idea.fim.FimPlugin.getOptionService().addListener(OptionConstants.guicursorName, GuicursorChangeListener)
      com.flop.idea.fim.FimPlugin.getOptionService().addListener(OptionConstants.iskeywordName, KeywordOptionChangeListener, true)

      com.flop.idea.fim.EventFacade.getInstance().addEditorFactoryListener(FimEditorFactoryListener, com.flop.idea.fim.FimPlugin.getInstance().onOffDisposable)

      EditorFactory.getInstance().eventMulticaster.addCaretListener(FimCaretListener, com.flop.idea.fim.FimPlugin.getInstance().onOffDisposable)
    }

    fun disable() {
      com.flop.idea.fim.EventFacade.getInstance().restoreTypedActionHandler()

      com.flop.idea.fim.FimPlugin.getOptionService().removeListener(OptionConstants.numberName, com.flop.idea.fim.group.EditorGroup.NumberChangeListener.INSTANCE)
      com.flop.idea.fim.FimPlugin.getOptionService().removeListener(OptionConstants.relativenumberName, com.flop.idea.fim.group.EditorGroup.NumberChangeListener.INSTANCE)
      com.flop.idea.fim.FimPlugin.getOptionService().removeListener(OptionConstants.scrolloffName, com.flop.idea.fim.group.MotionGroup.ScrollOptionsChangeListener.INSTANCE)
      com.flop.idea.fim.FimPlugin.getOptionService().removeListener(OptionConstants.showcmdName, ShowCmdOptionChangeListener)
      com.flop.idea.fim.FimPlugin.getOptionService().removeListener(OptionConstants.guicursorName, GuicursorChangeListener)
      com.flop.idea.fim.FimPlugin.getOptionService().removeListener(OptionConstants.iskeywordName, KeywordOptionChangeListener)
    }
  }

  object EditorListeners {
    fun addAll() {
      localEditors().forEach { editor ->
        this.add(editor)
      }
    }

    fun removeAll() {
      localEditors().forEach { editor ->
        this.remove(editor, false)
      }
    }

    fun add(editor: Editor) {
      val pluginLifetime = com.flop.idea.fim.FimPlugin.getInstance().createLifetime()
      val editorLifetime = (editor as EditorImpl).disposable.createLifetime()
      val disposable = editorLifetime.intersect(pluginLifetime).createNestedDisposable("MyLifetimedDisposable")

      editor.contentComponent.addKeyListener(FimKeyListener)
      Disposer.register(disposable) { editor.contentComponent.removeKeyListener(FimKeyListener) }

      val eventFacade = com.flop.idea.fim.EventFacade.getInstance()
      eventFacade.addEditorMouseListener(editor, EditorMouseHandler, disposable)
      eventFacade.addEditorMouseMotionListener(editor, EditorMouseHandler, disposable)
      eventFacade.addEditorSelectionListener(editor, EditorSelectionHandler, disposable)
      eventFacade.addComponentMouseListener(editor.contentComponent, ComponentMouseListener, disposable)

      com.flop.idea.fim.FimPlugin.getEditor().editorCreated(editor)

      com.flop.idea.fim.FimPlugin.getChange().editorCreated(editor, disposable)

      Disposer.register(disposable) {
        com.flop.idea.fim.FimPlugin.getEditorIfCreated()?.editorDeinit(editor, true)
      }
    }

    fun remove(editor: Editor, isReleased: Boolean) {

      editor.contentComponent.removeKeyListener(FimKeyListener)
      val eventFacade = com.flop.idea.fim.EventFacade.getInstance()
      eventFacade.removeEditorMouseListener(editor, EditorMouseHandler)
      eventFacade.removeEditorMouseMotionListener(editor, EditorMouseHandler)
      eventFacade.removeEditorSelectionListener(editor, EditorSelectionHandler)
      eventFacade.removeComponentMouseListener(editor.contentComponent, ComponentMouseListener)

      com.flop.idea.fim.FimPlugin.getEditorIfCreated()?.editorDeinit(editor, isReleased)

      com.flop.idea.fim.FimPlugin.getChange().editorReleased(editor)
    }
  }

  object FimCaretListener : CaretListener {
    override fun caretAdded(event: CaretEvent) {
      if (fimDisabled(event.editor)) return
      event.editor.updateCaretsVisualAttributes()
    }

    override fun caretRemoved(event: CaretEvent) {
      if (fimDisabled(event.editor)) return
      event.editor.updateCaretsVisualAttributes()
    }
  }

  class FimFileEditorManagerListener : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
      if (!com.flop.idea.fim.FimPlugin.isEnabled()) return
      com.flop.idea.fim.group.MotionGroup.fileEditorManagerSelectionChangedCallback(event)
      com.flop.idea.fim.group.FileGroup.fileEditorManagerSelectionChangedCallback(event)
      com.flop.idea.fim.group.SearchGroup.fileEditorManagerSelectionChangedCallback(event)
    }
  }

  private object FimEditorFactoryListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
      add(event.editor)
      UpdatesChecker.check()
    }

    override fun editorReleased(event: EditorFactoryEvent) {
      com.flop.idea.fim.FimPlugin.getMark().editorReleased(event)
    }
  }

  private object EditorSelectionHandler : SelectionListener {
    private var myMakingChanges = false

    /**
     * This event is executed for each caret using [com.intellij.openapi.editor.CaretModel.runForEachCaret]
     */
    override fun selectionChanged(selectionEvent: SelectionEvent) {
      if (selectionEvent.editor.isIdeaFimDisabledHere) return
      val editor = selectionEvent.editor
      val document = editor.document

      logger.trace { "Selection changed" }
      logger.trace { ExceptionUtil.currentStackTrace() }

      //region Not selected last character protection
      // Here is currently a bug in IJ for IdeaFim. If you start selection right from the line end, then
      //  move to the left, the last character remains unselected.
      //  It's not clear why this happens, but this code fixes it.
      val caret = editor.caretModel.currentCaret
      val lineEnd = com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(editor, caret.offset)
      val lineStart = com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(editor, caret.offset)
      if (skipNDragEvents < skipEvents &&
        lineEnd != lineStart &&
        selectionEvent.newRange.startOffset == selectionEvent.newRange.endOffset &&
        selectionEvent.newRange.startOffset == lineEnd - 1 &&
        selectionEvent.newRange.startOffset == caret.offset
      ) {
        caret.setSelection(lineEnd, lineEnd - 1)
      }
      //endregion

      if (SelectionFimListenerSuppressor.isNotLocked) {
        logger.debug("Adjust non fim selection change")
        IdeaSelectionControl.controlNonFimSelectionChange(editor)
      }

      if (myMakingChanges || document is DocumentEx && document.isInEventsHandling) {
        return
      }

      myMakingChanges = true
      try {
        // Synchronize selections between editors
        val newRange = selectionEvent.newRange
        for (e in localEditors(document)) {
          if (e != editor) {
            e.selectionModel.fimSetSystemSelectionSilently(newRange.startOffset, newRange.endOffset)
          }
        }
      } finally {
        myMakingChanges = false
      }
    }
  }

  private object EditorMouseHandler : EditorMouseListener, EditorMouseMotionListener {
    private var mouseDragging = false
    private var cutOffFixed = false

    override fun mouseDragged(e: EditorMouseEvent) {
      if (e.editor.isIdeaFimDisabledHere) return

      val caret = e.editor.caretModel.primaryCaret

      clearFirstSelectionEvents(e)

      if (mouseDragging && caret.hasSelection()) {
        /**
         * We force the bar caret while dragging because it matches IntelliJ's selection model better.
         * * Fim's drag selection is based on character bounding boxes. When 'selection' is set to "inclusive" (the
         *   default), Fim selects a character when the mouse cursor drags the text caret into its bounding box (LTR).
         *   The character at the text caret is selected and the block caret is drawn to cover the character (the bar
         *   caret would be between the selection and the last character of the selection, which is weird). See "v" in
         *   'guicursor'. When 'selection' is "exclusive", Fim will select a character when the mouse cursor drags the
         *   text caret out of its bounding box. The character at the text caret is not selected and the bar caret is
         *   drawn at the start of this character to make it more obvious that it is unselected. See "ve" in
         *   'guicursor'.
         * * IntelliJ's selection is based on character mid-points. E.g. the caret is moved to the start of offset 2
         *   when the second half of offset 1 is clicked, and a character is selected when the mouse is moved from the
         *   first half to the second half. This means:
         *   1) While dragging, the selection is always exclusive - the character at the text caret is not selected. We
         *   convert to an inclusive selection when the mouse is released, by moving back one character. It makes
         *   sense to match Fim's bar caret here.
         *   2) An exclusive selection should trail behind the mouse cursor, but IntelliJ doesn't, because the selection
         *   boundaries are mid-points - the text caret can be in front of/to the right of the mouse cursor (LTR).
         *   Using a block caret would push the block further out passed the selection and the mouse cursor, and
         *   feels wrong. The bar caret is a better user experience.
         *   RTL probably introduces other fun issues
         * We can implement inclusive/exclusive 'selection' with normal text movement, but unless we can change the way
         * selection works while dragging, I don't think we can match Fim's selection behaviour exactly.
         */
        caret.forceBarCursor()

        if (!cutOffFixed && ComponentMouseListener.cutOffEnd) {
          cutOffFixed = true
          SelectionFimListenerSuppressor.lock().use {
            if (caret.selectionEnd == e.editor.document.getLineEndOffset(caret.logicalPosition.line) - 1 &&
              caret.leadSelectionOffset == caret.selectionEnd
            ) {
              // A small but important customization. Because IdeaFim doesn't allow to put the caret on the line end,
              //   the selection can omit the last character if the selection was started in the middle on the
              //   last character in line and has a negative direction.
              caret.setSelection(caret.selectionStart, caret.selectionEnd + 1)
            }
            // This is the same correction, but for the newer versions of the IDE: 213+
            if (caret.selectionEnd == e.editor.document.getLineEndOffset(caret.logicalPosition.line) &&
              caret.selectionEnd == caret.selectionStart + 1
            ) {
              caret.setSelection(caret.selectionEnd, caret.selectionEnd)
            }
          }
        }
      }
      skipNDragEvents -= 1
    }

    /**
     * When user places the caret, sometimes they perform a small drag. This doesn't affect clear IJ, but with IdeaFim
     * it may introduce unwanted selection. Here we remove any selection if "dragging" happens for less than 3 events.
     * This is because the first click moves the caret passed the end of the line, is then received in
     * [ComponentMouseListener] and the caret is moved back to the start of the last character of the line. If there is
     * a drag, this translates to a selection of the last character. In this case, remove the selection.
     * We force the bar caret simply because it looks better - the block caret is dragged to the end, becomes a less
     * intrusive bar caret and snaps back to the last character (and block caret) when the mouse is released.
     * TODO: Fim supports selection of the character after the end of line
     * (Both with mouse and with v$. IdeaFim treats v$ as an exclusive selection)
     */
    private fun clearFirstSelectionEvents(e: EditorMouseEvent) {
      if (skipNDragEvents > 0) {
        logger.debug("Mouse dragging")
        FimVisualTimer.swingTimer?.stop()
        if (!mouseDragging) {
          SelectionFimListenerSuppressor.lock()
        }
        mouseDragging = true

        val caret = e.editor.caretModel.primaryCaret
        if (onLineEnd(caret)) {
          SelectionFimListenerSuppressor.lock().use {
            caret.removeSelection()
            caret.forceBarCursor()
          }
        }
      }
    }

    private fun onLineEnd(caret: Caret): Boolean {
      val editor = caret.editor
      val lineEnd = com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(editor, caret.offset)
      val lineStart = com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(editor, caret.offset)
      return caret.offset == lineEnd && lineEnd != lineStart && caret.offset - 1 == caret.selectionStart && caret.offset == caret.selectionEnd
    }

    override fun mousePressed(event: EditorMouseEvent) {
      if (event.editor.isIdeaFimDisabledHere) return

      skipNDragEvents = skipEvents
      SelectionFimListenerSuppressor.reset()
    }

    /**
     * This method may not be called
     * Known cases:
     * - Click-hold and close editor (ctrl-w)
     * - Click-hold and switch editor (ctrl-tab)
     */
    override fun mouseReleased(event: EditorMouseEvent) {
      if (event.editor.isIdeaFimDisabledHere) return

      SelectionFimListenerSuppressor.unlock()

      clearFirstSelectionEvents(event)
      skipNDragEvents = skipEvents
      if (mouseDragging) {
        logger.debug("Release mouse after dragging")
        val editor = event.editor
        val caret = editor.caretModel.primaryCaret
        SelectionFimListenerSuppressor.lock().use {
          val predictedMode = IdeaSelectionControl.predictMode(editor, SelectionSource.MOUSE)
          IdeaSelectionControl.controlNonFimSelectionChange(editor, SelectionSource.MOUSE)
          // TODO: This should only be for 'selection'=inclusive
          moveCaretOneCharLeftFromSelectionEnd(editor, predictedMode)

          // Reset caret after forceBarShape while dragging
          editor.updateCaretsVisualAttributes()
          caret.fimLastColumn = editor.caretModel.visualPosition.column
        }

        mouseDragging = false
        cutOffFixed = false
      }
    }

    override fun mouseClicked(event: EditorMouseEvent) {
      if (event.editor.isIdeaFimDisabledHere) return
      logger.debug("Mouse clicked")

      if (event.area == EditorMouseEventArea.EDITING_AREA) {
        com.flop.idea.fim.FimPlugin.getMotion()
        val editor = event.editor
        if (com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance().isActive) {
          com.flop.idea.fim.FimPlugin.getProcess().cancelExEntry(editor.fim, false)
        }

        ExOutputModel.getInstance(editor).clear()

        val caretModel = editor.caretModel
        if (editor.subMode != FimStateMachine.SubMode.NONE) {
          caretModel.removeSecondaryCarets()
        }

        // Removing selection on just clicking.
        //
        // Actually, this event should not be fired on right click (when the menu appears), but since 202 it happens
        //   sometimes. To prevent unwanted selection removing, an assertion for isRightClick was added.
        // See:
        //   https://youtrack.jetbrains.com/issue/IDEA-277716
        //   https://youtrack.jetbrains.com/issue/VIM-2368
        if (event.mouseEvent.clickCount == 1 && !SwingUtilities.isRightMouseButton(event.mouseEvent)) {
          if (editor.inVisualMode) {
            editor.exitVisualMode()
          } else if (editor.inSelectMode) {
            editor.exitSelectMode(false)
            KeyHandler.getInstance().reset(editor.fim)
          }
        }

        // TODO: 2019-03-22 Multi?
        caretModel.primaryCaret.fimLastColumn = caretModel.visualPosition.column
      } else if (event.area != EditorMouseEventArea.ANNOTATIONS_AREA &&
        event.area != EditorMouseEventArea.FOLDING_OUTLINE_AREA &&
        event.mouseEvent.button != MouseEvent.BUTTON3
      ) {
        com.flop.idea.fim.FimPlugin.getMotion()
        if (com.flop.idea.fim.ui.ex.ExEntryPanel.getInstance().isActive) {
          com.flop.idea.fim.FimPlugin.getProcess().cancelExEntry(event.editor.fim, false)
        }

        ExOutputModel.getInstance(event.editor).clear()
      }
    }
  }

  private object ComponentMouseListener : MouseAdapter() {

    var cutOffEnd = false

    override fun mousePressed(e: MouseEvent?) {
      val editor = (e?.component as? EditorComponentImpl)?.editor ?: return
      if (editor.isIdeaFimDisabledHere) return
      val predictedMode = IdeaSelectionControl.predictMode(editor, SelectionSource.MOUSE)
      when (e.clickCount) {
        1 -> {
          // If you click after the line, the caret is placed by IJ after the last symbol.
          // This is not allowed in some fim modes, so we move the caret over the last symbol.
          if (!predictedMode.isEndAllowed) {
            @Suppress("ideafimRunForEachCaret")
            editor.caretModel.runForEachCaret { caret ->
              val lineEnd = com.flop.idea.fim.helper.EditorHelper.getLineEndForOffset(editor, caret.offset)
              val lineStart = com.flop.idea.fim.helper.EditorHelper.getLineStartForOffset(editor, caret.offset)
              cutOffEnd = if (caret.offset == lineEnd && lineEnd != lineStart) {
                caret.moveToInlayAwareOffset(caret.offset - 1)
                true
              } else {
                false
              }
            }
          } else cutOffEnd = false
        }
        // Double-clicking a word in IntelliJ will select the word and locate the caret at the end of the selection,
        // on the following character. When using a bar caret, this is drawn as between the end of selection and the
        // following char. With a block caret, this draws the caret "over" the following character.
        // In Fim, when 'selection' is "inclusive" (default), double clicking a word will select the last character of
        // the word and leave the caret on the last character, drawn as a block caret. We move one character left to
        // match this behaviour.
        // When 'selection' is exclusive, the caret is placed *after* the end of the word, and is drawn using the 've'
        // option of 'guicursor' - as a bar, so it appears to be in between the end of the word and the start of the
        // following character.
        // TODO: Modify this to support 'selection' set to "exclusive"
        2 -> moveCaretOneCharLeftFromSelectionEnd(editor, predictedMode)
      }
    }
  }

  enum class SelectionSource {
    MOUSE,
    OTHER
  }
}

private object MouseEventsDataHolder {
  const val skipEvents = 3
  var skipNDragEvents = skipEvents
}
