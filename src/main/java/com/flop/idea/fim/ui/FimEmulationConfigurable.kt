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
package com.flop.idea.fim.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBoxTableRenderer
import com.intellij.openapi.ui.StripeTable
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.DumbAwareActionButton
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.UIUtil
import com.flop.idea.fim.api.injector
import com.flop.idea.fim.helper.MessageHelper.message
import com.flop.idea.fim.key.ShortcutOwner
import com.flop.idea.fim.key.ShortcutOwnerInfo
import com.flop.idea.fim.key.ShortcutOwnerInfo.AllModes
import com.flop.idea.fim.key.ShortcutOwnerInfo.PerMode
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.*
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.KeyStroke
import javax.swing.border.LineBorder
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import javax.swing.table.TableColumn

/**
 * @author vlan
 */
class FimEmulationConfigurable : Configurable {
  private val settingsPanel: FimSettingsPanel by lazy { FimSettingsPanel() }

  override fun getDisplayName(): String = message("configurable.name.fim.emulation")

  override fun getHelpTopic(): String? {
    return null
  }

  // The component here should be created on demand, only after calling this method.
  // We're trying to do that by initializing settingsPanel in lazy
  override fun createComponent(): JComponent {
    return settingsPanel
  }

  override fun isModified(): Boolean {
    return settingsPanel.model.isModified
  }

  override fun apply() {
    settingsPanel.model.apply()
  }

  override fun reset() {
    settingsPanel.model.reset()
  }

  override fun disposeUIResources() {}

  private class FimSettingsPanel() : JPanel() {

    val model: FimShortcutConflictsTable.Model = FimShortcutConflictsTable.Model()

    init {
      val shortcutConflictsTable = FimShortcutConflictsTable(model)
      layout = BorderLayout()
      val decorator = ToolbarDecorator.createDecorator(shortcutConflictsTable)
      decorator.setToolbarPosition(ActionToolbarPosition.RIGHT)
      decorator.addExtraAction(CopyForRcAction(model))
      decorator.addExtraAction(ResetHandlersAction(model, shortcutConflictsTable))
      val scrollPane = decorator.createPanel()
      scrollPane.border = LineBorder(JBColor.border())
      val conflictsPanel = JPanel(BorderLayout())
      val title = message("border.title.shortcut.conflicts.for.active.keymap")
      conflictsPanel.border = IdeBorderFactory.createTitledBorder(title, false)
      conflictsPanel.add(scrollPane)
      add(conflictsPanel, BorderLayout.CENTER)
      addHelpLine(model)
    }

    fun addHelpLine(model: FimShortcutConflictsTable.Model) {
      val firstPerMode = ContainerUtil.find(model.rows) { row: FimShortcutConflictsTable.Row ->
        val owner: ShortcutOwnerInfo = row.owner
        owner is PerMode
      }
      if (firstPerMode == null) {
        val label = HyperlinkLabel()
        label.setHtmlText(message("configurable.keyhandler.link"))
        label.setHyperlinkTarget("https://jb.gg/fim-sethandler")
        label.foreground = UIUtil.getInactiveTextColor()
        add(label, BorderLayout.SOUTH)
      } else {
        val helpLine = JBLabel()
        helpLine.text = message(
          "configurable.noneditablehandler.helper.text.with.example",
          (firstPerMode.owner as PerMode).toNotation(),
          KeymapUtil.getShortcutText(KeyboardShortcut(firstPerMode.keyStroke, null))
        )
        helpLine.foreground = UIUtil.getInactiveTextColor()
        add(helpLine, BorderLayout.SOUTH)
      }
    }
  }

  class FimShortcutConflictsTable(model: Model) : StripeTable(model) {
    private val renderer: ComboBoxTableRenderer<ShortcutOwner> = ShortcutOwnerRenderer()

