package com.flop.idea.fim.api

import com.flop.idea.fim.command.SelectionType
import com.flop.idea.fim.common.TextRange
import com.flop.idea.fim.register.Register
import com.flop.idea.fim.register.RegisterConstants
import com.flop.idea.fim.register.FimRegisterGroupBase
import javax.swing.KeyStroke

abstract class FimCaretBase : FimCaret

open class CaretRegisterStorageBase : CaretRegisterStorage, FimRegisterGroupBase() {
  override var lastRegisterChar: Char
    get() {
      return injector.registerGroup.lastRegisterChar
    }
    set(_) {}

  override fun storeText(caret: FimCaret, editor: FimEditor, range: TextRange, type: SelectionType, isDelete: Boolean): Boolean {
    if (caret.isPrimary) {
      return injector.registerGroup.storeText(editor, range, type, isDelete)
    }
    val register = lastRegisterChar
    if (!RegisterConstants.RECORDABLE_REGISTERS.contains(register)) {
      return false
    }
    return super.storeText(editor, range, type, isDelete)
  }

  override fun getRegister(caret: FimCaret, r: Char): Register? {
    if (caret.isPrimary || !RegisterConstants.RECORDABLE_REGISTERS.contains(r)) {
      return injector.registerGroup.getRegister(r)
    }
    return super.getRegister(r) ?: injector.registerGroup.getRegister(r)
  }

  override fun setKeys(caret: FimCaret, register: Char, keys: List<KeyStroke>) {
    if (caret.isPrimary) {
      injector.registerGroup.setKeys(register, keys)
    }
    if (!RegisterConstants.RECORDABLE_REGISTERS.contains(register)) {
      return
    }
    return super.setKeys(register, keys)
  }

  override fun saveRegister(caret: FimCaret, r: Char, register: Register) {
    if (caret.isPrimary) {
      injector.registerGroup.saveRegister(r, register)
    }
    if (!RegisterConstants.RECORDABLE_REGISTERS.contains(r)) {
      return
    }
    return super.saveRegister(r, register)
  }
}
