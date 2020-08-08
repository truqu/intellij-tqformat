package com.truqu.intellijtqformat

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import java.io.OutputStreamWriter
import org.intellij.erlang.jps.model.JpsErlangSdkType
import org.intellij.erlang.sdk.ErlangSdkType
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.runAsync

fun formatDocument(project: Project, document: Document): Promise<Unit>? {
    val eScriptPath = getEScriptPath(project) ?: return null
    val tqFormatPath = TqFormatConfiguration(project).tqformatPath
    val handler = createHandler(eScriptPath, tqFormatPath, document.text)

    return runAsync { handler.runProcess() }
        .then { done: ProcessOutput ->
            if (!done.isCancelled && done.exitCode == 0) {
                runInEdt {
                    CommandProcessor.getInstance().executeCommand(
                        project,
                        { WriteAction.run<Throwable> { document.setText(done.stdout) } },
                        "Run tqformat on current file",
                        null,
                        document
                    )
                }
            }
        }
}

private fun createHandler(eScriptPath: String, tqFormatPath: String, stdIn: String): CapturingProcessHandler {
    val cli = GeneralCommandLine(eScriptPath)
        .withParameters(tqFormatPath, "-")

    val process = cli.createProcess()
    val stdInStream = process.outputStream
    val writer = OutputStreamWriter(stdInStream, Charsets.UTF_8)
    writer.write(stdIn)
    writer.flush()
    writer.close()
    return CapturingProcessHandler(process, Charsets.UTF_8, cli.commandLineString)
}

fun getEScriptPath(project: Project): String? {
    val sdkHome = ErlangSdkType.getSdkPath(project) ?: return null
    return JpsErlangSdkType.getScriptInterpreterExecutable(sdkHome).absolutePath
}
