package wdawson.samples.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.health.UserInfoHealthCheck;
import wdawson.samples.dropwizard.resources.UserInfoResource;
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

        final UserInfoHealthCheck userInfoHealthCheck = new UserInfoHealthCheck(configuration.getData().getNamesResource());
        final UserInfoResource userInfoResource = new UserInfoResource(configuration.getData().getNamesResource());

        environment.healthChecks().register("users", userInfoHealthCheck);
        environment.jersey().register(userInfoResource);
    }

    public static void main(String[] args) throws Exception {
        new UserInfoApplication().run(args);
    }
}
