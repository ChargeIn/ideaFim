package com.flop.idea.fim.mark

import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Command
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.diagnostic.debug
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.helper.fimStateMachine
import java.util.*

abstract class FimMarkGroupBase : FimMarkGroup {
  @JvmField
  protected val fileMarks = HashMap<String, FileMarks<Char, Mark>>()
  @JvmField
  protected val globalMarks = HashMap<Char, Mark>()
  @JvmField
  // COMPATIBILITY-LAYER: Changed to public
  // Please see: https://jb.gg/zo8n0r
  // Use dropLastJump method instead of direct access
  /*protected*/ val jumps: MutableList<Jump> = ArrayList<Jump>()
  @JvmField
  protected var jumpSpot = -1

  protected class FileMarks<K, V> : HashMap<K, V>() {
    var myTimestamp = Date()

    fun setTimestamp(timestamp: Date) {
      this.myTimestamp = timestamp
    }

    override fun put(key: K, value: V): V? {
      myTimestamp = Date()
      return super.put(key, value)
    }
  }

  /**
   * This updates all the marks for a file whenever text is deleted from the file. If the line that contains a mark
   * is completely deleted then the mark is deleted too. If the deleted text is before the marked line, the mark is
   * moved up by the number of deleted lines.
   *
   * @param editor      The modified editor
   * @param marks       The editor's marks to update
   * @param delStartOff The offset within the editor where the deletion occurred
   * @param delLength   The length of the deleted text
   */
  override fun updateMarkFromDelete(editor: FimEditor?, marks: HashMap<Char, Mark>?, delStartOff: Int, delLength: Int) {
    // Skip all this work if there are no marks
    if (marks != null && marks.size > 0 && editor != null) {
      // Calculate the logical position of the start and end of the deleted text
      val delEndOff = delStartOff + delLength - 1
      val delStart = editor.offsetToLogicalPosition(delStartOff)
      val delEnd = editor.offsetToLogicalPosition(delEndOff + 1)
      logger.debug { "mark delete. delStart = $delStart, delEnd = $delEnd" }

      // Now analyze each mark to determine if it needs to be updated or removed
      for (ch in marks.keys) {
        val myMark = marks[ch]
        if (myMark !is FimMark) continue

        logger.debug { "mark = $myMark" }
        // If the end of the deleted text is prior to the marked line, simply shift the mark up by the
        // proper number of lines.
        if (delEnd.line < myMark.logicalLine) {
          val lines = delEnd.line - delStart.line
          logger.debug { "Shifting mark by $lines lines" }
          myMark.logicalLine = myMark.logicalLine - lines
        } else if (delStart.line <= myMark.logicalLine/* && delEnd.line >= mark.logicalLine*/) {
          // Regarding the commented out condition in if: This additional condition was here before moving to kotlin
          // But now it's highlighted as "always true", so I commented it out for case of it's a bug

          val markLineStartOff = injector.engineEditorHelper.getLineStartOffset(editor, myMark.logicalLine)
          val markLineEndOff = injector.engineEditorHelper.getLineEndOffset(editor, myMark.logicalLine, true)

          val command = editor.fimStateMachine.executingCommand
          // If text is being changed from the start of the mark line (a special case for mark deletion)
          val changeFromMarkLineStart = (command != null && command.type === Command.Type.CHANGE && delStartOff == markLineStartOff)
          // If the marked line is completely within the deleted text, remove the mark (except the special case)
          if (delStartOff <= markLineStartOff && delEndOff >= markLineEndOff && !changeFromMarkLineStart) {
            injector.markGroup.removeMark(ch, myMark)
            logger.debug("Removed mark")
          } else if (delStart.line < myMark.logicalLine) {
            // shift mark
            myMark.logicalLine = delStart.line
            logger.debug { "Shifting mark to line " + delStart.line }
          } // The deletion only covers part of the marked line so shift the mark only if the deletion begins
          // on a line prior to the marked line (which means the deletion must end on the marked line).
        } // If the deleted text begins before the mark and ends after the mark then it may be shifted or deleted
      }
    }
  }

  /**
   * This updates all the marks for a file whenever text is inserted into the file. If the line that contains a mark
   * that is after the start of the insertion point, shift the mark by the number of new lines added.
   *
   * @param editor      The editor that was updated
   * @param marks       The editor's marks
   * @param insStartOff The insertion point
   * @param insLength   The length of the insertion
   */
  override fun updateMarkFromInsert(editor: FimEditor?, marks: HashMap<Char, Mark>?, insStartOff: Int, insLength: Int) {
    if (marks != null && marks.size > 0 && editor != null) {
      val insEndOff = insStartOff + insLength
      val insStart = editor.offsetToLogicalPosition(insStartOff)
      val insEnd = editor.offsetToLogicalPosition(insEndOff)
      logger.debug { "mark insert. insStart = $insStart, insEnd = $insEnd" }
      val lines = insEnd.line - insStart.line
      if (lines == 0) return

      for (mark in marks.values.filterIsInstance<FimMark>()) {
        logger.debug { "mark = $mark" }
        // Shift the mark if the insertion began on a line prior to the marked line.
        if (insStart.line < mark.logicalLine) {
          mark.logicalLine = mark.logicalLine + lines
          logger.debug { "Shifting mark by $lines lines" }
        }
      }
    }
  }

