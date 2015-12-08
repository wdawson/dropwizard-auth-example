package wdawson.samples.dropwizard.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.util.jwt.JwtIssuer;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericType;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author wdawson
 */
public class UserInfoResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new UserInfoResource("fixtures/users/test-names.txt"))
            .build();

    private static final char[] PASSPHRASE = "notsecret".toCharArray();

    private static String jwt;

    private final UserInfo jane = new UserInfo(1, "Jane Doe");
    private final UserInfo john = new UserInfo(2, "John Doe");

    @BeforeClass
    public static void setupClass() throws Exception {
        KeyStore jwtIssuerKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        jwtIssuerKeyStore.load(getResource("jwt/homepage-jwt-issuer.jks").openStream(), PASSPHRASE);

        RSAPrivateKey signingKey = (RSAPrivateKey) jwtIssuerKeyStore.getKey("jwt-issuer", PASSPHRASE);
        X509Certificate signingCertificate = (X509Certificate) jwtIssuerKeyStore.getCertificate("jwt-issuer");
        JwtIssuer jwtIssuer = new JwtIssuer(signingKey, signingCertificate);

        Date now = new Date();
        JwtClaims jwtClaims = JwtClaims.JwtClaimsBuilder.newInstance()
                .addJwtId(UUID.randomUUID().toString())
                .addSubject("userId")
                .addNotBefore(now)
                .addIssuedAt(now)
                .addExpirationTime(new Date(now.getTime() + 10000)) // 10 seconds
                .addCustomClaim("orgId", "theOrgId")
                .addCustomClaim("version", 1)
                .addCustomClaim("scope", "ADMIN")
                .build();

        jwt = jwtIssuer.issueToken(jwtClaims);
    }

    @Test
    public void testGetUserReturnsUserInfo() {
        assertThat(resources.client().target("/users/1").request()
                .header("Authorization", "Bearer " + jwt)
                .get(UserInfo.class))
                .isEqualTo(jane);
        assertThat(resources.client().target("/users/2").request()
                .header("Authorization", "Bearer " + jwt)
                .get(UserInfo.class))
                .isEqualTo(john);
    }

    @Test
    public void testGetUserReturns404ForLargerID() {
        try {
            UserInfo userInfo = resources.client().target("/users/3").request()
                    .header("Authorization", "Bearer " + jwt)
                    .get(UserInfo.class);
            fail("Expected a 404 but got: " + userInfo);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Test
    public void testGetAllUsersReturnsAllUsersInOrder() {
        List<UserInfo> users = resources.client().target("/users").request()
                .header("Authorization", "Bearer " + jwt)
                .get(new GenericType<List<UserInfo>>() { });

        assertThat(users).containsExactly(jane, john);
    }
}
