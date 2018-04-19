package net.nemerosa.ontrack.extension.neo4j.core

import net.nemerosa.ontrack.model.structure.ID
import net.nemerosa.ontrack.model.structure.NameDescription
import net.nemerosa.ontrack.model.structure.Project
import org.junit.Test
import kotlin.test.assertEquals

class EntityIDFormatterTest {

    @Test
    fun `ID formatted as pseudo UUID`() {
        // Creating a project
        val project = Project.of(NameDescription.nd("Test", "")).withId(ID.of(23))
        // UUID
        val id = entityId<Project>()(project)
        // Check
        assertEquals("00000000-0000-0000-0001-000000000023", id)
    }

}