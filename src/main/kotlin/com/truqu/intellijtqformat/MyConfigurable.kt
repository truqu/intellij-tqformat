package com.truqu.intellijtqformat

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import javax.swing.JCheckBox
import javax.swing.JComponent

class MyConfigurable(private val project: Project) : Configurable {
    var tqformatPath: String = getCurrent()
    private var onSaveCheckbox = {
        val checkbox = JCheckBox()
        checkbox.isSelected = onSave()
        checkbox
    }.invoke()


    override fun isModified(): Boolean = tqformatPath != getCurrent() || onSaveCheckbox.isSelected != onSave()

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
                textFieldWithBrowseButton(self::tqformatPath, "Path to TQFormat executable", project, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(), {
                    self.tqformatPath = it.path
                    it.path
                })
            }
            row("Format on save?") { onSaveCheckbox() }
        }
    }

    private fun getCurrent(): String = PropertiesComponent.getInstance(project).getValue("com.truqu.tqformat.path", "")
    private fun onSave(): Boolean = PropertiesComponent.getInstance(project).getBoolean("com.truqu.tqformat.on_save", false)
}
