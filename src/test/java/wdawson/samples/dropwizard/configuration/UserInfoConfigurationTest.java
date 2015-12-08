package wdawson.samples.dropwizard.configuration;

import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class UserInfoConfigurationTest {

    @Test
    public void testThatValidConfigurationIsPopulated() throws Exception {
        final UserInfoConfiguration validConfiguration = buildConfigurationFromString("dropwizard/valid-conf.yml");

        assertThat(validConfiguration.getData().getNamesResource()).isEqualTo("fixtures/users/test-names.txt");
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testThatInvalidConfigurationFailsToBeBuilt() throws Exception {
        buildConfigurationFromString("dropwizard/invalid-conf.yml");
    }

    @Test(expected = ConfigurationValidationException.class)
    public void testThatConfigurationWithEmptyValueFailsToBeBuilt() throws Exception {
        buildConfigurationFromString("dropwizard/empty-conf.yml");
    }

    private UserInfoConfiguration buildConfigurationFromString(String fileLocation) throws Exception {
        final File file = new File(Resources.getResource(fileLocation).toURI());

        return new DefaultConfigurationFactoryFactory<UserInfoConfiguration>()
                .create(UserInfoConfiguration.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(file);
    }
}
