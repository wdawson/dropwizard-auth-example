package wdawson.samples.dropwizard.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import wdawson.samples.dropwizard.util.resources.ResourceUtils;

import java.util.List;

/**
 * Health check for user info
 *
 * Checks that we have access to user data store
 *
 * @author wdawson
 */
public class UserInfoHealthCheck extends HealthCheck {

    private final String namesResource;
    private final List<String> names;

    public UserInfoHealthCheck(String namesResource) {
        this.namesResource = namesResource;
        names = ResourceUtils.readLinesFromResource(this.namesResource);
    }

    @VisibleForTesting
    public UserInfoHealthCheck(List<String> names) {
        this.namesResource = null;
        this.names = names;
    }

    @Override
    protected Result check() throws Exception {
        if (names == null || names.isEmpty()) {
            return Result.unhealthy("Could not find any names in the resource: " + namesResource);
        }

        for (String name : names) {
            if (StringUtils.isBlank(name)) {
                return Result.unhealthy("Names contained a blank name");
            }
        }

        return Result.healthy();
    }

    @VisibleForTesting
    public List<String> getNames() {
        return names;
    }

    @VisibleForTesting
    public String getNamesResource() {
        return namesResource;
    }
}
