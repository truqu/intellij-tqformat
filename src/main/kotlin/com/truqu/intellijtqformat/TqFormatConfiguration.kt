package com.truqu.intellijtqformat

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

private const val V_PATH = "com.truqu.tqformat.path"
private const val V_ON_SAVE = "com.truqu.tqformat.on_save"

class TqFormatConfiguration(private val project: Project) : Configurable {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)

    private var tqformatPathInput: String = tqformatPath
    private var onSaveChecked: Boolean = onSaveEnabled

    val tqformatPath: String
        get() = properties.getValue(V_PATH, "")
    val onSaveEnabled: Boolean
        get() = properties.getBoolean(V_ON_SAVE, false)

    override fun isModified(): Boolean = tqformatPathInput != tqformatPath || onSaveChecked != onSaveEnabled

    override fun getDisplayName(): String = "TQFormat"

    override fun apply() {
        properties.setValue(V_PATH, tqformatPathInput)
        properties.setValue(V_ON_SAVE, onSaveChecked)
    }

    override fun createComponent(): JComponent? {
        return panel {
            row("Executable") { tqFormatPathPicker() }
            row { checkBox("Format on save", this@TqFormatConfiguration::onSaveChecked) }
            commentRow(
                """
                    In order for formatting to work, you must have an Erlang SDK selected.
                """.trimIndent()
            )
        }
    }

    private fun Row.tqFormatPathPicker() {
        textFieldWithBrowseButton(
            this@TqFormatConfiguration::tqformatPathInput,
            "Path to TQFormat executable",
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
            {
                this@TqFormatConfiguration.tqformatPathInput = it.path
                it.path
            }
        ).apply {
            this.component.childComponent.document.addDocumentListener(object : DocumentAdapter() {
                override fun textChanged(e: DocumentEvent) {
                    this@TqFormatConfiguration.tqformatPathInput = this@apply.component.childComponent.text
                    this@TqFormatConfiguration.tqformatPathInput
                }
            })
        }
    }
}
