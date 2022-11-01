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
package com.flop.idea.fim.key

import com.flop.idea.fim.KeyHandler
import com.flop.idea.fim.action.change.Extension.clean
import com.flop.idea.fim.action.change.Extension.lastExtensionHandler
import com.flop.idea.fim.action.change.FimRepeater.repeatHandler
import com.flop.idea.fim.api.ExecutionContext
import com.flop.idea.fim.api.FimCaret
import com.flop.idea.fim.api.FimEditor
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.command.Argument
import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.command.SelectionType.Companion.fromSubMode
import com.flop.idea.fim.command.FimStateMachine
import com.flop.idea.fim.common.Offset
import com.flop.idea.fim.common.argumentCaptured
import com.flop.idea.fim.common.offset
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.extension.ExtensionHandler
import com.flop.idea.fim.group.visual.FimSelection
import com.flop.idea.fim.group.visual.FimSelection.Companion.create
import com.flop.idea.fim.helper.FimNlsSafe
import com.flop.idea.fim.helper.subMode
import com.flop.idea.fim.helper.fimStateMachine
import com.flop.idea.fim.listener.SelectionFimListenerSuppressor
import com.flop.idea.fim.fimscript.model.CommandLineFimLContext
import com.flop.idea.fim.fimscript.model.expressions.Expression
import java.awt.event.KeyEvent
import javax.swing.KeyStroke
import kotlin.math.min

/**
 * @author vlan
 */
sealed class MappingInfo(
  val fromKeys: List<KeyStroke>,
  val isRecursive: Boolean,
  val owner: MappingOwner,
) : Comparable<MappingInfo>, MappingInfoLayer {

  @FimNlsSafe
  abstract override fun getPresentableString(): String

  abstract override fun execute(editor: FimEditor, context: ExecutionContext)

  override fun compareTo(other: MappingInfo): Int {
    val size = fromKeys.size
    val otherSize = other.fromKeys.size
    val n = min(size, otherSize)
    for (i in 0 until n) {
      val diff = compareKeys(fromKeys[i], other.fromKeys[i])
      if (diff != 0) return diff
    }
    return size - otherSize
  }

  private fun compareKeys(key1: KeyStroke, key2: KeyStroke): Int {
    val c1 = key1.keyChar
    val c2 = key2.keyChar
    return when {
      c1 == KeyEvent.CHAR_UNDEFINED && c2 == KeyEvent.CHAR_UNDEFINED -> {
        val keyCodeDiff = key1.keyCode - key2.keyCode
        if (keyCodeDiff != 0) keyCodeDiff else key1.modifiers - key2.modifiers
      }
      c1 == KeyEvent.CHAR_UNDEFINED -> -1
      c2 == KeyEvent.CHAR_UNDEFINED -> 1
      else -> c1 - c2
    }
  }
}

class ToKeysMappingInfo(
  val toKeys: List<KeyStroke>,
  fromKeys: List<KeyStroke>,
  isRecursive: Boolean,
  owner: MappingOwner,
) : MappingInfo(fromKeys, isRecursive, owner) {
  override fun getPresentableString(): String = injector.parser.toKeyNotation(toKeys)

  override fun execute(editor: FimEditor, context: ExecutionContext) {
    LOG.debug("Executing 'ToKeys' mapping info...")
    val editorDataContext = injector.executionContextManager.onEditor(editor, context)
    val fromIsPrefix = KeyHandler.isPrefix(fromKeys, toKeys)
    val keyHandler = KeyHandler.getInstance()
    keyHandler.keyStack.addKeys(toKeys)
    var first = true
    while (keyHandler.keyStack.hasStroke()) {
      val keyStroke = keyHandler.keyStack.feedStroke()
      val recursive = isRecursive && !(first && fromIsPrefix)
      keyHandler.handleKey(editor, keyStroke, editorDataContext, recursive, false)
      first = false
    }
    keyHandler.keyStack.removeFirst()
  }

  companion object {
    private val LOG = fimLogger<ToKeysMappingInfo>()
  }
}

class ToExpressionMappingInfo(
  private val toExpression: Expression,
  fromKeys: List<KeyStroke>,
  isRecursive: Boolean,
  owner: MappingOwner,
  private val originalString: String,
) : MappingInfo(fromKeys, isRecursive, owner) {
  override fun getPresentableString(): String = originalString

  override fun execute(editor: FimEditor, context: ExecutionContext) {
    LOG.debug("Executing 'ToExpression' mapping info...")
    val editorDataContext = injector.executionContextManager.onEditor(editor, context)
    val toKeys = injector.parser.parseKeys(toExpression.evaluate(editor, context, CommandLineFimLContext).toString())
    val fromIsPrefix = KeyHandler.isPrefix(fromKeys, toKeys)
    var first = true
    for (keyStroke in toKeys) {
      val recursive = isRecursive && !(first && fromIsPrefix)
      val keyHandler = KeyHandler.getInstance()
      keyHandler.handleKey(editor, keyStroke, editorDataContext, recursive, false)
      first = false
    }
  }

  companion object {
    private val LOG = fimLogger<ToExpressionMappingInfo>()
  }
}

