package net.nemerosa.ontrack.service

import net.nemerosa.ontrack.it.AbstractDSLTestSupport
import net.nemerosa.ontrack.model.structure.AutoLabelDescription
import net.nemerosa.ontrack.model.structure.AutoLabelProvider
import net.nemerosa.ontrack.model.structure.AutoLabelService
import net.nemerosa.ontrack.model.structure.Project
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AutoLabelServiceIT : AbstractDSLTestSupport() {

    @Autowired
    private lateinit var autoLabelService: AutoLabelService

    @Configuration
    class AutoLabelServiceITConfig {
        @Bean
        fun firstLetterProjectLabel() = object : AutoLabelProvider {
            override fun getProjectLabel(project: Project): AutoLabelDescription? {
                val letter = project.name[0].toString()
                return AutoLabelDescription(
                        "Letter",
                        letter,
                        "First letter: $letter"
                )
            }

        }
    }

    @Test
    fun `Creating a label for a project`() {
        project {
            val labels = autoLabelService.getLabelsForProject(this)
            val label = labels.find { it.category == "Letter" }
            assertNotNull(label, "Letter label created") {
                assertNotNull(it.colour, "Colour set")
                assertEquals("Letter", it.category)
                assertEquals(this.name[0].toString(), it.name)
                assertTrue(it.computed, "Computed label")
            }
        }
    }

}