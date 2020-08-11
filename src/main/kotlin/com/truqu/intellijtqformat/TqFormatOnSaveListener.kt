package com.truqu.intellijtqformat

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.erlang.ErlangFileType

class TqFormatOnSaveListener(private val project: Project) : FileDocumentManagerListener {
    override fun beforeDocumentSaving(document: Document) {
        if (shouldRunOn(document)) {
            invokeLater { formatDocument(project, document) }
        }
    }
    private fun shouldRunOn(document: Document): Boolean {
        return TqFormatSettings(project).onSave &&
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
