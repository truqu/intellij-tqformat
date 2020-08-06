package com.truqu.intellijtqformat

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.erlang.ErlangFileType
import org.intellij.erlang.jps.model.JpsErlangSdkType
import org.intellij.erlang.sdk.ErlangSdkType
import java.io.OutputStreamWriter

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

    val result = ProgressManager
        .getInstance()
        .runProcessWithProgressSynchronously<ProcessOutput, ExecutionException>(
            {
                val cli = GeneralCommandLine(eScriptPath)
                    .withParameters(TqFormatConfiguration(project).tqformatPath, "-")

                val process = cli.createProcess()
                val stdInStream = process.outputStream
                val writer = OutputStreamWriter(stdInStream, Charsets.UTF_8)
                writer.write(document.text)
                writer.flush()
                writer.close()
                CapturingProcessHandler(process, Charsets.UTF_8, cli.commandLineString).runProcess()
            },
            "Running TQFormat",
            true,
            project
        )

    if (!result.isCancelled && result.exitCode == 0) {
        val formatted = result.stdout
        val source = document.text

        if (source != formatted) {
            CommandProcessor.getInstance().executeCommand(
                project,
                {
                    ApplicationManager.getApplication().runWriteAction {
                        document.setText(formatted)
                    }
                },
                "Run elm-format on current file",
                null,
                document
            )
        }
    } else {
        error(result)
    }
}

private fun getEscriptPath(project: Project): String? {
    val sdkHome = ErlangSdkType.getSdkPath(project) ?: return null
    return JpsErlangSdkType.getScriptInterpreterExecutable(sdkHome).absolutePath
}
