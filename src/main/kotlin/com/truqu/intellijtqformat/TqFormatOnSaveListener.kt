package com.truqu.intellijtqformat

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.erlang.ErlangFileType

const val TIMEOUT = 2000

class TqFormatOnSaveListener : FileDocumentManagerListener {
    override fun beforeDocumentSaving(document: Document) {
        val project = getProject() ?: return
        if (shouldRunOn(project, document)) {
            formatDocument(project, document)
        }
    }

    private fun getProject(): Project? {
        return DataManager.getInstance().dataContextFromFocusAsync.blockingGet(TIMEOUT)?.getData(CommonDataKeys.PROJECT)
    }

    private fun shouldRunOn(project: Project, document: Document): Boolean {
        return TqFormatConfiguration(project).onSaveEnabled &&
            isValidPsiDocument(project, document) &&
            isValidErlangFile(document)
    }

    private fun isValidErlangFile(document: Document): Boolean {
        val vFile = FileDocumentManager.getInstance().getFile(document) ?: return false
        return vFile.fileType is ErlangFileType
    }

    private fun isValidPsiDocument(project: Project, document: Document): Boolean {
        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return false
        return !PsiTreeUtil.hasErrorElements(psiFile)
    }
}
