package net.nemerosa.ontrack.extension.neo4j.model

data class Neo4JExportRecordExtractor<T>(
        val collectionSupplier: () -> Sequence<T>,
        val recordDefinitions: List<Neo4JExportRecordDef<T>>
)
