package net.nemerosa.ontrack.migration.postgresql

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import java.lang.String.format
import java.sql.BatchUpdateException
import java.util.*
import javax.sql.DataSource

@Component
class Migration(
        @Qualifier("h2")
        h2Datasource: DataSource,
        @Qualifier("postgresql")
        postgresqlDatasource: DataSource,
        private val migrationProperties: MigrationProperties
) {

    private val logger = LoggerFactory.getLogger(Migration::class.java)
    private val h2: NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(h2Datasource)
    private val postgresql: NamedParameterJdbcTemplate = NamedParameterJdbcTemplate(postgresqlDatasource)
    private val txTemplate: TransactionTemplate = TransactionTemplate(DataSourceTransactionManager(postgresqlDatasource))

    fun run() {
        // Cleanup?
        if (migrationProperties.isCleanup) {
            cleanup()
        }

        /*
         * Global data
         */

        // CONFIGURATIONS
        copy("CONFIGURATIONS", "ID", "TYPE", "NAME", "CONTENT::JSONB")

        // SETTINGS
        copy("SETTINGS", "CATEGORY", "NAME", "VALUE")

        // PREDEFINED_PROMOTION_LEVELS
        copy("PREDEFINED_PROMOTION_LEVELS", "ID", "ORDERNB", "NAME", "DESCRIPTION", "IMAGETYPE", "IMAGEBYTES")

        // PREDEFINED_VALIDATION_STAMPS
        copy("PREDEFINED_VALIDATION_STAMPS", "ID", "NAME", "DESCRIPTION", "IMAGETYPE", "IMAGEBYTES")

        // STORAGE
        copy("STORAGE", "STORE", "NAME", "DATA::JSONB")

        // APPLICATION_LOG_ENTRIES
        // No migration of the log entries

        /*
         * Entities
         */

        // PROJECTS
        copy("PROJECTS", "ID", "NAME", "DESCRIPTION", "DISABLED", "CREATION", "CREATOR")

        // BRANCHES
        copy("BRANCHES", "ID", "PROJECTID", "NAME", "DESCRIPTION", "DISABLED", "CREATION", "CREATOR")

        // PROMOTION_LEVELS
        copy("PROMOTION_LEVELS", "ID", "BRANCHID", "ORDERNB", "NAME", "DESCRIPTION", "IMAGETYPE", "IMAGEBYTES", "CREATION", "CREATOR")

        // VALIDATION_STAMPS
        copy("VALIDATION_STAMPS", "ID", "BRANCHID", "OWNER", "PROMOTION_LEVEL", "ORDERNB", "NAME", "DESCRIPTION", "IMAGETYPE", "IMAGEBYTES", "CREATION", "CREATOR")

        // BUILDS
        copy("BUILDS", "ID", "BRANCHID", "NAME", "DESCRIPTION", "CREATION", "CREATOR")

        // PROMOTION_RUNS
        copy("PROMOTION_RUNS", "ID", "BUILDID", "PROMOTIONLEVELID", "CREATION", "CREATOR", "DESCRIPTION")

        // VALIDATION_RUNS
        copy("VALIDATION_RUNS", "ID", "BUILDID", "VALIDATIONSTAMPID")

        // VALIDATION_RUN_STATUSES
        copy("VALIDATION_RUN_STATUSES", "ID", "VALIDATIONRUNID", "VALIDATIONRUNSTATUSID", "CREATION", "CREATOR", "DESCRIPTION")

        /*
         * Branch templating
         */

        // BRANCH_TEMPLATE_DEFINITIONS
        copy("BRANCH_TEMPLATE_DEFINITIONS", "BRANCHID", "ABSENCEPOLICY", "SYNCINTERVAL", "SYNCHRONISATIONSOURCEID", "SYNCHRONISATIONSOURCECONFIG::JSONB")

        // BRANCH_TEMPLATE_DEFINITION_PARAMS
        copy("BRANCH_TEMPLATE_DEFINITION_PARAMS", "BRANCHID", "NAME", "DESCRIPTION", "EXPRESSION")

        // BRANCH_TEMPLATE_INSTANCES
        copy("BRANCH_TEMPLATE_INSTANCES", "BRANCHID", "TEMPLATEBRANCHID")

        // BRANCH_TEMPLATE_INSTANCE_PARAMS
        copy("BRANCH_TEMPLATE_INSTANCE_PARAMS", "BRANCHID", "NAME", "VALUE")

        /*
         * Entity data
         */

        // FIXME ENTITY_DATA
        // copy("ENTITY_DATA", "ID", "PROJECT", "BRANCH", "PROMOTION_LEVEL", "VALIDATION_STAMP", "BUILD", "PROMOTION_RUN", "VALIDATION_RUN", "NAME", "JSON_VALUE::JSONB");

        // ENTITY_DATA_STORE
        copy("ENTITY_DATA_STORE", "ID", "PROJECT", "BRANCH", "PROMOTION_LEVEL", "VALIDATION_STAMP", "BUILD", "PROMOTION_RUN", "VALIDATION_RUN", "CREATION", "CREATOR", "CATEGORY", "NAME", "GROUPID", "JSON::JSONB")

        // FIXME ENTITY_DATA_STORE
        // copy("ENTITY_DATA_STORE_AUDIT", "ID", "RECORD_ID", "AUDIT_TYPE", "TIMESTAMP", "USER->CREATOR");

        // PROPERTIES
        copyProperties()

        // SHARED_BUILD_FILTERS
        copy("SHARED_BUILD_FILTERS", "BRANCHID", "NAME", "TYPE", "DATA::JSONB")

        // VALIDATION_STAMP_FILTERS
        copy("VALIDATION_STAMP_FILTERS", "ID", "NAME", "PROJECT", "BRANCH", "VSNAMES")

        // BUILD_LINKS
        copy("BUILD_LINKS", "ID", "BUILDID", "TARGETBUILDID")

        // EVENTS
        if (migrationProperties.isSkipEvents) {
            logger.warn("Skipping events migration")
        } else {
            copyEvents()
        }

        /*
         * ACL
         */

        // ACCOUNTS
        copy("ACCOUNTS", "ID", "NAME", "FULLNAME", "EMAIL", "MODE", "PASSWORD", "ROLE")

        // ACCOUNT_GROUPS
        copy("ACCOUNT_GROUPS", "ID", "NAME", "DESCRIPTION")

        // ACCOUNT_GROUP_LINK
        copy("ACCOUNT_GROUP_LINK", "ACCOUNT", "ACCOUNTGROUP")

        // ACCOUNT_GROUP_MAPPING
        copy("ACCOUNT_GROUP_MAPPING", "ID", "GROUPID", "MAPPING", "SOURCE")

        // GLOBAL_AUTHORIZATIONS
        copy("GLOBAL_AUTHORIZATIONS", "ACCOUNT", "ROLE")

        // GROUP_GLOBAL_AUTHORIZATIONS
        copy("GROUP_GLOBAL_AUTHORIZATIONS", "ACCOUNTGROUP", "ROLE")

        // GROUP_PROJECT_AUTHORIZATIONS
        copy("GROUP_PROJECT_AUTHORIZATIONS", "ACCOUNTGROUP", "PROJECT", "ROLE")

        // PROJECT_AUTHORIZATIONS
        copy("PROJECT_AUTHORIZATIONS", "ACCOUNT", "PROJECT", "ROLE")

        // PREFERENCES
        copy("PREFERENCES", "ACCOUNTID", "TYPE", "CONTENT")

        // BUILD_FILTERS
        copy("BUILD_FILTERS", "ACCOUNTID", "BRANCHID", "NAME", "TYPE", "DATA::JSONB")

        // PROJECT_FAVOURITES
        copy("PROJECT_FAVOURITES", "ID", "ACCOUNTID", "PROJECTID")

        // Subversion
        // Subversion tables do not need to be migrated - they will be filled on demand

        // Update of sequences
        updateSequences()

    }

    private fun copyProperties() {
        copyWithTmp(
                "PROPERTIES",
                "CREATE TABLE TMP_PROPERTIES " +
                        "( " +
                        "  ID INTEGER PRIMARY KEY, " +
                        "  TYPE CHARACTER VARYING(150), " +
                        "  PROJECT INTEGER, " +
                        "  BRANCH INTEGER, " +
                        "  PROMOTION_LEVEL INTEGER, " +
                        "  VALIDATION_STAMP INTEGER, " +
                        "  BUILD INTEGER, " +
                        "  PROMOTION_RUN INTEGER, " +
                        "  VALIDATION_RUN INTEGER, " +
                        "  SEARCHKEY CHARACTER VARYING(200), " +
                        "  JSON JSONB " +
                        ");",
                "ID", "PROJECT", "BRANCH", "PROMOTION_LEVEL", "VALIDATION_STAMP", "BUILD", "PROMOTION_RUN", "VALIDATION_RUN", "TYPE", "SEARCHKEY", "JSON::JSONB"
        )
    }

    private fun copyEvents() {
        copyWithTmp(
                "EVENTS",
                "CREATE TABLE TMP_EVENTS " +
                        "( " +
                        "  ID INTEGER PRIMARY KEY, " +
                        "  EVENT_TYPE CHARACTER VARYING(120), " +
                        "  PROJECT INTEGER, " +
                        "  BRANCH INTEGER, " +
                        "  PROMOTION_LEVEL INTEGER, " +
                        "  VALIDATION_STAMP INTEGER, " +
                        "  BUILD INTEGER, " +
                        "  PROMOTION_RUN INTEGER, " +
                        "  VALIDATION_RUN INTEGER, " +
                        "  REF CHARACTER VARYING(20), " +
                        "  EVENT_VALUES CHARACTER VARYING(500), " +
                        "  EVENT_TIME CHARACTER VARYING(24), " +
                        "  EVENT_USER CHARACTER VARYING(40) " +
                        ");",
                "ID", "PROJECT", "BRANCH", "PROMOTION_LEVEL", "VALIDATION_STAMP", "BUILD", "PROMOTION_RUN", "VALIDATION_RUN", "EVENT_TYPE", "REF", "EVENT_VALUES", "EVENT_TIME", "EVENT_USER"
        )
    }

    private fun copyWithTmp(table: String, tmpCreation: String, vararg columns: String) {
        val h2Query = format("SELECT * FROM %s", table)

        val insert = columns.joinToString(",") { it.substringBefore("::") }
        val postgresqlUpdate = format(
                "INSERT INTO TMP_%s (%s) VALUES (%s)",
                table,
                insert,
                columns.joinToString(",") { column ->
                    "?" + if (column.contains("::")) {
                        "::" + column.substringAfter("::")
                    } else {
                        ""
                    }
                }
        )

        tx({
            // Makes sure temp table is dropped
            postgresql.jdbcOperations.execute(format("DROP TABLE IF EXISTS TMP_%s", table))
            // Creates temporary table, without any contraint
            logger.info(format("Creating TMP_%s...", table))
            postgresql.jdbcOperations.execute(tmpCreation)
        })

        val count = intx<Int>({
            logger.info("Migrating {} to TMP_{} (no check)...", table, table)
            val sources = h2.queryForList(h2Query, emptyMap<String, Any>())
            val size = sources.size

            postgresql.jdbcOperations.execute(
                    postgresqlUpdate,
                    { ps ->
                        var index = 0
                        var tosend = 0
                        for (source in sources) {
                            index++
                            var i = 1
                            for (column in columns) {
                                ps.setObject(i++, source[StringUtils.substringBefore(column, "::")])
                            }
                            ps.addBatch()
                            tosend++
                            if (tosend % 1000 == 0) {
                                val percent = (index * 100 / size).toLong()
                                logger.info("Migrating {} to TMP_{} (no check) {}/{} [{}%]", table, table, index, size, percent)
                                ps.executeBatch()
                                tosend = 0
                            }
                        }
                        // Final statement
                        if (tosend > 0) {
                            val percent = (index * 100 / size).toLong()
                            logger.info("Migrating {} to TMP_{} (no check) {}/{} [{}%]", table, table, index, size, percent)
                            ps.executeBatch()
                        }
                        null
                    }
            )
            size
        })

        tx({
            // Copying the tmp
            logger.info("Copying TMP_{} into {}...", table, table)
            postgresql.jdbcOperations.execute(format("INSERT INTO %s SELECT * FROM TMP_%s", table, table))

            // Deletes tmp table
            logger.info("Deleting TMP_{}...", table)
            postgresql.jdbcOperations.execute(format("DROP TABLE TMP_%s;", table))
        })

        // OK
        logger.info("{} count = {}...", table, count)
    }

    private fun updateSequences() {
        logger.info("Resetting the sequences...")
        val tables = arrayOf("ACCOUNT_GROUP_MAPPING", "ACCOUNT_GROUPS", "ACCOUNTS", "BRANCHES", "BUILDS", "CONFIGURATIONS", "ENTITY_DATA", "ENTITY_DATA_STORE", "ENTITY_DATA_STORE_AUDIT", "EVENTS", "EXT_SVN_REPOSITORY", "PREDEFINED_PROMOTION_LEVELS", "PREDEFINED_VALIDATION_STAMPS", "PROJECTS", "PROMOTION_LEVELS", "PROMOTION_RUNS", "PROPERTIES", "BUILD_LINKS", "VALIDATION_RUN_STATUSES", "VALIDATION_RUNS", "VALIDATION_STAMPS", "VALIDATION_STAMP_FILTERS")
        tx({
            Arrays.stream(tables).forEach { table ->
                val max = postgresql.queryForObject(
                        format("SELECT MAX(ID) AS ID FROM %s", table),
                        emptyMap<String, Any>(),
                        Int::class.java
                )
                val value = if (max != null) max + 1 else 1
                postgresql.update(
                        format("ALTER SEQUENCE %s_ID_SEQ RESTART WITH %d", table, value),
                        emptyMap<String, Any>()
                )
                logger.info("Resetting sequence for {} to {}.", table, value)
            }
        })
    }

    private fun cleanup() {
        logger.info("Cleanup of target database...")
        val tables = arrayOf("ACCOUNTS", "ACCOUNT_GROUPS", "CONFIGURATIONS", "EXT_SVN_REPOSITORY", "PREDEFINED_PROMOTION_LEVELS", "PREDEFINED_VALIDATION_STAMPS", "PROJECTS", "EVENTS", "SETTINGS", "STORAGE", "VALIDATION_STAMP_FILTERS")
        tx {
            for (table in tables) {
                postgresql.update(format("DELETE FROM %s", table), emptyMap<String, Any>())
            }
        }
    }

    private fun tx(task: () -> Unit) {
        txTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(status: TransactionStatus) {
                try {
                    task()
                } catch (ex: Exception) {
                    val message = ex.message
                    if (StringUtils.contains(message, "Block not found") && migrationProperties.isSkipBlobErrors) {
                        logger.warn("Ignoring BLOB read error: {}", message)
                    } else {
                        throw ex
                    }
                }

            }
        })
    }

    private fun <T> intx(supplier: () -> T): T {
        return txTemplate.execute { _ -> supplier() }
    }

    private fun copy(table: String, vararg columns: String) {
        val h2Query = format("SELECT * FROM %s", table)

        val insert = columns.joinToString(",") { it.substringBefore("::") }
        val values = columns.joinToString(",") { column -> ":" + column }
        val postgresqlUpdate = format("INSERT INTO %s (%s) VALUES (%s)", table, insert, values)

        tx {
            simpleMigration(
                    table,
                    h2Query,
                    emptyMap(),
                    postgresqlUpdate
            )
        }
    }

    private fun simpleMigration(name: String, h2Query: String, h2Params: Map<String, Any>, postgresqlUpdate: String) {
        logger.info("Migrating {}...", name)
        val sources: List<Map<String, Any>> = h2.queryForList(h2Query, h2Params)
        val count = sources.size
        @Suppress("UNCHECKED_CAST")
        val array: Array<Map<String, Any>> = sources.toTypedArray<Map<*, *>>() as Array<Map<String, Any>>
        try {
            postgresql.batchUpdate(postgresqlUpdate, array)
        } catch (ex: DataAccessException) {
            val cause = ex.cause
            if (cause is BatchUpdateException) {
                val sqlException = cause.nextException
                throw RuntimeException("SQL Error", sqlException)
            } else {
                throw ex
            }
        }

        logger.info("{} count = {}...", name, count)
    }

}