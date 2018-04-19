package net.nemerosa.ontrack.extension.neo4j.core

import net.nemerosa.ontrack.model.structure.Branch
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.model.structure.ProjectEntity

val ids = mapOf(
        Project::class to 1,
        Branch::class to 2
)

inline fun <reified T : ProjectEntity> entityId(): (T) -> String {
    val typeId = ids[T::class] ?: throw IllegalArgumentException("Project entity not managed: ${T::class}")
    return { t ->
        val id = t.id()
        "00000000-0000-0000-%04d-%012d".format(typeId, id)
    }
}