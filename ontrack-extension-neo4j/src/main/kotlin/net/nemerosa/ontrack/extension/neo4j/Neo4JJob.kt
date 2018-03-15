package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.job.*
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.support.JobProvider
import org.springframework.stereotype.Component

/**
 * Export job provider
 */
@Component
class Neo4JJob(
        private val securityService: SecurityService,
        private val neo4JExportService: Neo4JExportService
) : JobProvider {

    companion object {
        val NEO4J_EXPORT_JOB = JobCategory.of("neo4j").withName("Neo4J")
                .getType("export").withName("Neo4J Export")
                .getKey("main")
    }

    override fun getStartingJobs() =
            listOf(
                    // Not scheduled by default
                    JobRegistration.of(createExportJob()).withSchedule(Schedule.NONE)
            )

    private fun createExportJob() = object : Job {

        override fun getKey() = NEO4J_EXPORT_JOB

        override fun getDescription() = "Neo4J Export job"

        override fun isDisabled() = false

        override fun getTask() = JobRun {
            securityService.asAdmin {
                neo4JExportService.export(it)
            }
        }

    }

}