package net.nemerosa.ontrack.extension.neo4j.model

class Neo4JExportRecordDef<T>(
        val type: Neo4JExportRecordType,
        val name: String,
        val columns: List<Neo4JExportColumn<T>>
)
