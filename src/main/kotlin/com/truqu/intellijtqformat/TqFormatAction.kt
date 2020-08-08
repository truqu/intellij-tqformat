package com.truqu.intellijtqformat

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.intellij.erlang.ErlangFileType

class TqFormatAction : AnAction() {
    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.isEnabled = getContext(e) != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val ctx = getContext(e) ?: error("null context?")
        invokeLater { formatDocument(ctx.project, ctx.document) }
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
