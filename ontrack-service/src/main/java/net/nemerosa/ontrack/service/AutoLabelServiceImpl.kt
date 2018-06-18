package net.nemerosa.ontrack.service

import net.nemerosa.ontrack.model.structure.*
import net.nemerosa.ontrack.repository.jpa.LabelRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class AutoLabelServiceImpl(
        private val autoLabelProviders: List<AutoLabelProvider>,
        private val labelRepository: LabelRepository
) : AutoLabelService {

    override fun getLabelsForProject(project: Project): List<Label> =
            autoLabelProviders
                    .mapNotNull { it.getProjectLabel(project) }
                    .map { toLabel(it) }

    private fun toLabel(autoLabelDescription: AutoLabelDescription): Label {
        // Gets any existing label
        return labelRepository
                .findLabelByCategoryAndNameAndComputed(
                        category = autoLabelDescription.category,
                        name = autoLabelDescription.name,
                        computed = true
                ) ?: createLabel(autoLabelDescription)
    }

    private fun createLabel(autoLabelDescription: AutoLabelDescription): Label =
            labelRepository.save(
                    Label(
                            id = 0,
                            category = autoLabelDescription.category,
                            name = autoLabelDescription.name,
                            description = autoLabelDescription.description,
                            // FIXME #615 Generates a colour
                            colour = "green",
                            computed = true
                    )
            )
}

@Component
class NOPAutoLabelProvider : AutoLabelProvider {
    override fun getProjectLabel(project: Project): AutoLabelDescription? = null
}
