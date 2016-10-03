package wdawson.samples.dropwizard.helpers;

import com.google.common.collect.Lists;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import wdawson.samples.dropwizard.UserInfoApplication;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.util.jwt.JwtIssuer;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;
import wdawson.samples.dropwizard.util.resources.ConfigurableURLStreamHandlerFactory;
import wdawson.samples.revoker.RevokerApplication;
import wdawson.samples.revoker.configuration.RevokerConfiguration;

import javax.ws.rs.client.Client;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.UUID;

import static com.google.common.io.Resources.getResource;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

/**
 * Base integration test and boiler plate
 *
 * @author wdawson
 */
public class IntegrationTest {

    protected static final DropwizardAppRule<UserInfoConfiguration> USER_INFO_APP_RULE =
            new DropwizardAppRule<>(UserInfoApplication.class, resourceFilePath("dropwizard/valid-conf.yml"));

    private static final DropwizardAppRule<RevokerConfiguration> ROOT_REVOKER_APP_RULE =
            new DropwizardAppRule<>(RevokerApplication.class, resourceFilePath("revoker/root-conf.yml"));

    private static final DropwizardAppRule<RevokerConfiguration> INTERMEDIATE_REVOKER_APP_RULE =
            new DropwizardAppRule<>(RevokerApplication.class, resourceFilePath("revoker/intermediate-conf.yml"));

    @ClassRule
    public static final TestRule RULE_CHAIN = RuleChain
            .outerRule(ROOT_REVOKER_APP_RULE)
            .around(INTERMEDIATE_REVOKER_APP_RULE)
            .around(USER_INFO_APP_RULE);

    private static final char[] PASSPHRASE = "notsecret".toCharArray();

    protected static String adminJWT;

    @BeforeClass
    public static void setupIntegrationTest() throws Exception {
        KeyStore jwtIssuerKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        jwtIssuerKeyStore.load(getResource("jwt/homepage-jwt-issuer.jks").openStream(), PASSPHRASE);

        RSAPrivateKey signingKey = (RSAPrivateKey) jwtIssuerKeyStore.getKey("jwt-issuer", PASSPHRASE);
        X509Certificate signingCertificate = (X509Certificate) jwtIssuerKeyStore.getCertificate("jwt-issuer");
        JwtIssuer jwtIssuer = new JwtIssuer(signingKey, signingCertificate);

        Date now = new Date();
        JwtClaims jwtClaims = JwtClaims.JwtClaimsBuilder.newInstance()
                .addJwtId(UUID.randomUUID().toString())
                .addSubject("1")
                .addNotBefore(now)
                .addIssuedAt(now)
                .addExpirationTime(new Date(now.getTime() + 10000)) // 10 seconds
                .addCustomClaim("orgId", "theOrgId")
                .addCustomClaim("version", 1)
                .addCustomClaim("scope", "ADMIN")
                .build();

        adminJWT = jwtIssuer.issueToken(jwtClaims);
    }

    @AfterClass
    public static void teardownIntegrationTest() throws Exception {
        ConfigurableURLStreamHandlerFactory.unsetURLStringHandlerFactory();
    }

    protected Client getNewSecureClient() throws Exception {
        return getNewSecureClient("tls/homepage-service-keystore.jks");
    }

    protected Client getNewSecureClient(String keyStoreResourcePath) throws Exception {
        TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setKeyStorePath(new File(resourceFilePath(keyStoreResourcePath)));
        tlsConfiguration.setKeyStorePassword("notsecret");

        tlsConfiguration.setTrustStorePath(new File(resourceFilePath("tls/test-truststore.jks")));
        tlsConfiguration.setTrustStorePassword("notsecret");

        tlsConfiguration.setVerifyHostname(false);

        tlsConfiguration.setSupportedCiphers(Lists.newArrayList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));
        tlsConfiguration.setSupportedProtocols(Lists.newArrayList("TLSv1.2"));

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setTlsConfiguration(tlsConfiguration);
        configuration.setTimeout(Duration.seconds(30));
        configuration.setConnectionTimeout(Duration.seconds(30));
        configuration.setConnectionRequestTimeout(Duration.seconds(30));

        return new JerseyClientBuilder(USER_INFO_APP_RULE.getEnvironment())
                .using(configuration)
                .build(UUID.randomUUID().toString());
    }
}
