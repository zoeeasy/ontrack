package net.nemerosa.ontrack.extension.neo4j

data class Neo4JExportOutput(
        val uuid: String,
        val nodeFiles: List<String>,
        val relFiles: List<String>
)
