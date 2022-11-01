package com.flop.idea.fim.api

import com.flop.idea.fim.api.stubs.ExecutionContextManagerStub
import com.flop.idea.fim.api.stubs.FimApplicationStub
import com.flop.idea.fim.api.stubs.FimEnablerStub
import com.flop.idea.fim.api.stubs.FimMessagesStub
import com.flop.idea.fim.api.stubs.FimProcessGroupStub
import com.flop.idea.fim.common.FimMachine
import com.flop.idea.fim.common.FimMachineBase
import com.flop.idea.fim.diagnostic.fimLogger
import com.flop.idea.fim.register.FimRegisterGroup
import com.flop.idea.fim.register.FimRegisterGroupBase
import com.flop.idea.fim.fimscript.services.OptionService
import com.flop.idea.fim.fimscript.services.VariableService
import com.flop.idea.fim.fimscript.services.FimVariableServiceBase

abstract class FimInjectorBase : FimInjector {
  companion object {
    val logger by lazy { fimLogger<FimInjectorBase>() }
    val registerGroupStub by lazy { object : FimRegisterGroupBase() {} }
  }

  override val parser: FimStringParser = object : FimStringParserBase() {}
  override val fimMachine: FimMachine = object : FimMachineBase() {}
  override val optionService: OptionService by lazy { object : FimOptionServiceBase() {} }
  override val variableService: VariableService by lazy { object : FimVariableServiceBase() {} }

  override val registerGroup: FimRegisterGroup by lazy { registerGroupStub }
  override val registerGroupIfCreated: FimRegisterGroup? by lazy { registerGroupStub }
  override val messages: FimMessages by lazy { FimMessagesStub() }
  override val processGroup: FimProcessGroup by lazy { FimProcessGroupStub() }
  override val application: FimApplication by lazy { FimApplicationStub() }
  override val executionContextManager: ExecutionContextManager by lazy { ExecutionContextManagerStub() }
  override val enabler: FimEnabler by lazy { FimEnablerStub() }
}
