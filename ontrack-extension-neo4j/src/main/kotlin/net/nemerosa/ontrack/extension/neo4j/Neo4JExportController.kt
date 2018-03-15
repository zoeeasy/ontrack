package net.nemerosa.ontrack.extension.neo4j

import org.springframework.web.bind.annotation.*
import java.util.concurrent.Callable
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
    fun launchExport(@RequestBody input: Neo4JExportInput): Callable<Neo4JExportOutput> =
            Callable { neo4JExportService.export(input) }


    /**
     * Download
     */
    @GetMapping("{uuid}")
    fun download(@PathVariable uuid: String, response: HttpServletResponse) {
        val document = neo4JExportService.download(uuid)
        response.contentType = document.type
        val bytes = document.content
        response.setContentLength(bytes.size)
        response.setHeader("Content-Disposition", "attachment; filename=neo4j.zip")
        response.outputStream.write(bytes)
        response.outputStream.flush()
    }

}