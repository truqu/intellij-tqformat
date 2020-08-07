package com.truqu.intellijtqformat

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.io.OutputStreamWriter
import org.intellij.erlang.ErlangFileType
import org.intellij.erlang.jps.model.JpsErlangSdkType
import org.intellij.erlang.sdk.ErlangSdkType
import org.jetbrains.concurrency.runAsync

class TqFormatAction : AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = getContext(e) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val ctx = getContext(e) ?: error("null context?")
        formatDocument(ctx.project, ctx.document)
    }

    private fun getContext(e: AnActionEvent): Context? {
        val project = e.project
        val file = validatedFile(e)
        if (project != null && file != null) return getContext(project, file)
        return null
    }

    private fun getContext(project: Project, file: VirtualFile): Context? {
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return null
        return Context(project, document)
    }

    data class Context(
        val project: Project,
        val document: Document
    )

    companion object {
        const val ID = "TQ.Format"
    }
}

private fun validatedFile(e: AnActionEvent): VirtualFile? {
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    if (file != null && file.isInLocalFileSystem && file.fileType is ErlangFileType) {
        return file
    }
    return null
}

fun formatDocument(project: Project, document: Document) {
    val eScriptPath = getEscriptPath(project) ?: return
    val tqFormatPath = TqFormatConfiguration(project).tqformatPath
    val handler = createHandler(eScriptPath, tqFormatPath, document.text)

    invokeLater {
        runAsync { handler.runProcess() }
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

private fun getEscriptPath(project: Project): String? {
    val sdkHome = ErlangSdkType.getSdkPath(project) ?: return null
    return JpsErlangSdkType.getScriptInterpreterExecutable(sdkHome).absolutePath
}
