package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportModule
import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportRecordDef
import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportRecordExtractor
import net.nemerosa.ontrack.model.security.ApplicationManagement
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.security.callAsAdmin
import net.nemerosa.ontrack.model.structure.NameDescription
import net.nemerosa.ontrack.model.support.ApplicationLogEntry
import net.nemerosa.ontrack.model.support.ApplicationLogService
import net.nemerosa.ontrack.model.support.EnvService
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Service
@Transactional
class Neo4JExportServiceImpl(
        private val configProperties: Neo4JConfigProperties,
        private val exportModules: Collection<Neo4JExportModule>,
        private val securityService: SecurityService,
        private val envService: EnvService,
        private val applicationLogService: ApplicationLogService
) : Neo4JExportService {

    /**
     * Download context
     */
    private val currentExportContext = AtomicReference<Neo4JExportContext>()

    override fun download(uuid: String, stream: OutputStream) {
        // Gets the current context
        val exportContext: Neo4JExportContext? = currentExportContext.getAndSet(null)
        // If present
        if (exportContext != null) {
            // Checks the UUID
            if (uuid != exportContext.uuid) {
                throw Neo4JExportDownloadNotFoundException(uuid)
            }
            // Checks the context state
            if (exportContext.state != Neo4JExportContextState.READY) {
                throw Neo4JExportDownloadNotReadyException(uuid)
            }
            // Closes the context when done
            exportContext.close()
            // Gets the list of paths
            val paths = exportContext.paths
            // Zips the directory
            ZipOutputStream(stream).use { zout ->
                paths.forEach { path ->
                    zout.putNextEntry(ZipEntry(path))
                    exportContext.open(path).use { input ->
                        IOUtils.copy(input, zout)
                    }
                }
            }
            // Clean up
            closeContext(exportContext)
        } else {
            throw Neo4JExportNoDownloadException()
        }
    }

    override fun export(request: Neo4JExportRequest): CompletionStage<Neo4JExportResponse> {

        // Checks authorizations
        securityService.checkGlobalFunction(ApplicationManagement::class.java)

        /*
         * If the current context is present and if it is not ready for download,
         * the new export request is rejected.
         *
         * If it is present, and if it ready for download,
         * the previous request is cancelled and a new one is created.
         */

        // Cleanup of previous context
        val exportContext = currentExportContext.updateAndGet { ctx ->
            if (ctx != null && ctx.state != Neo4JExportContextState.READY) {
                throw Neo4JExportAlreadyRunningException()
            } else {
                closeContext(ctx)
                // New context
                val uuid = UUID.randomUUID().toString()
                createExportContext(uuid)
            }
        }

        // Initialisation of the context
        exportModules
                .flatMap { exportModule -> exportModule.recordExtractors }
                .flatMap { recordExtractor -> recordExtractor.recordDefinitions }
                .forEach { exportContext.init(it) }

        // Collecting all record extractors
        val recordDefinitions = securityService.callAsAdmin {
            exportModules.flatMap { exportModule -> exportModule.recordExtractors }
        }

        // Launching the export in a separate thread, run by the admin user
        return CompletableFuture
                .runAsync { export(exportContext, recordDefinitions) }
                .thenApply {
                    Neo4JExportResponse(
                            exportContext.uuid,
                            exportContext.stats
                    )
                }

    }

    private fun export(exportContext: Neo4JExportContext, recordExtractors: List<Neo4JExportRecordExtractor<*>>) {
        exportContext.start()
        recordExtractors.forEach { recordExtractor -> export(exportContext, recordExtractor) }
        exportContext.ready()
    }

    /**
     * Gets the list of items defined by the [recordExtractor] and exports each of them
     * using the list of [record definitions][Neo4JExportRecordExtractor.recordDefinitions].
     */
    private fun <T> export(exportContext: Neo4JExportContext, recordExtractor: Neo4JExportRecordExtractor<T>) {
        // Gets the list of items
        val items = securityService.callAsAdmin {
            recordExtractor.collectionSupplier()
        }
        // Exports each items
        items.forEach { o -> export(exportContext, recordExtractor, o) }
    }

    /**
     * Exports an [item][o] using a given [extractor][recordExtractor].
     */
    private fun <T> export(exportContext: Neo4JExportContext, recordExtractor: Neo4JExportRecordExtractor<T>, o: T) {
        recordExtractor.recordDefinitions.forEach { recordDef -> export(exportContext, recordDef, o) }
    }

    /**
     * Exports a given [item][o] using a [record definition][recordDef].
     */
    private fun <T> export(exportContext: Neo4JExportContext, recordDef: Neo4JExportRecordDef<T>, o: T) {
        exportContext.writeRow(
                recordDef.name,
                recordDef.columns
                        .map { (_, valueFn) -> valueFn(o) }
        )
        // Upgrading the stats
        exportContext.recordStat(recordDef.name, recordDef.type)
    }

    private fun closeContext(ctx: Neo4JExportContext?) {
        if (ctx != null) {
            ctx.close()
            val contextWorkingDir = getContextWorkingDir(ctx.uuid)
            try {
                FileUtils.forceDelete(contextWorkingDir)
            } catch (e: IOException) {
                applicationLogService.log(
                        ApplicationLogEntry.error(
                                e,
                                NameDescription.nd("Neo4J Export Cleanup", "Cannot delete Neo4J export working directory"),
                                ""
                        )
                                .withDetail("neo4j.export.uuid", ctx.uuid)
                                .withDetail("neo4j.export.dir", contextWorkingDir.absolutePath)
                )
            }
        }
    }

    private fun createExportContext(id: String): Neo4JExportContext {
        val dir = getContextWorkingDir(id)
        return Neo4JExportContext(id, dir)
    }

    private fun getContextWorkingDir(id: String): File =
            envService.getWorkingDir(configProperties.exportDownloadPath, id)

}