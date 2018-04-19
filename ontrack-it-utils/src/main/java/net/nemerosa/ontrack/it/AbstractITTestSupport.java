package net.nemerosa.ontrack.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.nemerosa.ontrack.common.RunProfile;
import net.nemerosa.ontrack.json.ObjectMapperFactory;
import net.nemerosa.ontrack.model.structure.NameDescription;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;

import static net.nemerosa.ontrack.test.TestUtils.uid;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        loader = AnnotationConfigContextLoader.class,
        classes = AbstractITTestSupport.AbstractIntegrationTestConfiguration.class)
@Transactional
@ActiveProfiles(profiles = {RunProfile.UNIT_TEST})
public abstract class AbstractITTestSupport extends AbstractTransactionalJUnit4SpringContextTests {

    @Configuration
    @Profile(RunProfile.UNIT_TEST)
    @ComponentScan("net.nemerosa.ontrack")
    public static class AbstractIntegrationTestConfiguration {
    }

    public static NameDescription nameDescription() {
        String uid = uid("");
        return new NameDescription(
                uid,
                String.format("%s description", uid)
        );
    }

    protected final ObjectMapper objectMapper = ObjectMapperFactory.create();

    @Autowired
    protected PlatformTransactionManager platformTransactionManager;

    protected <T> T tx(Callable<T> callable) {
        TransactionTemplate txTemplate = new TransactionTemplate(
                platformTransactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        );
        return txTemplate.execute(status -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
