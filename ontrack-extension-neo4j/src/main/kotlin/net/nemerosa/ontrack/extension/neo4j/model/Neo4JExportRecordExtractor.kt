package net.nemerosa.ontrack.extension.neo4j.model

import java.util.stream.Stream

data class Neo4JExportRecordExtractor<T>(
        val collectionSupplier: () -> Stream<T>,
        val recordDefinitions: List<Neo4JExportRecordDef<T>>
)
