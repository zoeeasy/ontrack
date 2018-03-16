package net.nemerosa.ontrack.extension.neo4j

enum class Neo4JExportContextState {
    /**
     * Context just created
     */
    CREATED,
    /**
     * Export running
     */
    RUNNING,
    /**
     * Ready
     */
    READY
}