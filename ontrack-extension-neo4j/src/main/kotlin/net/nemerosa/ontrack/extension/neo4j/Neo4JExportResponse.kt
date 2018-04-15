package net.nemerosa.ontrack.extension.neo4j

/**
 * Result of an export session.
 *
 * @property uuid UUID of the export, used to download the associated files
 * @property stats Number of objects having been exported
 */
data class Neo4JExportResponse(
        val uuid: String,
        val stats: Map<String, Int>
)
