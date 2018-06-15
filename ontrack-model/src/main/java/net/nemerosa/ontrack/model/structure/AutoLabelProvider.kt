package net.nemerosa.ontrack.model.structure

/**
 * This interface defines a component which can assign an
 * automated label to a project.
 */
interface AutoLabelProvider {

    /**
     * Label being provided for a project, if any. Only the category, the name and description
     * are required. The registration of the label and its
     * colour are handled separately.
     */
    fun getProjectLabel(project: Project): AutoLabelDescription?

}