    init {
      getTableColumn(Column.KEYSTROKE).preferredWidth = 100
      getTableColumn(Column.IDE_ACTION).preferredWidth = 400
      val ownerColumn = getTableColumn(Column.OWNER)
      ownerColumn.preferredWidth = 150
    }

    override fun getCellRenderer(row: Int, column: Int): TableCellRenderer {
      if (column != Column.OWNER.index) return super.getCellRenderer(row, column)
      val model = model as Model
      val owner: ShortcutOwnerInfo = model.rows[row].owner
      return if (owner is PerMode) super.getCellRenderer(row, column) else renderer
    }

    override fun getCellEditor(row: Int, column: Int): TableCellEditor {
      if (column != Column.OWNER.index) return super.getCellEditor(row, column)
      val model = model as Model
      val owner: ShortcutOwnerInfo = model.rows[row].owner
      return if (owner is PerMode) super.getCellEditor(row, column) else renderer
    }

    override fun getMinimumSize(): Dimension {
      return calcSize(super.getMinimumSize())
    }

    override fun getPreferredSize(): Dimension {
      return calcSize(super.getPreferredSize())
    }

    private fun calcSize(dimension: Dimension): Dimension {
      val container = parent
      if (container != null) {
        val size = container.size
        return Dimension(size.width, dimension.height)
      }
      return dimension
    }

    private fun getTableColumn(column: Column): TableColumn {
      return getColumnModel().getColumn(column.index)
    }

    private class ShortcutOwnerRenderer : ComboBoxTableRenderer<ShortcutOwner>(ShortcutOwner.values()) {
      override fun customizeComponent(owner: ShortcutOwner, table: JTable, isSelected: Boolean) {
        super.customizeComponent(owner, table, isSelected)
        if (owner == ShortcutOwner.UNDEFINED) {
          foreground = UIUtil.getComboBoxDisabledForeground()
        }
      }

      override fun isCellEditable(event: EventObject): Boolean {
        return true
      }
    }

    private enum class Column(val index: Int, val title: @Nls(capitalization = Nls.Capitalization.Title) String) {
      KEYSTROKE(0, "Shortcut"),
      IDE_ACTION(1, "IDE Action"),
      OWNER(2, "Handler");

      companion object {
        private val ourMembers: MutableMap<Int, Column> = HashMap()

        init {
          for (column in values()) {
            ourMembers[column.index] = column
          }
        }

        fun fromIndex(index: Int): Column? {
          return ourMembers[index]
        }
      }
    }

    class Row(val keyStroke: KeyStroke, val action: AnAction, var owner: ShortcutOwnerInfo) : Comparable<Row> {

      override fun compareTo(other: Row): Int {
        val otherKeyStroke: KeyStroke = other.keyStroke
        val keyCodeDiff: Int = keyStroke.keyCode - otherKeyStroke.keyCode
        return if (keyCodeDiff != 0) keyCodeDiff else keyStroke.modifiers - otherKeyStroke.modifiers
      }
    }

    class Model : AbstractTableModel() {
      val rows: MutableList<Row> = ArrayList()

      init {
        reset()
      }

      override fun getRowCount(): Int {
        return rows.size
      }

      override fun getColumnCount(): Int {
        return Column.values().size
      }

      override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
        val column = Column.fromIndex(columnIndex)
        if (column != null && rowIndex >= 0 && rowIndex < rows.size) {
          val row = rows[rowIndex]
          when (column) {
            Column.KEYSTROKE -> return KeymapUtil.getShortcutText(KeyboardShortcut(row.keyStroke, null))
            Column.IDE_ACTION -> return row.action.templatePresentation.text
            Column.OWNER -> {
              val owner: ShortcutOwnerInfo = row.owner
              if (owner is AllModes) {
                return owner.owner
              } else if (owner is PerMode) {
                return owner.toNotation()
              }
            }
          }
        }
        return null
      }

