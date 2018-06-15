package net.nemerosa.ontrack.model.structure

/**
 * This service collects labels for a project when they
 * can be computed.
 */
interface AutoLabelService {

    fun getLabelsForProject(project: Project): List<Label>

}