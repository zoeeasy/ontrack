package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.common.BaseException

class Neo4JExportDownloadNotReadyException(uuid: String) : BaseException("Download with UUID = $uuid is not ready.")
