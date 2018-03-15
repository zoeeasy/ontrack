package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.common.BaseException

class Neo4JExportDownloadException(uuid: String, ex: Exception) : BaseException(ex, "Cannot download $uuid")
