package com.flop.idea.fim.put

import com.flop.idea.fim.command.SelectionType

data class ProcessedTextData(
  val text: String,
  val typeInRegister: SelectionType,
  val transferableData: List<Any>,
)
