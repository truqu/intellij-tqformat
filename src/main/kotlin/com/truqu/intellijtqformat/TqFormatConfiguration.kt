package com.truqu.intellijtqformat

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import kotlin.reflect.KProperty

class TqFormatConfiguration(private val project: Project) : Configurable {
    private val settings = TqFormatSettings(project)

    private val tqFormatPathField = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            "Select path",
            "Select the tqformat escript",
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
        )
    }
    private var tqformatPathInput: String by PathSelectorDelegate(tqFormatPathField)

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
            row("Path to tqformat executable") { tqFormatPathField() }
            row {
                onSaveCheckbox().comment("Format all Erlang files when persisting to disk")
            }
        }
}

private class PathSelectorDelegate(private val pathSelector: TextFieldWithBrowseButton) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String = pathSelector.text

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        pathSelector.text = value
    }
}

private class CheckboxDelegate(private val checkbox: JBCheckBox) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = checkbox.isSelected

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        checkbox.isSelected = value
    }
}
