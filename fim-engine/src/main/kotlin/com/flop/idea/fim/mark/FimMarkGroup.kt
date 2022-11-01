package com.flop.idea.fim.mark

import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.common.TextRange

interface FimMarkGroup {
  fun saveJumpLocation(editor: FimEditor)
  fun setChangeMarks(fimEditor: FimEditor, range: TextRange)
  fun addJump(editor: FimEditor, reset: Boolean)

  /**
   * Gets the requested mark for the editor
   *
   * @param editor The editor to get the mark for
   * @param ch     The desired mark
   * @return The requested mark if set, null if not set
   */
  fun getMark(editor: FimEditor, ch: Char): Mark?

  /**
   * Get the requested jump.
   *
   * @param count Postive for next jump (Ctrl-I), negative for previous jump (Ctrl-O).
   * @return The jump or null if out of range.
   */
  fun getJump(count: Int): Jump?
  fun createSystemMark(ch: Char, line: Int, col: Int, editor: FimEditor): Mark?

  /**
   * Sets the specified mark to the specified location.
   *
   * @param editor  The editor the mark is associated with
   * @param ch      The mark to set
   * @param offset  The offset to set the mark to
   * @return true if able to set the mark, false if not
   */
  fun setMark(editor: FimEditor, ch: Char, offset: Int): Boolean

  /**
   * Sets the specified mark to the caret position of the editor
   *
   * @param editor  The editor to get the current position from
   * @param ch      The mark set set
   * @return True if a valid, writable mark, false if not
   */
  fun setMark(editor: FimEditor, ch: Char): Boolean
  fun includeCurrentCommandAsNavigation(editor: FimEditor)

  /**
   * Get's a mark from the file
   *
   * @param editor The editor to get the mark from
   * @param ch     The mark to get
   * @return The mark in the current file, if set, null if no such mark
   */
  fun getFileMark(editor: FimEditor, ch: Char): Mark?
  fun setVisualSelectionMarks(editor: FimEditor, range: TextRange)
  fun getChangeMarks(editor: FimEditor): TextRange?
  fun getVisualSelectionMarks(editor: FimEditor): TextRange?
  fun resetAllMarks()
  fun removeMark(ch: Char, mark: Mark)
  fun getMarks(editor: FimEditor): List<Mark>
  fun getJumps(): List<Jump>
  fun getJumpSpot(): Int

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
  fun updateMarkFromDelete(editor: FimEditor?, marks: HashMap<Char, Mark>?, delStartOff: Int, delLength: Int)

  /**
   * This updates all the marks for a file whenever text is inserted into the file. If the line that contains a mark
   * that is after the start of the insertion point, shift the mark by the number of new lines added.
   *
   * @param editor      The editor that was updated
   * @param marks       The editor's marks
   * @param insStartOff The insertion point
   * @param insLength   The length of the insertion
   */
  fun updateMarkFromInsert(editor: FimEditor?, marks: java.util.HashMap<Char, Mark>?, insStartOff: Int, insLength: Int)
  fun dropLastJump()
}
