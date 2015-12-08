package wdawson.samples.dropwizard.resources;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdawson.samples.dropwizard.UserInfoApplication;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.configuration.UserInfoConfiguration;
import wdawson.samples.dropwizard.util.jwt.JwtIssuer;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;
import wdawson.samples.dropwizard.util.x509.X509CertificateUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class UserInfoResourceIT {
    private static final Logger LOG = LoggerFactory.getLogger(UserInfoResourceIT.class);

    private static final Provider SECURITY_PROVIDER =  new BouncyCastleProvider();

    private static final RSAPrivateKey PRIVATE_KEY = parsePrivateKeyPemString(
            "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEoQIBAAKCAQEAqfYIyOq98r/K3w3W+PGrJwjy3CqS802EQYebKdm5DmyHKW74\n" +
            "46x7+Bv1f/UE9apwoL6DMvo0cUWV63OANiBe4OhZSqsz34kDmk7Ok4TLs8eKMtkq\n" +
            "HIDXI7+nAT/SBQApAmgCadAkTs+ARri8u9TO+b/UP3IdJ1BYJLtYjtP54aGUeIyv\n" +
            "e1pNs5vw/ecmci3bW/KfvRA4ODH0r42Bx8lctHEHaCfqwgH6rYcfrQ0aT60DmPWW\n" +
            "3TrYdOoe7wSjKvKWoZQl7BMMiPytGCSznq0KQZW0lKnI2N1WMTxpkWxBbE8D0skz\n" +
            "KPaBzSB86PJ3E7TptjDXR7XIxVJlwC4DZYiVKwIDAQABAoIBAQCpqa+PUbYYc5kD\n" +
            "HX+xtx3Rs78sRVu4gXM7LzGXj36KhZBPrjXKoU6HmPFzsJYo3uHbtRKnetmLEZnd\n" +
            "FsmwPpQ4E/m+7jS0OsRb77uLy47FgrXUmLDPD1a0mwcN2jW/RC+r9UQjeOIlwkId\n" +
            "VyEgqAmNiw9H3pR3wF6dHGAFBFp3/wH4nxI7XXummiruSV4Yg0IKq+8G4XL4tJ4Y\n" +
            "bEe+XlNDAxZpiRvPGu32sYoin/bG/shBUKVDqjpEnrEvvrnLw3ked059lg6hTZUX\n" +
            "dBA/6xI9eYYNf6kvM0FQaHXV2AXRHAf0jK1oYgQqNOAzFT64fYwmZT0Feilcwwl+\n" +
            "nOIvLcLRAoGBANUVGn59I8vZlm9z3X4tO+d29AR5H2ffdtbDtWM8WmN3C+x10Qud\n" +
            "PypcrZO2bdI1NLsDXFAhDKLCLnj9E2rdbuw2PeBJVefLyYdjvOlfS+pZNL/u2FhM\n" +
            "OqJPf1wkdXOb3hAJrdhm6bhBqmPua7CqMHmIEOShY26G2H99QJQ5Bep/AoGBAMwx\n" +
            "hOp6YBVZnoIj2dqZej4IFEE1BbHK+GZpFYO31q3KdZLbHQIIFU37L+WWXlHF0Qba\n" +
            "OvAlT4iazghTUKtEOEH04Rh/vwNa+TGAn5Ni96bH5LnMlv3WsT3Xgrk1KBgaZAj3\n" +
            "9ZX7XOKqbNhz1VmZU3W6pyNvVK5xDAUS/7aEN8dVAn80b/0VI7arc+CRjPH1Gyyi\n" +
            "yaNDdotEBLo6H++DfFbCI4nLpzLKqOfihOwybbKZCH7xhuIw1fFGHINTQvSEV/n0\n" +
            "J2USzPlXEc+GggT7aXhFM67HjP+wuxGy3913z4EX8kOzrq4ZkznxHjfQX9wgncFQ\n" +
            "JywBF7ZgfM7KkQhTcbUDAoGAbjSPwymXMA61cRG+Y7AP/OjJXcQrNaERXdx7YyLW\n" +
            "d+fcew1NY6pPLU5TtHrqnwG/5g3kX3YTreu3JWEqGbVxE5AB6QIRatMvyVrdRWPV\n" +
            "u6sJNIpKN+gmsvTcte8Nm6yqrvh9EJygrilDI7Oow3nwRIsf6A0PTDLxRM/TOYO0\n" +
            "wx0CgYBr5157xRDem17TX8KVmLqS9RfHd57R9o0n3hs/7GP2sUhaA0ydlHZd4IW7\n" +
            "4UGFT4OEl6aDO8zKC7+IoIoSV01DRQRUdlmsIQCMxet5GF8+8hu7isRl6857coKh\n" +
            "rX8XTdoRM6cq221ghBSD8Ydg6Ikopoe0wB6PFVyD0RlGmTEGNw==\n" +
            "-----END RSA PRIVATE KEY-----");

    /*
    Certificate:
    Data:
        Version: 3 (0x2)
        Serial Number: 152 (0x98)
    Signature Algorithm: sha256WithRSAEncryption
        Issuer: C=US, O=Okta, Inc., OU=Technical Operations, CN=Okta Infrastructure CA
        Validity
            Not Before: Dec  8 22:35:15 2015 GMT
            Not After : Dec 15 22:35:15 2015 GMT
        Subject: C=US, O=Okta, Inc., OU=Technical Operations, CN=dev.homepageservice.okta.com
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
                Modulus:
                    00:a9:f6:08:c8:ea:bd:f2:bf:ca:df:0d:d6:f8:f1:
                    ab:27:08:f2:dc:2a:92:f3:4d:84:41:87:9b:29:d9:
                    b9:0e:6c:87:29:6e:f8:e3:ac:7b:f8:1b:f5:7f:f5:
                    04:f5:aa:70:a0:be:83:32:fa:34:71:45:95:eb:73:
                    80:36:20:5e:e0:e8:59:4a:ab:33:df:89:03:9a:4e:
                    ce:93:84:cb:b3:c7:8a:32:d9:2a:1c:80:d7:23:bf:
                    a7:01:3f:d2:05:00:29:02:68:02:69:d0:24:4e:cf:
                    80:46:b8:bc:bb:d4:ce:f9:bf:d4:3f:72:1d:27:50:
                    58:24:bb:58:8e:d3:f9:e1:a1:94:78:8c:af:7b:5a:
                    4d:b3:9b:f0:fd:e7:26:72:2d:db:5b:f2:9f:bd:10:
                    38:38:31:f4:af:8d:81:c7:c9:5c:b4:71:07:68:27:
                    ea:c2:01:fa:ad:87:1f:ad:0d:1a:4f:ad:03:98:f5:
                    96:dd:3a:d8:74:ea:1e:ef:04:a3:2a:f2:96:a1:94:
                    25:ec:13:0c:88:fc:ad:18:24:b3:9e:ad:0a:41:95:
                    b4:94:a9:c8:d8:dd:56:31:3c:69:91:6c:41:6c:4f:
                    03:d2:c9:33:28:f6:81:cd:20:7c:e8:f2:77:13:b4:
                    e9:b6:30:d7:47:b5:c8:c5:52:65:c0:2e:03:65:88:
                    95:2b
                Exponent: 65537 (0x10001)
        X509v3 extensions:
            X509v3 Basic Constraints:
                CA:FALSE
            X509v3 Key Usage:
                Digital Signature, Non Repudiation, Key Encipherment
            X509v3 CRL Distribution Points:

                Full Name:
                  URI:http://cdp.okta.com/Okta-Infrastructure-CA.crl

            Authority Information Access:
                CA Issuers - URI:http://ca.okta.com/Okta-Infrastructure-CA.crt

    Signature Algorithm: sha256WithRSAEncryption
         9e:96:c6:91:1e:3f:b6:dc:99:06:54:e2:15:7f:aa:97:f3:4b:
         50:dd:21:5f:10:b7:72:1a:3b:d7:77:3f:5e:93:84:30:62:1d:
         95:3c:16:23:e2:f4:00:82:de:b1:7d:1f:04:41:dd:df:0a:2f:
         4e:76:67:44:b9:c6:7c:c5:f0:27:53:ad:d3:02:87:81:69:9d:
         eb:9c:f7:b4:2e:23:d6:a7:cf:24:3e:c4:7a:4a:49:bf:10:b8:
         5d:d3:9c:6d:92:ca:c1:b2:ac:ff:25:43:77:a9:ab:fd:e1:8b:
         08:30:42:4b:88:89:9d:05:37:c8:b7:28:31:f7:05:d1:69:b4:
         c1:8c:02:ad:a2:13:5e:e3:97:ea:a5:90:7a:71:a3:be:51:01:
         27:06:b8:03:25:0d:eb:98:6d:be:20:f4:50:3c:0a:dc:a7:71:
         4a:dc:e1:d3:83:45:ae:ed:c7:ff:d2:29:4a:fb:60:14:6d:c3:
         ef:1d:57:6e:e1:cf:54:29:4c:52:06:f4:ae:a3:39:66:93:3d:
         9d:3b:2d:71:37:df:77:2f:50:f3:ab:9a:6c:a7:4d:ab:ae:df:
         12:b2:8e:de:ec:1c:8a:e8:b2:0b:b7:9a:33:13:fb:15:b4:39:
         2e:15:2f:fe:7e:49:d0:03:f1:a7:00:93:d1:52:2c:a1:6f:e9:
         ae:db:85:73
     */
    private static final X509Certificate SIGNING_CERTIFICATE = X509CertificateUtil.parsePemEncodedCertificateString(
            "-----BEGIN CERTIFICATE-----\n" +
            "MIID7jCCAtagAwIBAgICAJgwDQYJKoZIhvcNAQELBQAwYjELMAkGA1UEBhMCVVMx\n" +
            "EzARBgNVBAoMCk9rdGEsIEluYy4xHTAbBgNVBAsMFFRlY2huaWNhbCBPcGVyYXRp\n" +
            "b25zMR8wHQYDVQQDDBZPa3RhIEluZnJhc3RydWN0dXJlIENBMB4XDTE1MTIwODIy\n" +
            "MzUxNVoXDTE1MTIxNTIyMzUxNVowaDELMAkGA1UEBhMCVVMxEzARBgNVBAoMCk9r\n" +
            "dGEsIEluYy4xHTAbBgNVBAsMFFRlY2huaWNhbCBPcGVyYXRpb25zMSUwIwYDVQQD\n" +
            "DBxkZXYuaG9tZXBhZ2VzZXJ2aWNlLm9rdGEuY29tMIIBIjANBgkqhkiG9w0BAQEF\n" +
            "AAOCAQ8AMIIBCgKCAQEAqfYIyOq98r/K3w3W+PGrJwjy3CqS802EQYebKdm5DmyH\n" +
            "KW7446x7+Bv1f/UE9apwoL6DMvo0cUWV63OANiBe4OhZSqsz34kDmk7Ok4TLs8eK\n" +
            "MtkqHIDXI7+nAT/SBQApAmgCadAkTs+ARri8u9TO+b/UP3IdJ1BYJLtYjtP54aGU\n" +
            "eIyve1pNs5vw/ecmci3bW/KfvRA4ODH0r42Bx8lctHEHaCfqwgH6rYcfrQ0aT60D\n" +
            "mPWW3TrYdOoe7wSjKvKWoZQl7BMMiPytGCSznq0KQZW0lKnI2N1WMTxpkWxBbE8D\n" +
            "0skzKPaBzSB86PJ3E7TptjDXR7XIxVJlwC4DZYiVKwIDAQABo4GnMIGkMAkGA1Ud\n" +
            "EwQCMAAwCwYDVR0PBAQDAgXgMD8GA1UdHwQ4MDYwNKAyoDCGLmh0dHA6Ly9jZHAu\n" +
            "b2t0YS5jb20vT2t0YS1JbmZyYXN0cnVjdHVyZS1DQS5jcmwwSQYIKwYBBQUHAQEE\n" +
            "PTA7MDkGCCsGAQUFBzAChi1odHRwOi8vY2Eub2t0YS5jb20vT2t0YS1JbmZyYXN0\n" +
            "cnVjdHVyZS1DQS5jcnQwDQYJKoZIhvcNAQELBQADggEBAJ6WxpEeP7bcmQZU4hV/\n" +
            "qpfzS1DdIV8Qt3IaO9d3P16ThDBiHZU8FiPi9ACC3rF9HwRB3d8KL052Z0S5xnzF\n" +
            "8CdTrdMCh4Fpneuc97QuI9anzyQ+xHpKSb8QuF3TnG2SysGyrP8lQ3epq/3hiwgw\n" +
            "QkuIiZ0FN8i3KDH3BdFptMGMAq2iE17jl+qlkHpxo75RAScGuAMlDeuYbb4g9FA8\n" +
            "CtyncUrc4dODRa7tx//SKUr7YBRtw+8dV27hz1QpTFIG9K6jOWaTPZ07LXE333cv\n" +
            "UPOrmmynTauu3xKyjt7sHIrosgu3mjMT+xW0OS4VL/5+SdAD8acAk9FSLKFv6a7b\n" +
            "hXM=\n" +
            "-----END CERTIFICATE-----",
            SECURITY_PROVIDER);


    private final UserInfo jane = new UserInfo(1, "Jane Doe");
    private final UserInfo john = new UserInfo(2, "John Doe");

    @ClassRule
    public static final DropwizardAppRule<UserInfoConfiguration> RULE =
            new DropwizardAppRule<>(UserInfoApplication.class, resourceFilePath("dropwizard/valid-conf.yml"));

    @Test
    public void userInfoReturnsAllUsersInOrder() throws Exception {
        Client client = createApiClient();
        JwtIssuer jwtIssuer = new JwtIssuer(PRIVATE_KEY, SIGNING_CERTIFICATE);
        Date now = new Date();

        JwtClaims claims = JwtClaims.JwtClaimsBuilder.newInstance()
                .addJwtId(UUID.randomUUID().toString())
                .addSubject("userId")
                .addNotBefore(now)
                .addIssuedAt(now)
                .addExpirationTime(new Date(now.getTime() + 10000)) // 10 seconds
                .addIssuer("test_issuer")
                .addCustomClaim("orgId", "theOrgId")
                .addCustomClaim("version", 1)
                .addCustomClaim("scope", "ADMIN")
                .build();

        String jwt = jwtIssuer.issueToken(claims);
        LOG.debug(jwt);

        List<UserInfo> users = client.target(String.format("http://localhost:%d/users", RULE.getLocalPort()))
                .request()
                .header("Authorization", "Bearer " + jwt)
                .get(new GenericType<List<UserInfo>>() { });

        assertThat(users).containsExactly(jane, john);
    }

    private Client createApiClient() {
        JerseyClientConfiguration config = new JerseyClientConfiguration();
        config.setTimeout(Duration.seconds(30));

        return new JerseyClientBuilder(RULE.getEnvironment())
                .using(config)
                .build("user info test client");
    }

    private static RSAPrivateKey parsePrivateKeyPemString(String privateKey) {
        final PEMParser pemParser = new PEMParser(new StringReader(privateKey));
        try {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            KeyPair keyPair = converter.getKeyPair((PEMKeyPair) pemParser.readObject());
            return (RSAPrivateKey) keyPair.getPrivate();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
