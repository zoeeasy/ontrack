package net.nemerosa.ontrack.extension.neo4j

import java.util.concurrent.CompletionStage

interface Neo4JExportService {
    /**
     * Launches an export session
     *
     * @param request Export request
     * @return Future containing the export summary
     */
    fun export(request: Neo4JExportRequest): CompletionStage<Neo4JExportResponse>
}