  companion object {
    private val logger = fimLogger<FimMarkGroupBase>()
    const val SAVE_JUMP_COUNT = 100
  }

  override fun addJump(editor: FimEditor, reset: Boolean) {
    addJump(editor, editor.currentCaret().offset.point, reset)
  }

  private fun addJump(editor: FimEditor, offset: Int, reset: Boolean) {
    val path = editor.getPath() ?: return

    val lp = editor.offsetToLogicalPosition(offset)
    val jump = Jump(lp.line, lp.column, path)
    val filename = jump.filepath

    for (i in jumps.indices) {
      val j = jumps[i]
      if (filename == j.filepath && j.logicalLine == jump.logicalLine) {
        jumps.removeAt(i)
        break
      }
    }

    jumps.add(jump)

    if (reset) {
      jumpSpot = -1
    } else {
      jumpSpot++
    }

    if (jumps.size > SAVE_JUMP_COUNT) {
      jumps.removeAt(0)
    }
  }

  /**
   * Gets the map of marks for the specified file
   *
   * @param filename The file to get the marks for
   * @return The map of marks. The keys are `Character`s of the mark names, the values are
   * `Mark`s.
   */
  protected fun getFileMarks(filename: String): FileMarks<Char, Mark> {
    return fileMarks.getOrPut(filename) { FileMarks() }
  }

  /**
   * Gets the requested mark for the editor
   *
   * @param editor The editor to get the mark for
   * @param ch     The desired mark
   * @return The requested mark if set, null if not set
   */
  override fun getMark(editor: FimEditor, ch: Char): Mark? {
    var myCh = ch
    var mark: Mark? = null
    if (myCh == '`') myCh = '\''

    // Make sure this is a valid mark
    if (FimMarkConstants.VALID_GET_MARKS.indexOf(myCh) < 0) return null

    val editorPath = editor.getPath()
    if ("{}".indexOf(myCh) >= 0 && editorPath != null) {
      var offset = injector.searchHelper.findNextParagraph(
        editor, editor.primaryCaret(), if (myCh == '{') -1 else 1,
        false
      )
      offset = injector.engineEditorHelper.normalizeOffset(editor, offset, false)
      val lp = editor.offsetToLogicalPosition(offset)
      mark = FimMark(myCh, lp.line, lp.column, editorPath, editor.extractProtocol())
    } else if ("()".indexOf(myCh) >= 0 && editorPath != null) {
      var offset = injector.searchHelper
        .findNextSentenceStart(
          editor, editor.primaryCaret(), if (myCh == '(') -1 else 1,
          countCurrent = false,
          requireAll = true
        )

      offset = injector.engineEditorHelper.normalizeOffset(editor, offset, false)
      val lp = editor.offsetToLogicalPosition(offset)
      mark = FimMark(myCh, lp.line, lp.column, editorPath, editor.extractProtocol())
    } else if (FimMarkConstants.FILE_MARKS.indexOf(myCh) >= 0) {
      var fmarks: FileMarks<Char, Mark>? = null
      if (editorPath != null) {
        fmarks = getFileMarks(editorPath)
      }

      if (fmarks != null) {
        mark = fmarks[myCh]
        if (mark != null && mark.isClear()) {
          fmarks.remove(myCh)
          mark = null
        }
      }
    } else if (FimMarkConstants.GLOBAL_MARKS.indexOf(myCh) >= 0) {
      mark = globalMarks[myCh]
      if (mark != null && mark.isClear()) {
        globalMarks.remove(myCh)
        mark = null
      }
    } // This is a mark from another file
    // If this is a file mark, get the mark from this file

    return mark
  }

  /**
   * Get the requested jump.
   *
   * @param count Postive for next jump (Ctrl-I), negative for previous jump (Ctrl-O).
   * @return The jump or null if out of range.
   */
  override fun getJump(count: Int): Jump? {
    val index = jumps.size - 1 - (jumpSpot - count)
    return if (index < 0 || index >= jumps.size) {
      null
    } else {
      jumpSpot -= count
      jumps[index]
    }
  }

