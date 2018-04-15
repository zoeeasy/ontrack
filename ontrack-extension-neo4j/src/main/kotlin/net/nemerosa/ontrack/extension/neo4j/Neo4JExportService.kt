package net.nemerosa.ontrack.extension.neo4j

import java.io.OutputStream
import java.util.concurrent.CompletionStage

interface Neo4JExportService {
    /**
     * Launches an export session
     *
     * @param request Export request
     * @return Future containing the export summary
     */
    fun export(request: Neo4JExportRequest): CompletionStage<Neo4JExportResponse>
    /**
     * Downloads the result of the export into a stream. The export is performed
     * as a ZIP file.
     */
    fun download(uuid: String, stream: OutputStream)
}
