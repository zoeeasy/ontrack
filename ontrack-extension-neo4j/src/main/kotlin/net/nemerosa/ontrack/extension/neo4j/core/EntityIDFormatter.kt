package net.nemerosa.ontrack.extension.neo4j.core

import net.nemerosa.ontrack.model.structure.Branch
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.model.structure.ProjectEntity

val ids = mapOf(
        Project::class to 1,
        Branch::class to 2
)

const val ENTITY_PSEUDO_UUID = "00000000-0000-0000-%04d-%012d"

inline fun <reified T : ProjectEntity> entityId(): (T) -> String {
    val typeId = ids[T::class] ?: throw IllegalArgumentException("Project entity not managed: ${T::class}")
    return { t ->
        val id = t.id()
        ENTITY_PSEUDO_UUID.format(typeId, id)
    }
}

inline fun <reified T : ProjectEntity, reified U : ProjectEntity> entityId(crossinline fn: (T) -> U): (T) -> String {
    val typeId = ids[U::class] ?: throw IllegalArgumentException("Project entity not managed: ${U::class}")
    return { t ->
        val u = fn(t)
        val id = u.id()
        ENTITY_PSEUDO_UUID.format(typeId, id)
    }
}
