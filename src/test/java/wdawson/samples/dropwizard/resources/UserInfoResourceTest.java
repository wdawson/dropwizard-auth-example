package wdawson.samples.dropwizard.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import wdawson.samples.dropwizard.api.UserInfo;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author wdawson
 */
public class UserInfoResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new UserInfoResource("fixtures/users/test-names.txt"))
            .build();

    private final UserInfo jane = new UserInfo(1, "Jane Doe");
    private final UserInfo john = new UserInfo(2, "John Doe");

    @Test
    public void testGetUserReturnsUserInfo() {
        assertThat(resources.client().target("/users/1").request().get(UserInfo.class))
                .isEqualTo(jane);
        assertThat(resources.client().target("/users/2").request().get(UserInfo.class))
                .isEqualTo(john);
    }

    @Test
    public void testGetUserReturns404ForLargerID() {
        try {
            UserInfo userInfo = resources.client().target("/users/3").request().get(UserInfo.class);
            fail("Expected a 404 but got: " + userInfo);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Test
    public void testGetAllUsersReturnsAllUsersInOrder() {
        List<UserInfo> users = resources.client().target("/users").request()
                .get(new GenericType<List<UserInfo>>() { });

        assertThat(users).containsExactly(jane, john);
    }
}
