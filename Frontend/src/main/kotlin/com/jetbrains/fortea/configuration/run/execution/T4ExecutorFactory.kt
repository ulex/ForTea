package com.jetbrains.fortea.configuration.run.execution

import com.intellij.execution.CantRunException
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.jetbrains.fortea.configuration.run.T4RunConfigurationParameters
import com.jetbrains.fortea.model.t4ProtocolModel
import com.jetbrains.rd.platform.util.getComponent
import com.jetbrains.rd.util.lifetime.Lifetime
import com.jetbrains.rider.projectView.solution
import com.jetbrains.rider.run.configurations.AsyncExecutorFactory
import com.jetbrains.rider.run.configurations.exe.ProcessExecutionDetails
import com.jetbrains.rider.runtime.DotNetRuntime
import com.jetbrains.rider.runtime.RiderDotNetActiveRuntimeHost

class T4ExecutorFactory(project: Project, private val parameters: T4RunConfigurationParameters) : AsyncExecutorFactory {
  private val riderDotNetActiveRuntimeHost = project.getComponent<RiderDotNetActiveRuntimeHost>()
  override suspend fun create(
    executorId: String,
    environment: ExecutionEnvironment,
    lifetime: Lifetime,
  ): RunProfileState {
    val dotNetExecutable = parameters.toDotNetExecutableSuspending(ProcessExecutionDetails.Default)
    val runtimeToExecute = DotNetRuntime.detectRuntimeForExeOrThrow(
      environment.project,
      riderDotNetActiveRuntimeHost,
      dotNetExecutable.exePath,
      dotNetExecutable.runtimeType,
      dotNetExecutable.projectTfm
    )
    val model = environment.project.solution.t4ProtocolModel
    return when (executorId) {
      DefaultRunExecutor.EXECUTOR_ID -> {
        val wrappee = runtimeToExecute.createRunState(dotNetExecutable, environment)
        T4RunProfileWrapperState(wrappee, model, parameters)
      }
      DefaultDebugExecutor.EXECUTOR_ID -> {
        val wrappee = runtimeToExecute.createDebugState(dotNetExecutable, environment)
        T4DebugProfileWrapperState(wrappee, model, parameters)
      }
      else -> throw CantRunException("Unsupported executor $executorId")
    }
  }
}
