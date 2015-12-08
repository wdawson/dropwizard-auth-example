package wdawson.samples.dropwizard.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class UserInfoHealthCheckTest {

    @Test
    public void testHealthCheckParsesNames() throws Exception {
        UserInfoHealthCheck healthCheck = new UserInfoHealthCheck("fixtures/users/test-names.txt");
        assertThat(healthCheck.check()).isEqualTo(HealthCheck.Result.healthy());
        assertThat(healthCheck.getNames()).containsExactly("Jane Doe", "John Doe");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHealthCheckConstructionFailsIfNamesAreNotFound() throws Exception {
        UserInfoHealthCheck healthCheck = new UserInfoHealthCheck("fixtures/users/does-not-exist.txt");
    }

    @Test
    public void testHealthCheckIsUnhealthyIfNamesAreNotFound() throws Exception {
        UserInfoHealthCheck healthCheck = new UserInfoHealthCheck(Collections.emptyList());

        // Compare the value of the healthy boolean since we don't really care about the message
        assertThat(healthCheck.check().isHealthy())
                .isEqualTo(HealthCheck.Result.unhealthy("No names were found").isHealthy());
    }

    @Test
    public void testHealthCheckIsUnhealthyIfNamesAreBlank() throws Exception {
        UserInfoHealthCheck healthCheck = new UserInfoHealthCheck(Lists.newArrayList("Jane Doe", "John Doe", "  "));

        // Compare the value of the healthy boolean since we don't really care about the message
        assertThat(healthCheck.check().isHealthy())
                .isEqualTo(HealthCheck.Result.unhealthy("Name was blank").isHealthy());
    }
}
