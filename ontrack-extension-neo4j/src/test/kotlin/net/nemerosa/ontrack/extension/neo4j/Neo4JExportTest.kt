package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.it.AbstractDSLTestSupport
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Integration test about the export to Neo4J.
 */
class Neo4JExportTest : AbstractDSLTestSupport() {

    @Autowired
    private lateinit var neo4JExportService: Neo4JExportService

    /**
     * Removing all data from Ontrack in order to start with a clean
     * and controllable slate.
     */
    @Before
    fun cleanup() {
        asAdmin().execute {
            structureService.projectList.forEach {
                structureService.deleteProject(it.id)
            }
        }
    }

    @Test(expected = Neo4JExportNoDownloadException::class)
    fun `Downloading without any export`() {
        val file = File.createTempFile("test", ".zip")
        // Attempting to download anything
        file.outputStream().use {
            asAdmin().call {
                neo4JExportService.download("Totally unknown UUID", it)
            }
        }
    }

    @Test(expected = Neo4JExportDownloadNotFoundException::class)
    fun `Downloading an unknown UUID`() {
        // Creates projects, branches, etc.
        tx {
            project {
                branch("master") {}
            }
        }
        // Launching the export and gets the answer
        asAdmin().call {
            neo4JExportService.export(Neo4JExportRequest())
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS)
        }
        // Attempting to download another UUID
        val file = File.createTempFile("test", ".zip")
        file.outputStream().use {
            asAdmin().call {
                neo4JExportService.download("Totally unknown UUID", it)
            }
        }
    }

    @Test
    fun `Cannot download twice`() {
        // Creates projects, branches, etc.
        tx {
            project {
                branch("master") {}
            }
        }
        // Launching the export and gets the answer
        val response = asAdmin().call {
            neo4JExportService.export(Neo4JExportRequest())
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS)
        }
        // Downloads the result and unzips them on the go
        val file = File.createTempFile("test", ".zip")
        file.outputStream().use {
            asAdmin().call {
                neo4JExportService.download(response.uuid, it)
            }
        }
        assertTrue(file.exists())
        assertTrue(file.length() > 0)
        // Downloads a second time
        assertFailsWith(Neo4JExportNoDownloadException::class) {
            file.outputStream().use {
                asAdmin().call {
                    neo4JExportService.download(response.uuid, it)
                }
            }
        }
    }

    @Test
    fun `Exporting and downloading`() {
        // Creates projects, branches, etc.
        tx {
            project {
                branch("master") {}
                branch("release/2.0") {}
                branch("hotfix/456-aie") {}
            }
            project {
                branch("release/1.0") {}
                branch("feature/123-great") {}
            }
        }
        // Launching the export and gets the answer
        val response = asAdmin().call {
            neo4JExportService.export(Neo4JExportRequest())
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS)
        }
        // Summary
        response.stats.forEach { name, count ->
            println("$name = $count")
        }
        response.apply {
            assertEquals(2, stats["node/Project"])
            assertEquals(5, stats["node/Branch"])
            assertEquals(5, stats["rel/BRANCH_OF"])
        }
        // Downloads and checks the results
        download(response.uuid) { dir ->
            csvTest(dir, "node/Project.csv") { _, record ->
                val id: String? = record.get(":ID")
                assertEquals("....1", id)
            }
        }
    }

    private fun csvTest(dir: File, path: String, max: Int = 1, code: (Int, CSVRecord) -> Unit) {
        val csvFile = File(dir, path)
        assertTrue(csvFile.exists(), "$path file does exist.")
        csvFile.reader().use { reader ->
            val csvParser = CSVFormat.RFC4180
                    .withFirstRecordAsHeader()
                    .parse(reader)
                    .iterator()
            var line = 0
            while (line < max && csvParser.hasNext()) {
                line++
                val csvRecord: CSVRecord = csvParser.next()
                code(line, csvRecord)
            }
        }
    }

    private fun download(uuid: String, code: (File) -> Unit) {
        val zip = File.createTempFile("test", ".zip")
        try {
            zip.outputStream().use {
                asAdmin().call {
                    neo4JExportService.download(uuid, it)
                }
            }
            assertTrue(zip.exists())
            assertTrue(zip.length() > 0)
            // Unzips the file in a directory
            val dir = File(zip.parentFile, zip.nameWithoutExtension)
            FileUtils.forceMkdir(dir)
            try {
                val zipFile = ZipFile(zip)
                val zipEntries = zipFile.entries()
                while (zipEntries.hasMoreElements()) {
                    val zipEntry = zipEntries.nextElement()
                    zipFile.getInputStream(zipEntry).use { zin ->
                        val targetFile = File(dir, zipEntry.name)
                        FileUtils.copyInputStreamToFile(zin, targetFile)
                    }
                }
                // Tests
                code(dir)
            } finally {
                FileUtils.deleteDirectory(dir)
            }
        } finally {
            FileUtils.deleteQuietly(zip)
        }
    }

}
