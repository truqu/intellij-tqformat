package com.truqu.intellijtqformat

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.panel
import com.intellij.util.Alarm
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import kotlin.reflect.KProperty
import org.jetbrains.annotations.Nullable

private const val DELAY_MILLIS = 200

class TqFormatConfiguration(private val project: Project) : Configurable, @Nullable Disposable {
    private val settings = TqFormatSettings(project)

    private val tqFormatPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Select path",
            "Select the tqformat escript",
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )
        text = settings.path
        childComponent.document.addDocumentListener(object : DocumentListener {
            override fun changedUpdate(e: DocumentEvent?) = changed()
            override fun insertUpdate(e: DocumentEvent?) = changed()
            override fun removeUpdate(e: DocumentEvent?) = changed()
            private fun changed() {
                tqformatPathInput = childComponent.text
            }
        })
    }
    private var tqformatPathInput: String = settings.path
        set(value) {
            field = value
            updateVersion()
        }
    private val tqformatVersionField = JLabel().apply {
        foreground = UIUtil.getContextHelpForeground()
    }

    private val onSaveCheckbox = JBCheckBox("Format on save")
    private var onSaveChecked: Boolean by CheckboxDelegate(onSaveCheckbox)

    override fun isModified(): Boolean = tqformatPathInput != settings.path || onSaveChecked != settings.onSave

    override fun reset() {
        tqformatPathInput = settings.path
        onSaveChecked = settings.onSave
    }

    override fun getDisplayName(): String = "TQFormat"

    override fun apply() {
        settings.path = tqformatPathInput
        settings.onSave = onSaveChecked
        settings.persist()
    }

    override fun createComponent(): JComponent? =
        panel {
            if (getEScriptPath(project) == null) {
                commentRow(
                    """
                        You do not have an Erlang SDK configured for this project.<br>
                        The TQFormat escript is invoked using the selected Erlang SDK, which means you
                        <strong>must</strong> select an Erlang SDK in the Project Structure.
                    """.trimIndent()
                )
            }
            row("Path to tqformat executable") { tqFormatPathField(); tqformatVersionField() }
            row {
                onSaveCheckbox().comment("Format all Erlang files when persisting to disk")
            }
        }

    private fun updateVersion() {
        val alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
        alarm.cancelAllRequests()
        alarm.addRequest(
            {
                val r = findVersion(getEScriptPath(project), tqformatPathInput)
                invokeLater(ModalityState.any()) {
                    tqformatVersionField.text = r
                }
            },
            DELAY_MILLIS
        )
    }

    override fun dispose() = Unit
}

private fun findVersion(escriptPath: String?, tqformatPath: String): String {
    if (escriptPath == null) return "No SDK"
    val cli = GeneralCommandLine(escriptPath)
        .withParameters(tqformatPath, "--version")
    val handler = CapturingProcessHandler(cli)
    val result = handler.runProcess()
    return if (result.exitCode == 0) result.stdout else "Invalid"
}

private class CheckboxDelegate(private val checkbox: JBCheckBox) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = checkbox.isSelected

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        checkbox.isSelected = value
    }
}
