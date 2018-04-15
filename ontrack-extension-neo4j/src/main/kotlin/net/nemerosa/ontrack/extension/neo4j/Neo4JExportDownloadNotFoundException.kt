package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.model.exceptions.NotFoundException

class Neo4JExportDownloadNotFoundException(uuid: String) : NotFoundException("Cannot find download with UUID = $uuid")
