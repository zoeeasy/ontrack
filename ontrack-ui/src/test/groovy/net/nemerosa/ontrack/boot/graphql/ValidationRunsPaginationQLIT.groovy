package net.nemerosa.ontrack.boot.graphql

import net.nemerosa.ontrack.model.structure.Branch
import net.nemerosa.ontrack.model.structure.ValidationRunStatusID
import org.junit.Before
import org.junit.Test

import static net.nemerosa.ontrack.model.structure.NameDescription.nd

class ValidationRunsPaginationQLIT extends AbstractQLITSupport {

    private Branch branch

    @Before
    void setup() {
        branch = doCreateBranch()
        def vs = doCreateValidationStamp(branch, nd('VS', ''))
        (1..20).each { buildNo ->
            def build = doCreateBuild(branch, nd("${buildNo}", ''))
            (1..5).each { runNo ->
                doValidateBuild(
                        build,
                        vs,
                        runNo > 1 ? ValidationRunStatusID.STATUS_PASSED : ValidationRunStatusID.STATUS_FAILED
                )
            }
        }
    }

    @Test
    void 'Getting all validation runs'() {
        def data = run("""{
            branches (id: ${branch.id}) {
                validationStamps(name: "VS") {
                    validationRuns {
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                        }
                        edges {
                            node {
                                build {
                                    name
                                }
                            }
                        }
                    }
                }
            }
        }""")
        assert data.branches.first().validationStamps.first().validationRuns.pageInfo.hasPreviousPage == false
        assert data.branches.first().validationStamps.first().validationRuns.pageInfo.hasNextPage == false
        assert data.branches.first().validationStamps.first().validationRuns.edges.size() == 100
    }

    @Test
    void 'Getting 20 first runs'() {
        def data = run("""{
            branches (id: ${branch.id}) {
                validationStamps(name: "VS") {
                    validationRuns(first: 20) {
                        pageInfo {
                            hasNextPage
                            hasPreviousPage
                        }
                        edges {
                            node {
                                build {
                                    name
                                }
                            }
                        }
                    }
                }
            }
        }""")
        assert data.branches.first().validationStamps.first().validationRuns.pageInfo.hasPreviousPage == false
        assert data.branches.first().validationStamps.first().validationRuns.pageInfo.hasNextPage == true
        assert data.branches.first().validationStamps.first().validationRuns.edges.size() == 20
    }

}
