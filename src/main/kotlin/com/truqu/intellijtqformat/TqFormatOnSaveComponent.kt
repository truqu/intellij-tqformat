package com.truqu.intellijtqformat

import com.intellij.AppTopics
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.erlang.ErlangFileType

class TqFormatOnSaveComponent(val project: Project) : ProjectComponent {
    override fun initComponent() {
        ApplicationManager
            .getApplication()
            .messageBus
            .connect(project)
            .subscribe(
                AppTopics.FILE_DOCUMENT_SYNC,
                object : FileDocumentManagerListener {
                    override fun beforeDocumentSaving(document: Document) {
                        if (shouldRunOn(document)) {
                            formatDocument(project, document)
                        }
                    }
                }
            )
    }

    private fun shouldRunOn(document: Document): Boolean {
        return TqFormatConfiguration(project).onSaveEnabled &&
            isValidPsiDocument(document) &&
            isValidErlangFile(document)
    }

    private fun isValidErlangFile(document: Document): Boolean {
        val vFile = FileDocumentManager.getInstance().getFile(document) ?: return false
        return vFile.fileType is ErlangFileType
    }

    private fun isValidPsiDocument(document: Document): Boolean {
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return false
        return !PsiTreeUtil.hasErrorElements(psiFile)
    }
}
