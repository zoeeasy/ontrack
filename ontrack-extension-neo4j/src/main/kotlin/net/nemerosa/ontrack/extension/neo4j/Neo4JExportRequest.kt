package net.nemerosa.ontrack.extension.neo4j

import java.time.LocalDateTime

/**
 * @property timestamp Time to start the export from (if not set, exports everything)
 */
data class Neo4JExportRequest(
        val timestamp: LocalDateTime? = null
)
