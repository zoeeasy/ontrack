package net.nemerosa.ontrack.model.structure

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Label(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val category: String,
        val name: String,
        val description: String,
        val colour: String,
        val computed: Boolean
)
