package com.truqu.intellijtqformat

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import javax.swing.JCheckBox
import javax.swing.JComponent

class TqFormatConfiguration(private val project: Project) : Configurable {
    var tqformatPathInput: String = tqformatPath
    private var onSaveCheckbox = {
        val checkbox = JCheckBox()
        checkbox.isSelected = onSaveEnabled
        checkbox
    }.invoke()

    override fun isModified(): Boolean {
        return tqformatPathInput != tqformatPath || onSaveCheckbox.isSelected != onSaveEnabled
    }

    override fun getDisplayName(): String = "TQFormat"

    override fun apply() {
        val propertiesComponent = PropertiesComponent.getInstance(project)
        propertiesComponent.setValue("com.truqu.tqformat.path", tqformatPath)
        propertiesComponent.setValue("com.truqu.tqformat.on_save", onSaveCheckbox.isSelected)
    }

    override fun createComponent(): JComponent? {
        val self = this
        return panel {
            row {
                label("Path to TQFormat executable")
                tqFormatPathPicker(self)
            }
            row("Format on save?") { onSaveCheckbox() }
        }
    }

    private fun Row.tqFormatPathPicker(self: TqFormatConfiguration) {
        textFieldWithBrowseButton(
            self::tqformatPathInput,
            "Path to TQFormat executable",
            project,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
            {
                self.tqformatPathInput = it.path
                it.path
            }
        )
    }

    val tqformatPath: String
        get() = properties.getValue("com.truqu.tqformat.path", "")

    val onSaveEnabled: Boolean
        get() = properties.getBoolean("com.truqu.tqformat.on_save", false)

    val properties
        get() = PropertiesComponent.getInstance(project)
}
