package wdawson.samples.dropwizard.auth;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdawson.samples.dropwizard.util.jwt.JwtVerifier;
import wdawson.samples.dropwizard.util.jwt.dto.Jwt;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;
import wdawson.samples.dropwizard.util.jwt.exception.JwtParseException;
import wdawson.samples.dropwizard.util.jwt.exception.JwtVerifyException;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Jon Todd
 */
public class OAuth2Authenticator implements Authenticator<String, User> {
    private static final Logger LOG = LoggerFactory.getLogger(OAuth2Authenticator.class);

    private final JwtVerifier jwtVerifier;

    public OAuth2Authenticator(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public Optional<User> authenticate(String s) throws AuthenticationException {
        JwtClaims claims;
        try {
            claims = jwtVerifier.authenticate(Jwt.newFromString(s));
        } catch (JwtParseException | JwtVerifyException e) {
            LOG.error("Failed to authenticate token!", e);
            throw new AuthenticationException("Failed to authenticate token!", e);
        }

        Set<String> roles = parseRolesClaim(claims);

        return Optional.of(
                User.newBuilder()
                    .withId(claims.getSubject())
                    .withOrgId((String) claims.getClaim("orgId"))
                    .withRoles(roles)
                    .build());
    }

    private Set<String> parseRolesClaim(JwtClaims claims) throws AuthenticationException {
        Object scopesObject = claims.getClaim("scope");
        String[] scopes = {};
        if (scopesObject != null && scopesObject instanceof String) {
            scopes = ((String) scopesObject).split(" ");
        }

        return ImmutableSet.copyOf(Arrays.asList(scopes));
    }
}