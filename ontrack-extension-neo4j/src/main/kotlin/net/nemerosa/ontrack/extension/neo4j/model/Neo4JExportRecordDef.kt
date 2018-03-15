package net.nemerosa.ontrack.extension.neo4j.model

import com.google.common.collect.ImmutableList
import lombok.Data

import java.util.ArrayList
import java.util.function.Function

class Neo4JExportRecordDef<T>(
    val type: Neo4JExportRecordType,
    val name: String,
    val columns: List<Neo4JExportColumn<T>>
) {

    @Data
    class Neo4JExportRecordDefBuilder<T> {
        val type: Neo4JExportRecordType? = null
        val name: String? = null
        private val columns = ArrayList<Neo4JExportColumn<T>>()

        fun with(header: String, valueFn: Function<T, Any>): Neo4JExportRecordDefBuilder<T> {
            columns.add(Neo4JExportColumn.of(header, valueFn))
            return this
        }

        fun build(): Neo4JExportRecordDef<T> {
            return Neo4JExportRecordDef(
                    type,
                    name,
                    ImmutableList.copyOf(columns)
            )
        }
    }

    companion object {

        fun <T> node(name: String, idFn: Function<T, Any>): Neo4JExportRecordDefBuilder<T> {
            return Neo4JExportRecordDefBuilder<T>(Neo4JExportRecordType.NODE, name)
                    .with(":LABEL", { o -> name })
                    .with(
                            nodeIdLabel(name),
                            nodeIdValueFn(name, idFn)
                    )
        }

        fun <T> rel(name: String, startFn: Function<T, Any>, endFn: Function<T, Any>): Neo4JExportRecordDefBuilder<T> {
            return Neo4JExportRecordDefBuilder<T>(Neo4JExportRecordType.REL, name)
                    .with(":TYPE", { o -> name })
                    .with(":START_ID", startFn)
                    .with(":END_ID", endFn)
        }

        fun <T> nodeIdValueFn(name: String, idFn: Function<T, Any>): Function<T, Any> {
            return { t -> String.format("%s//%s", name, idFn.apply(t)) }
        }

        fun nodeIdLabel(name: String): String {
            return String.format("%s:ID", name)
        }
    }

}
