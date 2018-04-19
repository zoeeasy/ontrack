package net.nemerosa.ontrack.extension.neo4j.model

import net.nemerosa.ontrack.extension.neo4j.core.entityId
import net.nemerosa.ontrack.model.structure.ProjectEntity

fun extractors(code: ExtractorsContext.() -> Unit): List<Neo4JExportRecordExtractor<*>> {
    return ExtractorsContext()
            .apply { code() }
            .getRecordExtractors()
}

class ExtractorsContext {
    private val recordExtractors = mutableListOf<Neo4JExportRecordExtractor<*>>()
    fun <T> extractor(code: ExtractorContext<T>.() -> Unit) {
        ExtractorContext<T>()
                .apply { code() }
                .getRecordExtractor()
                .apply { recordExtractors += this }
    }

    fun getRecordExtractors() = recordExtractors.toList()
}

class ExtractorContext<T> {
    private var items: () -> Sequence<T> = { emptySequence() }
    private val recordDefinitions = mutableListOf<Neo4JExportRecordDef<T>>()

    fun records(items: () -> Sequence<T>) {
        this.items = items
    }

    fun node(name: String, code: NodeContext<T>.() -> Unit) {
        NodeContext<T>(name)
                .apply { code() }
                .getRecordDef()
                .apply { recordDefinitions += this }
    }

    fun rel(name: String, code: RelContext<T>.() -> Unit) {
        RelContext<T>(name)
                .apply { code() }
                .getRecordDef()
                .apply { recordDefinitions += this }
    }

    fun getRecordExtractor() = Neo4JExportRecordExtractor(
            items,
            recordDefinitions.toList()
    )
}

inline fun <reified T : ProjectEntity, reified U : ProjectEntity> ExtractorContext<T>.rel(name: String, crossinline to: (T) -> U) {
    rel(name) {
        start(entityId())
        end(entityId(to))
    }
}

abstract class AbstractGraphContext<T> {
    protected val columns = mutableListOf<Neo4JExportColumn<T>>()

    fun column(field: Pair<String, (T) -> Any>) {
        columns += Neo4JExportColumn(
                field.first,
                field.second
        )
    }

    abstract fun getRecordDef(): Neo4JExportRecordDef<T>
}

class NodeContext<T>(private val name: String) : AbstractGraphContext<T>() {

    init {
        column(":LABEL" to { _ -> name })
    }

    fun id(idProvider: (T) -> String) {
        column(":ID" to idProvider)
    }

    override fun getRecordDef() = Neo4JExportRecordDef(
            type = Neo4JExportRecordType.NODE,
            name = name,
            columns = columns.toList()
    )
}


class RelContext<T>(private val name: String) : AbstractGraphContext<T>() {

    init {
        column(":TYPE" to { _ -> name })
    }

    fun start(field: (T) -> String) {
        column(":START_ID" to field)
    }

    fun end(field: (T) -> String) {
        column(":END_ID" to field)
    }

    override fun getRecordDef() = Neo4JExportRecordDef(
            type = Neo4JExportRecordType.REL,
            name = name,
            columns = columns.toList()
    )
}
