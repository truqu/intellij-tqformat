package com.truqu.intellijtqformat

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

private const val V_PATH = "com.truqu.tqformat.path"
private const val V_ON_SAVE = "com.truqu.tqformat.on_save"

class TqFormatSettings(project: Project) {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance(project)
    var path: String = properties.getValue(V_PATH, "")
    var onSave: Boolean = properties.getBoolean(V_ON_SAVE, false)

    fun persist() {
        properties.setValue(V_PATH, path)
        properties.setValue(V_ON_SAVE, onSave)
    }
}
