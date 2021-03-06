package net.nemerosa.ontrack.graphql.schema

import graphql.Scalars
import graphql.Scalars.GraphQLInt
import graphql.schema.DataFetcher
import graphql.schema.GraphQLArgument
import graphql.schema.GraphQLFieldDefinition.newFieldDefinition
import graphql.schema.GraphQLObjectType
import graphql.schema.GraphQLObjectType.newObject
import graphql.schema.GraphQLTypeReference
import net.nemerosa.ontrack.graphql.support.GraphqlUtils
import net.nemerosa.ontrack.graphql.support.pagination.GQLPaginatedListFactory
import net.nemerosa.ontrack.model.structure.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class GQLTypeValidationStamp(
        private val structureService: StructureService,
        creation: GQLTypeCreation,
        private val projectEntityInterface: GQLProjectEntityInterface,
        private val paginatedListFactory: GQLPaginatedListFactory,
        private val validationRun: GQLTypeValidationRun,
        projectEntityFieldContributors: List<GQLProjectEntityFieldContributor>
) : AbstractGQLProjectEntity<ValidationStamp>(
        ValidationStamp::class.java,
        ProjectEntityType.VALIDATION_STAMP,
        projectEntityFieldContributors,
        creation
) {

    override fun getTypeName() = VALIDATION_STAMP

    override fun createType(cache: GQLTypeCache): GraphQLObjectType {
        return newObject()
                .name(VALIDATION_STAMP)
                .withInterface(projectEntityInterface.typeRef)
                .fields(projectEntityInterfaceFields())
                // Image flag
                .field { f ->
                    f.name("image")
                            .description("Flag to indicate if an image is associated")
                            .type(Scalars.GraphQLBoolean)
                }
                // Ref to branch
                .field(
                        newFieldDefinition()
                                .name("branch")
                                .description("Reference to branch")
                                .type(GraphQLTypeReference(GQLTypeBranch.BRANCH))
                                .build()
                )
                // Paginated list of validation runs
                .field(
                        paginatedListFactory.createPaginatedField<ValidationStamp, ValidationRun>(
                                cache = cache,
                                fieldName = "validationRunsPaginated",
                                fieldDescription = "Paginated list of validation runs",
                                itemType = validationRun,
                                itemListCounter = { environment, validationStamp ->
                                    val buildId: Int? = environment.getArgument<Int>("buildId")
                                    if (buildId != null) {
                                        structureService.getValidationRunsCountForBuildAndValidationStamp(
                                                ID.of(buildId),
                                                validationStamp.id
                                        )
                                    } else {
                                        structureService.getValidationRunsCountForValidationStamp(
                                                validationStamp.id
                                        )
                                    }
                                },
                                itemListProvider = { environment, validationStamp, offset, size ->
                                    val buildId: Int? = environment.getArgument<Int>("buildId")
                                    if (buildId != null) {
                                        structureService.getValidationRunsForBuildAndValidationStamp(
                                                ID.of(buildId),
                                                validationStamp.id,
                                                offset,
                                                size
                                        )
                                    } else {
                                        structureService.getValidationRunsForValidationStamp(
                                                validationStamp.id,
                                                offset,
                                                size
                                        )
                                    }
                                },
                                arguments = listOf(
                                        GraphQLArgument.newArgument()
                                                .name("buildId")
                                                .description("Validation runs for this build only")
                                                .type(GraphQLInt)
                                                .build()
                                )
                        )
                )
                // Validation runs
                .field(
                        newFieldDefinition()
                                .name("validationRuns")
                                .description("List of runs for this validation stamp")
                                .type(GraphqlUtils.stdList(validationRun.typeRef))
                                .argument {
                                    it.name("count")
                                            .description("Maximum number of validation runs")
                                            .type(Scalars.GraphQLInt)
                                            .defaultValue(50)
                                }
                                .dataFetcher(validationStampValidationRunsFetcher())
                                .build()
                )
                // OK
                .build()

    }

    private fun validationStampValidationRunsFetcher() =
            DataFetcher<List<ValidationRun>> { environment ->
                val validationStamp: ValidationStamp = environment.getSource()
                // Gets all the validation runs
                return@DataFetcher structureService.getValidationRunsForValidationStamp(
                        validationStamp.id,
                        0,
                        environment.getArgument<Int?>("count") ?: 50
                )
            }

    override fun getSignature(entity: ValidationStamp): Optional<Signature> {
        return Optional.ofNullable(entity.signature)
    }

    companion object {

        val VALIDATION_STAMP = "ValidationStamp"
    }

}