  /**
   * Sets the specified mark to the specified location.
   *
   * @param editor  The editor the mark is associated with
   * @param ch      The mark to set
   * @param offset  The offset to set the mark to
   * @return true if able to set the mark, false if not
   */
  override fun setMark(editor: FimEditor, ch: Char, offset: Int): Boolean {
    var myCh = ch
    if (myCh == '`') myCh = '\''
    val lp = editor.offsetToLogicalPosition(offset)

    val path = editor.getPath() ?: return false

    // File specific marks get added to the file
    if (FimMarkConstants.FILE_MARKS.indexOf(myCh) >= 0) {
      val fmarks = getFileMarks(path)

      val mark = FimMark(myCh, lp.line, lp.column, path, editor.extractProtocol())
      fmarks[myCh] = mark
    } else if (FimMarkConstants.GLOBAL_MARKS.indexOf(myCh) >= 0) {
      val fmarks = getFileMarks(path)

      var mark = createSystemMark(myCh, lp.line, lp.column, editor)
      if (mark == null) {
        mark = FimMark(myCh, lp.line, lp.column, path, editor.extractProtocol())
      }
      fmarks[myCh] = mark
      val oldMark = globalMarks.put(myCh, mark)
      if (oldMark is FimMark) {
        oldMark.clear()
      }
    } // Global marks get set to both the file and the global list of marks

    return true
  }

  /**
   * Sets the specified mark to the caret position of the editor
   *
   * @param editor  The editor to get the current position from
   * @param ch      The mark set set
   * @return True if a valid, writable mark, false if not
   */
  override fun setMark(editor: FimEditor, ch: Char): Boolean {
    return FimMarkConstants.VALID_SET_MARKS.indexOf(ch) >= 0 && setMark(
      editor,
      ch,
      editor.currentCaret().offset.point
    )
  }

  /**
   * Saves the caret location prior to doing a jump
   *
   * @param editor  The editor the jump will occur in
   */
  override fun saveJumpLocation(editor: FimEditor) {
    addJump(editor, true)
    setMark(editor, '\'')

    includeCurrentCommandAsNavigation(editor)
  }

  /**
   * Get's a mark from the file
   *
   * @param editor The editor to get the mark from
   * @param ch     The mark to get
   * @return The mark in the current file, if set, null if no such mark
   */
  override fun getFileMark(editor: FimEditor, ch: Char): Mark? {
    var myCh = ch
    if (myCh == '`') myCh = '\''
    val path = editor.getPath() ?: return null
    val fmarks = getFileMarks(path)
    var mark: Mark? = fmarks[myCh]
    if (mark != null && mark.isClear()) {
      fmarks.remove(myCh)
      mark = null
    }

    return mark
  }

  override fun setVisualSelectionMarks(editor: FimEditor, range: TextRange) {
    setMark(editor, FimMarkConstants.MARK_VISUAL_START, range.startOffset)
    setMark(editor, FimMarkConstants.MARK_VISUAL_END, range.endOffset)
  }

  override fun setChangeMarks(fimEditor: FimEditor, range: TextRange) {
    setMark(fimEditor, FimMarkConstants.MARK_CHANGE_START, range.startOffset)
    setMark(fimEditor, FimMarkConstants.MARK_CHANGE_END, range.endOffset - 1)
  }

  private fun getMarksRange(editor: FimEditor, startMark: Char, endMark: Char): TextRange? {
    val start = getMark(editor, startMark)
    val end = getMark(editor, endMark)
    if (start != null && end != null) {
      val startOffset = injector.engineEditorHelper.getOffset(editor, start.logicalLine, start.col)
      val endOffset = injector.engineEditorHelper.getOffset(editor, end.logicalLine, end.col)
      return TextRange(startOffset, endOffset + 1)
    }
    return null
  }

  override fun getChangeMarks(editor: FimEditor): TextRange? {
    return getMarksRange(editor, FimMarkConstants.MARK_CHANGE_START, FimMarkConstants.MARK_CHANGE_END)
  }

  override fun getVisualSelectionMarks(editor: FimEditor): TextRange? {
    return getMarksRange(editor, FimMarkConstants.MARK_VISUAL_START, FimMarkConstants.MARK_VISUAL_END)
  }

  override fun resetAllMarks() {
    globalMarks.clear()
    fileMarks.clear()
    jumps.clear()
  }

  override fun removeMark(ch: Char, mark: Mark) {
    if (FimMarkConstants.FILE_MARKS.indexOf(ch) >= 0) {
      val fmarks = getFileMarks(mark.filename)
      fmarks.remove(ch)
    } else if (FimMarkConstants.GLOBAL_MARKS.indexOf(ch) >= 0) {
      // Global marks are added to global and file marks
      val fmarks = getFileMarks(mark.filename)
      fmarks.remove(ch)
      globalMarks.remove(ch)
    }

    mark.clear()
  }

  override fun getMarks(editor: FimEditor): List<Mark> {
    val res = HashSet<Mark>()

    val path = editor.getPath()
    if (path != null) {
      val marks = getFileMarks(path)
      res.addAll(marks.values)
    }
    res.addAll(globalMarks.values)

    val list = ArrayList(res)

    list.sortWith(Mark.KeySorter)

    return list
  }

  override fun getJumps(): List<Jump> {
    return jumps
  }

  override fun getJumpSpot(): Int {
    return jumpSpot
  }

  override fun dropLastJump() {
    jumps.dropLast(1)
  }
}
