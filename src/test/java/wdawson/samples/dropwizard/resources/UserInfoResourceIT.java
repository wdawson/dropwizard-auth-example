package wdawson.samples.dropwizard.resources;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.helpers.IntegrationTest;
import wdawson.samples.dropwizard.util.jwt.JwtIssuer;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * @author wdawson
 */
public class UserInfoResourceIT extends IntegrationTest {

    private static final char[] PASSPHRASE = "notsecret".toCharArray();

    private final UserInfo jane = new UserInfo(1, "Jane Doe");
    private final UserInfo john = new UserInfo(2, "John Doe");

    private static JwtIssuer jwtIssuer;

    private Client client;

    private JwtClaims.JwtClaimsBuilder claimsBuilder;

    @BeforeClass
    public static void setupClass() throws Exception {
        KeyStore jwtIssuerKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        jwtIssuerKeyStore.load(getResource("jwt/homepage-jwt-issuer.jks").openStream(), PASSPHRASE);

        RSAPrivateKey signingKey = (RSAPrivateKey) jwtIssuerKeyStore.getKey("jwt-issuer", PASSPHRASE);
        X509Certificate signingCertificate = (X509Certificate) jwtIssuerKeyStore.getCertificate("jwt-issuer");
        jwtIssuer = new JwtIssuer(signingKey, signingCertificate);
    }

    @Before
    public void setup() throws Exception {
        client = getNewSecureClient();

        Date now = new Date();
        claimsBuilder = JwtClaims.JwtClaimsBuilder.newInstance()
                .addJwtId(UUID.randomUUID().toString())
                .addSubject("userId")
                .addNotBefore(now)
                .addIssuedAt(now)
                .addExpirationTime(new Date(now.getTime() + 10000)) // 10 seconds
                .addCustomClaim("orgId", "theOrgId")
                .addCustomClaim("version", 1)
                .addCustomClaim("scope", "ADMIN");
    }

    @Test
    public void userInfoReturnsAllUsersInOrder() throws Exception {
        List<UserInfo> users = client.target(String.format("https://localhost:%d/users", 8443))
                .request()
                .header("Authorization", "Bearer " + adminJWT)
                .get(new GenericType<List<UserInfo>>() { });

        assertThat(users).containsExactly(jane, john);
    }

    @Test
    public void userInfoReturnsAnySpecificUserForAdmin() throws Exception{
        UserInfo user = client.target(String.format("https://localhost:%d/users/%s", 8443, "2"))
                .request()
                .header("Authorization", "Bearer " + adminJWT)
                .get(new GenericType<UserInfo>() { });

        assertThat(user).isEqualTo(john);
    }

    @Test
    public void userInfoReturnsOnlyCallersUser() throws Exception {
        claimsBuilder.addSubject("1")
                .addCustomClaim("scope", "USER_READ_ONLY");

        String jwt = jwtIssuer.issueToken(claimsBuilder.build());

        UserInfo user = client.target(String.format("https://localhost:%d/users/%s", 8443, "1"))
                .request()
                .header("Authorization", "Bearer " + jwt)
                .get(new GenericType<UserInfo>() { });

        assertThat(user).isEqualTo(jane);
    }

    @Test
    public void userInfoErrorsOnWhenNonAdminUserTriesToGetOtherUser() throws Exception {
        claimsBuilder.addSubject("2")
                .addCustomClaim("scope", "USER_READ_ONLY");

        String jwt = jwtIssuer.issueToken(claimsBuilder.build());

        try {
            client.target(String.format("https://localhost:%d/users/%s", 8443, "1"))
                    .request()
                    .header("Authorization", "Bearer " + jwt)
                    .get(new GenericType<UserInfo>() { });
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            assertThat(response.getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }
}
