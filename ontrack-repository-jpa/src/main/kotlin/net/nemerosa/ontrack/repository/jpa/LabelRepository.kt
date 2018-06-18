package net.nemerosa.ontrack.repository.jpa

import net.nemerosa.ontrack.model.structure.Label
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface LabelRepository : PagingAndSortingRepository<Label, Long> {

    fun findLabelByCategoryAndNameAndComputed(
            category: String,
            name: String,
            computed: Boolean
    ): Label?

}
