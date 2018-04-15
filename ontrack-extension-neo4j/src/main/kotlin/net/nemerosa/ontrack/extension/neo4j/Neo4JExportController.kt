package net.nemerosa.ontrack.extension.neo4j

import org.springframework.web.bind.annotation.*
import java.util.concurrent.CompletionStage
import javax.servlet.http.HttpServletResponse

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

    /**
     * Downloading
     */
    @GetMapping("/{uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}")
    fun download(@PathVariable uuid: String, response: HttpServletResponse) {
        response.contentType = "application/zip"
        response.addHeader("Content-Disposition", "attachment;filename=$uuid.zip")
        neo4JExportService.download(uuid, response.outputStream)
    }

}