package net.nemerosa.ontrack.extension.neo4j

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ontrack.config.neo4j")
class Neo4JConfigProperties {

    /**
     * Retention period for a download (in minutes, -1 to keep them forever)
     */
    var exportDownloadRetentionMinutes = 15

    /**
     * Path for the export files, relative to the working directory of Ontrack
     */
    var exportDownloadPath = "neo4j/export"

}
