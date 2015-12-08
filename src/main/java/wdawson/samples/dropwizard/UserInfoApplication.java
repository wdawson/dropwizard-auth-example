package wdawson.samples.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import wdawson.samples.dropwizard.auth.OAuth2Authenticator;
import wdawson.samples.dropwizard.auth.OAuth2Authorizer;
import wdawson.samples.dropwizard.auth.User;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.health.UserInfoHealthCheck;
import wdawson.samples.dropwizard.resources.UserInfoResource;
import wdawson.samples.dropwizard.util.jwt.JwtVerifier;

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

        // Setup user auth
        JwtVerifier jwtVerifier = configuration.getSecurityConfiguration().getJwtVerificationConfiguration().newInstanceFromConfig();
        environment.jersey().register(new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<User>()
                        .setAuthenticator(new OAuth2Authenticator(jwtVerifier))
                        .setAuthorizer(new OAuth2Authorizer())
                        .setPrefix("Bearer")
                        .buildAuthFilter()
        ));
        // Enable the resource protection annotations: @RolesAllowed, @PermitAll & @DenyAll
        environment.jersey().register(RolesAllowedDynamicFeature.class);
        // Enable the @Auth annotation for binding authenticated users to resource method parameters
        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    }

    public static void main(String[] args) throws Exception {
        new UserInfoApplication().run(args);
    }
}