      override fun setValueAt(`object`: Any, rowIndex: Int, columnIndex: Int) {
        val column = Column.fromIndex(columnIndex)
        if (column != null && rowIndex >= 0 && rowIndex < rows.size && `object` is ShortcutOwner) {
          val row = rows[rowIndex]
          row.owner = AllModes(`object`)
        }
      }

      override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return if (rows[rowIndex].owner is PerMode) false else Column.fromIndex(columnIndex) == Column.OWNER
      }

      override fun getColumnName(index: Int): String? {
        return Column.fromIndex(index)?.title
      }

      val isModified: Boolean
        get() = com.flop.idea.fim.FimPlugin.getKey().shortcutConflicts != currentData

      fun apply() {
        com.flop.idea.fim.FimPlugin.getKey().savedShortcutConflicts.putAll(currentData)
      }

      fun reset() {
        rows.clear()
        for ((keyStroke, value) in com.flop.idea.fim.FimPlugin.getKey().shortcutConflicts) {
          val actions = com.flop.idea.fim.FimPlugin.getKey().getKeymapConflicts(keyStroke)
          if (actions.isNotEmpty()) {
            rows.add(Row(keyStroke, actions[0].action as AnAction, value))
          }
        }
        rows.sort()
      }

      private val currentData: Map<KeyStroke, ShortcutOwnerInfo>
        get() {
          val result: MutableMap<KeyStroke, ShortcutOwnerInfo> = HashMap()
          for (row in rows) {
            result[row.keyStroke] = row.owner
          }
          return result
        }
    }
  }

  private class CopyForRcAction(
    private val myModel: FimShortcutConflictsTable.Model,
  ) : DumbAwareActionButton(
    "Copy Config for .ideafimrc",
    "Copy config for .ideafimrc in sethandler format",
    AllIcons.Actions.Copy
  ) {

    override fun updateButton(e: AnActionEvent) {
      val enabled: Boolean = myModel.rows.stream().anyMatch {
        it.owner is AllModes && (it.owner as AllModes).owner != ShortcutOwner.UNDEFINED
      }
      e.presentation.isEnabled = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
      val stringBuilder = StringBuilder()
      for (row in myModel.rows) {
        val ownerInfo: ShortcutOwnerInfo = row.owner as? AllModes ?: continue
        val owner = (ownerInfo as AllModes).owner
        if (owner === ShortcutOwner.UNDEFINED) continue
        stringBuilder.append("sethandler ")
        stringBuilder.append(injector.parser.toKeyNotation(row.keyStroke))
        stringBuilder.append(" ")
        stringBuilder.append("a:")
        stringBuilder.append(owner.ownerName)
        stringBuilder.append("\n")
      }
      val data = stringBuilder.toString()
      injector.clipboardManager.setClipboardText(data, data, emptyList())
    }
  }

  class ResetHandlersAction(
    private val myModel: FimShortcutConflictsTable.Model,
    private val myTable: FimShortcutConflictsTable,
  ) : DumbAwareActionButton("Reset Handlers", "Reset handlers", AllIcons.General.Reset) {
    override fun updateButton(e: AnActionEvent) {
      val enabled: Boolean = myModel.rows.stream().anyMatch {
        it.owner is AllModes && (it.owner as AllModes).owner != ShortcutOwner.UNDEFINED
      }
      e.presentation.isEnabled = enabled
    }

    override fun actionPerformed(e: AnActionEvent) {
      TableUtil.stopEditing(myTable)
      for (row in myModel.rows) {
        val owner: ShortcutOwnerInfo = row.owner
        if (owner is AllModes) {
          if (owner.owner != ShortcutOwner.UNDEFINED) {
            row.owner = ShortcutOwnerInfo.allUndefined
          }
        }
      }
      IdeFocusManager.getGlobalInstance()
        .doWhenFocusSettlesDown { IdeFocusManager.getGlobalInstance().requestFocus(myTable, true) }
      TableUtil.updateScroller(myTable)
    }
  }
}
