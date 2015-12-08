package wdawson.samples.dropwizard;

import com.google.common.annotations.VisibleForTesting;
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
import wdawson.samples.dropwizard.filters.TLSCertificateAuthorizationFilter;
import wdawson.samples.dropwizard.health.UserInfoHealthCheck;
import wdawson.samples.dropwizard.resources.UserInfoResource;
import wdawson.samples.dropwizard.util.jwt.JwtVerifier;
import wdawson.samples.dropwizard.util.resources.ClasspathURLStreamHandler;
import wdawson.samples.dropwizard.util.resources.ConfigurableURLStreamHandlerFactory;

import java.net.URL;


/**
 * Sample Dropwizard application for finding user information
 *
 * Demonstrates:
 *   - TLS client authentication
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

        // allow the classpath protocol for urls
        ConfigurableURLStreamHandlerFactory urlHandlerFactory = new ConfigurableURLStreamHandlerFactory()
                .withHandler(ClasspathURLStreamHandler.PROTOCOL, new ClasspathURLStreamHandler())
                // Get default java handlers for known protocols
                .withStandardJavaHandlers();

        URL.setURLStreamHandlerFactory(urlHandlerFactory);
    }

    @Override
    public void run(UserInfoConfiguration configuration, Environment environment) throws Exception {
        // Override Java's trusted cacerts with our own trust store.
        System.setProperty("javax.net.ssl.trustStore", "java-cacerts.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "notsecret");

        // Register Filters
        registerFilters(configuration, environment);

        // Register Resources
        registerResources(configuration, environment);

        // Setup user auth
        registerUserAuth(configuration, environment);
    }

    @VisibleForTesting
    void registerFilters(UserInfoConfiguration configuration, Environment environment) {
        String dnRegex = configuration.getSecurity().getTlsAuthZ().getAllowedClientDnRegex();
        final TLSCertificateAuthorizationFilter tlsAuthZFilter = new TLSCertificateAuthorizationFilter(dnRegex);

        environment.jersey().register(tlsAuthZFilter);
    }

    @VisibleForTesting
    void registerResources(UserInfoConfiguration configuration, Environment environment) {
        final UserInfoHealthCheck userInfoHealthCheck = new UserInfoHealthCheck(configuration.getData().getNamesResource());
        final UserInfoResource userInfoResource = new UserInfoResource(configuration.getData().getNamesResource());

        environment.healthChecks().register("users", userInfoHealthCheck);
        environment.jersey().register(userInfoResource);
    }

    @VisibleForTesting
    void registerUserAuth(UserInfoConfiguration configuration, Environment environment) {
        JwtVerifier jwtVerifier = configuration.getSecurity().getJwtVerification().newInstanceFromConfig();
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
