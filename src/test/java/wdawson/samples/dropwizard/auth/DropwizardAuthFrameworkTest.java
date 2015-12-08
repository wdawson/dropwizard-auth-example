package wdawson.samples.dropwizard.auth;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class tests our assumptions about how the Dropwizard auth framework works. Specifically we validate our
 * assumptions about the @Auth, @RolesAllowed, @PermitAll, and @DenyAll annotations and their interoperability with
 * each other.
 *
 * @author Jon Todd
 */
@RunWith(MockitoJUnitRunner.class)
public class DropwizardAuthFrameworkTest {

    @Rule
    public ResourceTestRule rule = ResourceTestRule
            .builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(new AuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(new TestAuthenticator())
                    .setAuthorizer(new OAuth2Authorizer())
                    .setPrefix("Bearer")
                    .buildAuthFilter()))
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .addResource(new TestResource())
            .build();

    // Test that an endpoint with @RolesAllowed annotation passes when user has allowed role
    @Test
    public void testRolesAllowedAnnotationWithValidRole() throws Exception {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/rolesAllowedEndpoint")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,USER_READ_ONLY")
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    // Test that an endpoint with @RolesAllowed annotation fails when user doesn't have allowed role
    @Test
    public void testRolesAllowedAnnotationWithInvalidRole() throws Exception {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/rolesAllowedEndpoint")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,BAD_ROLE")
                .get();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    // Test that an endpoint with only @Auth annotation passes when user is authenticated
    @Test
    public void testAuthAnnotationWithValidUser() {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/authEndpoint")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,USER_READ_ONLY")
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeaderString("orgId")).isEqualTo("myOrgId");
        assertThat(response.getHeaderString("userId")).isEqualTo("myUserId");
    }

    // Test that an endpoint with only @Auth annotation fails when token can't be authenticated
    @Test
    public void testAuthAnnotationWithInvalidUser() {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/authEndpoint")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer invalidtoken")
                .get();

        assertThat(response.getStatus()).isEqualTo(401);
    }

    // Test that an endpoint with only @DenyAll annotation will always return an authz error
    @Test
    public void testDenyAllAnnotationReturnsError() {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/denyAllEndpoint")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,USER_READ_ONLY")
                .get();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    // @DenyAll annotation overrides a valid @AllowRoles annotation
    @Test
    public void testDenyAllAnnotationReturnsErrorEvenWithValidRole() {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/denyAllEndpointWithRole")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,USER_READ_ONLY")
                .get();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    // Test that an endpoint with only @PermitAll annotation does not fail authz check
    @Test
    public void testPermitAllAnnotationReturnsSuccess() {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/permitAllEndpoint")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,USER_READ_ONLY")
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    // @AllowRoles annotation overrides a @PermitAll annotation
    @Test
    public void testPermitAllAnnotationOverridesRoles() {
        final Response response = rule.getJerseyTest().target("/api/v1/testResource/permitAllEndpointWithRole")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Bearer myUserId,myOrgId,BAD_ROLE")
                .get();

        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Path("/api/v1/testResource")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestResource {
        @GET
        @Path("/rolesAllowedEndpoint")
        @RolesAllowed({Role.USER_READ_ONLY})
        public Response rolesAllowedEndpoint(@Auth User user) throws Exception {
            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder.header("userId", user.getId());
            responseBuilder.header("orgId", user.getOrgId());
            return responseBuilder.build();
        }

        @GET
        @Path("/authEndpoint")
        public Response authEndpoint(@Auth User user) throws Exception {
            Response.ResponseBuilder responseBuilder = Response.ok();
            responseBuilder.header("userId", user.getId());
            responseBuilder.header("orgId", user.getOrgId());
            return responseBuilder.build();
        }

        @GET
        @Path("/denyAllEndpoint")
        @DenyAll
        public Response denyAllEndpoint() throws Exception {
            Response.ResponseBuilder responseBuilder = Response.ok();
            return responseBuilder.build();
        }

        @GET
        @Path("/denyAllEndpointWithRole")
        @DenyAll
        @RolesAllowed({Role.USER_READ_ONLY})
        public Response denyAllEndpointWithRole() throws Exception {
            Response.ResponseBuilder responseBuilder = Response.ok();
            return responseBuilder.build();
        }

        @GET
        @Path("/permitAllEndpoint")
        @PermitAll
        public Response permitAllEndpoint() throws Exception {
            Response.ResponseBuilder responseBuilder = Response.ok();
            return responseBuilder.build();
        }

        @GET
        @Path("/permitAllEndpointWithRole")
        @PermitAll
        @RolesAllowed({Role.USER_READ_ONLY})
        public Response permitAllEndpointWithRole() throws Exception {
            Response.ResponseBuilder responseBuilder = Response.ok();
            return responseBuilder.build();
        }
    }

    /**
     * Simple implementation of Authenticator for testing. Assumes OAuth tokens are provided in CSV format with 3
     * entries of the form: "userId,orgId,role"
     */
    public static class TestAuthenticator implements Authenticator<String, User> {

        @Override
        public Optional<User> authenticate(String credentials) throws AuthenticationException {
            List<String> values = Splitter.on(",").trimResults().splitToList(credentials);
            if (values.size() == 3) {
                return Optional.of(
                        User.newBuilder()
                                .withId(values.get(0))
                                .withOrgId(values.get(1))
                                .withRoles(ImmutableSet.of(values.get(2)))
                                .build()
                );
            }
            return Optional.absent();
        }
    }
}
