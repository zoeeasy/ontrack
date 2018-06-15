package net.nemerosa.ontrack.model.structure

interface ProjectLabelService {

    fun getProjectLabels(project: Project): List<Label>

    fun addProjectLabel(project: Project, label: Label)

    fun removeProjectLabel(project: Project, label: Label)

    fun setProjectLabels(project: Project, labels: List<Label>)

}