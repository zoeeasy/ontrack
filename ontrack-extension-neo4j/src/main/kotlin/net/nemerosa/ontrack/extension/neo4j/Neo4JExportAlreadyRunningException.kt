package net.nemerosa.ontrack.extension.neo4j

import net.nemerosa.ontrack.common.BaseException

class Neo4JExportAlreadyRunningException: BaseException("A Neo4J export is already running in the background. Please stop it before launching it again.")
