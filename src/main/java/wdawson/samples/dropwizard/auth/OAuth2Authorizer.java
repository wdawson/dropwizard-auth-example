package wdawson.samples.dropwizard.auth;

import io.dropwizard.auth.Authorizer;

/**
 * @author Jon Todd
 */
public class OAuth2Authorizer implements Authorizer<User> {
    @Override
    public boolean authorize(User user, String role) {
        return user.getRoles().contains(role);
    }
}