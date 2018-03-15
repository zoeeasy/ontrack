package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportModule
import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportRecordDef
import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportRecordExtractor
import net.nemerosa.ontrack.job.JobRunListener
import net.nemerosa.ontrack.model.security.ApplicationManagement
import net.nemerosa.ontrack.model.security.SecurityService
import net.nemerosa.ontrack.model.security.callAsAdmin
import net.nemerosa.ontrack.model.structure.NameDescription
import net.nemerosa.ontrack.model.support.ApplicationLogEntry
import net.nemerosa.ontrack.model.support.ApplicationLogService
import net.nemerosa.ontrack.model.support.EnvService
import org.apache.commons.io.FileUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicReference


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

    override fun export(listener: JobRunListener) {

        // Checks authorizations
        securityService.checkGlobalFunction(ApplicationManagement::class.java)

        // Cleanup of previous context
        val exportContext = currentExportContext.updateAndGet { ctx ->
            closeContext(ctx)
            // New context
            val uuid = UUID.randomUUID().toString()
            createExportContext(uuid)
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

        // Launching the export
        securityService.asAdmin { export(exportContext, recordDefinitions) }

    }

    private fun export(exportContext: Neo4JExportContext, recordExtractors: List<Neo4JExportRecordExtractor<*>>) {
        recordExtractors.forEach { recordExtractor -> export(exportContext, recordExtractor) }
    }

    private fun <T> export(exportContext: Neo4JExportContext, recordExtractor: Neo4JExportRecordExtractor<T>) {
        // Gets the list of items
        recordExtractor.collectionSupplier().forEach { o -> export(exportContext, recordExtractor, o) }
    }

    private fun <T> export(exportContext: Neo4JExportContext, recordExtractor: Neo4JExportRecordExtractor<T>, o: T) {
        recordExtractor.recordDefinitions.forEach { recordDef -> export(exportContext, recordDef, o) }
    }

    private fun <T> export(exportContext: Neo4JExportContext, recordExtractor: Neo4JExportRecordDef<T>, o: T) {
        exportContext.writeRow(
                recordExtractor.name,
                recordExtractor.columns
                        .map { (_, valueFn) -> valueFn(o) }
        )
    }

    private fun closeContext(ctx: Neo4JExportContext?): Neo4JExportContext? {
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
        return null
    }

    private fun createExportContext(id: String): Neo4JExportContext {
        val dir = getContextWorkingDir(id)
        return Neo4JExportContext(id, dir)
    }

    private fun getContextWorkingDir(id: String): File =
            envService.getWorkingDir(configProperties.exportDownloadPath, id)

}