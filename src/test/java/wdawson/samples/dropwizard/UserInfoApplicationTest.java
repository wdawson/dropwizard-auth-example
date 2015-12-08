package wdawson.samples.dropwizard;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.io.Resources;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author wdawson
 */
public class UserInfoApplicationTest {

    private UserInfoConfiguration configuration;

    private final Environment environment = mock(Environment.class);
    private final HealthCheckRegistry healthCheckRegistry = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);

    private final UserInfoApplication userInfoApplication = new UserInfoApplication();

    @Before
    public void setup() throws Exception {
        final File configFile = new File(Resources.getResource("dropwizard/valid-conf.yml").toURI());

        configuration = new DefaultConfigurationFactoryFactory<UserInfoConfiguration>()
                .create(UserInfoConfiguration.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(configFile);

        // Common Expectations
        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        assertThat(configuration.getNamesResource()).isEqualTo("fixtures/users/test-names.txt");
    }
}
