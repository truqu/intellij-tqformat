package com.github.zwilias.intellijtqformat.services

import com.intellij.openapi.project.Project
import com.github.zwilias.intellijtqformat.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
