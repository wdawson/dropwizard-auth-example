package wdawson.samples.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.health.UserInfoHealthCheck;
import wdawson.samples.dropwizard.resources.UserInfoResource;

/**
 * Sample Dropwizard application
 *
 * @author wdawson
 */
public class UserInfoApplication extends Application<UserInfoConfiguration> {

    @Override
    public String getName() {
        return "User Info";
    }

    @Override
    public void initialize(Bootstrap<UserInfoConfiguration> bootstrap) {
        super.initialize(bootstrap);
    }

    @Override
    public void run(UserInfoConfiguration configuration, Environment environment) throws Exception {
        final UserInfoHealthCheck userInfoHealthCheck = new UserInfoHealthCheck(configuration.getNamesResource());
        final UserInfoResource userInfoResource = new UserInfoResource(configuration.getNamesResource());

        environment.healthChecks().register("users", userInfoHealthCheck);
        environment.jersey().register(userInfoResource);
    }

    public static void main(String[] args) throws Exception {
        new UserInfoApplication().run(args);
    }
}
