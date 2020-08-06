package com.truqu.intellijtqformat

import com.intellij.AppTopics
import com.intellij.ide.util.PropertiesComponent
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
        ApplicationManager.getApplication().messageBus.connect(project).subscribe(AppTopics.FILE_DOCUMENT_SYNC, object : FileDocumentManagerListener {
            override fun beforeDocumentSaving(document: Document) {
                if (!PropertiesComponent.getInstance(project).getBoolean("com.truqu.tqformat.on_save", false)) return
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return
                if (PsiTreeUtil.hasErrorElements(psiFile)) return

                val vFile = FileDocumentManager.getInstance().getFile(document) ?: return
                if (vFile.fileType !is ErlangFileType) return

                formatDocument(project, document)
            }
        })
    }
}
