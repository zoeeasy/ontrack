package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.job.JobRunListener

interface Neo4JExportService {
    fun export(listener: JobRunListener)
}