package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.job.JobScheduler
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/extension/neo4j/export")
class Neo4JExportController(
        private val jobScheduler: JobScheduler
) {

    /**
     * Launching the export in async mode
     */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun launchExport() {
        jobScheduler.fireImmediately(Neo4JJob.NEO4J_EXPORT_JOB)
    }


}