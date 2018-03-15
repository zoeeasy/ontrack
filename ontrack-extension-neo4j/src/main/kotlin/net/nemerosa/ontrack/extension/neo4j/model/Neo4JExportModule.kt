package net.nemerosa.ontrack.extension.neo4j.model

interface Neo4JExportModule {

    val recordExtractors: List<Neo4JExportRecordExtractor<*>>

}
