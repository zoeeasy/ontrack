package net.nemerosa.ontrack.service

import net.nemerosa.ontrack.model.structure.*
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class AutoLabelServiceImpl(
        private val autoLabelProviders: List<AutoLabelProvider>
) : AutoLabelService {

    override fun getLabelsForProject(project: Project): List<Label> =
            autoLabelProviders
                    .mapNotNull { it.getProjectLabel(project) }
                    .map { toLabel(it) }

    private fun toLabel(autoLabelDescription: AutoLabelDescription): Label {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

@Component
class NOPAutoLabelProvider : AutoLabelProvider {
    override fun getProjectLabel(project: Project): AutoLabelDescription? = null
}
