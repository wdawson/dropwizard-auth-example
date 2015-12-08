package wdawson.samples.dropwizard.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class UserInfoTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws Exception {
        final UserInfo user = new UserInfo(1, "Jane Doe");

        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/users/JaneDoe.json"), UserInfo.class));

        assertThat(MAPPER.writeValueAsString(user)).isEqualTo(expected);
    }

    @Test
    public void deserializesFromJSON() throws Exception {
        final UserInfo expected = new UserInfo(1, "Jane Doe");

        assertThat(MAPPER.readValue(fixture("fixtures/users/JaneDoe.json"), UserInfo.class))
                .isEqualTo(expected);
    }
}