class ToHandlerMappingInfo(
  private val extensionHandler: ExtensionHandler,
  fromKeys: List<KeyStroke>,
  isRecursive: Boolean,
  owner: MappingOwner,
) : MappingInfo(fromKeys, isRecursive, owner) {
  override fun getPresentableString(): String = "call ${extensionHandler.javaClass.canonicalName}"

  override fun execute(editor: FimEditor, context: ExecutionContext) {
    LOG.debug("Executing 'ToHandler' mapping info...")
    val fimStateMachine = FimStateMachine.getInstance(editor)

    // Cache isOperatorPending in case the extension changes the mode while moving the caret
    // See CommonExtensionTest
    // TODO: Is this legal? Should we assert in this case?
    val shouldCalculateOffsets: Boolean = fimStateMachine.isOperatorPending

    val startOffsets: Map<FimCaret, Offset> = editor.carets().associateWith { it.offset }

    if (extensionHandler.isRepeatable) {
      clean()
    }

    val handler = extensionHandler
    if (handler is ExtensionHandler.WithCallback) {
      handler._backingFunction = Runnable {
        myFun(shouldCalculateOffsets, editor, startOffsets)

        if (shouldCalculateOffsets) {
          injector.application.invokeLater {
            KeyHandler.getInstance().finishedCommandPreparation(
              editor,
              context, FimStateMachine.getInstance(editor), FimStateMachine.getInstance(editor).commandBuilder, null, false
            )
          }
        }
      }
    }

    injector.actionExecutor.executeCommand(
      editor, { extensionHandler.execute(editor, context) },
      "Fim " + extensionHandler.javaClass.simpleName, null
    )

    if (extensionHandler.isRepeatable) {
      lastExtensionHandler = extensionHandler
      argumentCaptured = null
      repeatHandler = true
    }

    if (handler !is ExtensionHandler.WithCallback) {
      myFun(shouldCalculateOffsets, editor, startOffsets)
    }
  }

  companion object {
    private val LOG = fimLogger<ToHandlerMappingInfo>()

    private fun myFun(
      shouldCalculateOffsets: Boolean,
      editor: FimEditor,
      startOffsets: Map<FimCaret, Offset>,
    ) {
      val commandState = editor.fimStateMachine
      if (shouldCalculateOffsets && !commandState.commandBuilder.hasCurrentCommandPartArgument()) {
        val offsets: MutableMap<FimCaret, FimSelection> = HashMap()
        for (caret in editor.carets()) {
          var startOffset = startOffsets[caret]
          if (caret.hasSelection()) {
            val fimSelection = create(caret.fimSelectionStart, caret.offset.point, fromSubMode(editor.subMode), editor)
            offsets[caret] = fimSelection
            commandState.popModes()
          } else if (startOffset != null && startOffset.point != caret.offset.point) {
            // Command line motions are always characterwise exclusive
            var endOffset = caret.offset
            if (startOffset.point < endOffset.point) {
              endOffset = (endOffset.point - 1).offset
            } else {
              startOffset = (startOffset.point - 1).offset
            }
            val fimSelection = create(startOffset.point, endOffset.point, SelectionType.CHARACTER_WISE, editor)
            offsets[caret] = fimSelection
            SelectionFimListenerSuppressor.lock().use {
              // Move caret to the initial offset for better undo action
              //  This is not a necessary thing, but without it undo action look less convenient
              editor.currentCaret().moveToOffset(startOffset.point)
            }
          }
        }
        if (offsets.isNotEmpty()) {
          commandState.commandBuilder.completeCommandPart(Argument(offsets))
        }
      }
    }
  }
}

class ToActionMappingInfo(
  val action: String,
  fromKeys: List<KeyStroke>,
  isRecursive: Boolean,
  owner: MappingOwner,
) : MappingInfo(fromKeys, isRecursive, owner) {
  override fun getPresentableString(): String = "action $action"

  override fun execute(editor: FimEditor, context: ExecutionContext) {
    LOG.debug("Executing 'ToAction' mapping...")
    val editorDataContext = injector.executionContextManager.onEditor(editor, context)
    val dataContext = injector.executionContextManager.createCaretSpecificDataContext(editorDataContext, editor.currentCaret())
    injector.actionExecutor.executeAction(action, dataContext)
  }

  companion object {
    private val LOG = fimLogger<ToActionMappingInfo>()
  }
}
