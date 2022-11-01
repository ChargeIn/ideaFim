package com.flop.idea.fim.helper

import com.flop.idea.fim.api.FimEditor

interface FimCommandLineHelper {
  fun inputString(fimEditor: FimEditor, prompt: String, finishOn: Char?): String?
}
