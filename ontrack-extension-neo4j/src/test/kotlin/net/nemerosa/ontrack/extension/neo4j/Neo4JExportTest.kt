package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.it.AbstractDSLTestSupport
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

/**
 * Integration test about the export to Neo4J.
 */
class Neo4JExportTest : AbstractDSLTestSupport() {

    @Autowired
    private lateinit var neo4JExportService: Neo4JExportService

    @Autowired
    private lateinit var platformTransactionManager: PlatformTransactionManager

    private lateinit var transactionTemplate: TransactionTemplate

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
        transactionTemplate = TransactionTemplate(platformTransactionManager)
    }

    @Test
    fun export() {
        // Creates projects, branches, etc.
        transactionTemplate.execute {
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
                    .get(10, TimeUnit.MINUTES)
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
    }

}
