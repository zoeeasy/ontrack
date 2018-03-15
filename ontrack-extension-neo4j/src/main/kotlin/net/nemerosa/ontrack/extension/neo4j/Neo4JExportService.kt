package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.common.Document

interface Neo4JExportService {
    fun export(input: Neo4JExportInput): Neo4JExportOutput
    fun download(uuid: String): Document
}