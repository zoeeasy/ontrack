package net.nemerosa.ontrack.extension.stash

import net.nemerosa.ontrack.extension.stash.property.StashProjectConfigurationPropertyType
import net.nemerosa.ontrack.model.structure.AutoLabelDescription
import net.nemerosa.ontrack.model.structure.AutoLabelProvider
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.model.structure.PropertyService
import org.springframework.stereotype.Component

@Component
class BitbucketProjectAutoLabelProvider(
        private val propertyService: PropertyService
) : AutoLabelProvider {

    override fun getProjectLabel(project: Project): AutoLabelDescription? =
            propertyService.getProperty(project, StashProjectConfigurationPropertyType::class.java)
                    .value
                    ?.run {
                        AutoLabelDescription(
                                "Bitbucket project",
                                this.project,
                                "Bitbucket project: ${this.project}"
                        )
                    }

}