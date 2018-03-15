package net.nemerosa.ontrack.extension.neo4j.model

data class Neo4JExportRecordExtractor<T>(
        // FIXME Async stream
        val collectionSupplier: () -> List<T>,
        val recordDefinitions: List<Neo4JExportRecordDef<T>>
)
