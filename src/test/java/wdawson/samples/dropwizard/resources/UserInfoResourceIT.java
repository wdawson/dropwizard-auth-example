package wdawson.samples.dropwizard.resources;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import wdawson.samples.dropwizard.UserInfoApplication;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class UserInfoResourceIT {

    private final UserInfo jane = new UserInfo(1, "Jane Doe");
    private final UserInfo john = new UserInfo(2, "John Doe");

    @ClassRule
    public static final DropwizardAppRule<UserInfoConfiguration> RULE =
            new DropwizardAppRule<>(UserInfoApplication.class, resourceFilePath("dropwizard/valid-conf.yml"));

    @Test
    public void userInfoReturnsAllUsersInOrder() {
        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("user info test client");

        List<UserInfo> users = client.target(String.format("http://localhost:%d/users", RULE.getLocalPort()))
                .request()
                .get(new GenericType<List<UserInfo>>() { });

        assertThat(users).containsExactly(jane, john);
    }
}
