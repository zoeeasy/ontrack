package net.nemerosa.ontrack.extension.neo4j.model

data class Neo4JExportColumn<in T>(
        val header: String,
        val valueFn: (T) -> Any
)
