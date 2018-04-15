package net.nemerosa.ontrack.extension.neo4j

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletionStage

@RestController
@RequestMapping("/extension/neo4j/export")
class Neo4JExportController(
        private val neo4JExportService: Neo4JExportService
) {

    /**
     * Launching the export in async mode
     */
    @PostMapping
    fun launchExport(): CompletionStage<Neo4JExportResponse> =
            neo4JExportService.export(Neo4JExportRequest())


}