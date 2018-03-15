package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.model.exceptions.NotFoundException

class Neo4JExportNoDownloadException : NotFoundException("No download is available for download")
