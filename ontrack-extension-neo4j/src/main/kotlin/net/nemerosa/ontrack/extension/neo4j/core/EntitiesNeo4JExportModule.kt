package net.nemerosa.ontrack.extension.neo4j.core

import net.nemerosa.ontrack.extension.neo4j.model.Neo4JExportModule
import net.nemerosa.ontrack.extension.neo4j.model.extractors
import net.nemerosa.ontrack.model.structure.Branch
import net.nemerosa.ontrack.model.structure.Project
import net.nemerosa.ontrack.model.structure.StructureService
import org.springframework.stereotype.Component

@Component
class EntitiesNeo4JExportModule(
        private val structureService: StructureService
) : Neo4JExportModule {
    override val recordExtractors = extractors {
        extractor<Project> {
            records { structureService.projectList.asSequence() }
            node("Project") {
                id(entityId())
                column("name" to Project::getName)
                column("description" to Project::getDescription)
                column("disabled" to Project::isDisabled)
                // TODO creator
                // TODO creation
            }
        }
        extractor<Branch> {
            records { structureService.projectList.asSequence().flatMap { structureService.getBranchesForProject(it.id).asSequence() } }
            node("Branch") {
                id(entityId())
                column("name" to Branch::getName)
                column("description" to Branch::getDescription)
                column("disabled" to Branch::isDisabled)
                // TODO creator
                // TODO creation
            }
            rel("BRANCH_OF") {
                start(entityId())
                end(entityId<Branch, Project> { t -> t.project })
            }
        }
    }
}