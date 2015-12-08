package wdawson.samples.dropwizard.util.jwt;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import wdawson.samples.dropwizard.util.x509.X509CertificateUtil;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * @author cbarbara
 */
public class JwtIssuerBuilder {

    public static JwtIssuer createFromRsaPrivateKeyStrings(String rsaPrivateKeySigningString, String signingCertificateString)
            throws IOException, CertificateException, InvalidKeySpecException, NoSuchAlgorithmException {
        Provider securityProvider = new BouncyCastleProvider();
        Security.addProvider(securityProvider);
        return new JwtIssuer(getRsaSigningKey(rsaPrivateKeySigningString), getSigningCertificate(signingCertificateString, securityProvider));
    }

    private static X509Certificate getSigningCertificate(String signingCertString, Provider securityProvider) throws IOException, CertificateException {
        return X509CertificateUtil.parsePemEncodedCertificateString(signingCertString, securityProvider);
    }

    private static RSAPrivateKey getRsaSigningKey(String signingKeyString) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        final PemReader pemReader = new PemReader(new StringReader(signingKeyString));
        final PemObject pemObject = pemReader.readPemObject();
        if (pemObject == null) {
            throw new IllegalArgumentException("Couldn't parse the provided signing key. Ensure it's a" +
                    "valid RSA private key");
        }
        if (!"RSA PRIVATE KEY".equals(pemObject.getType())) {
            throw new IllegalArgumentException(String.format("Expected the provided signing key to be of type 'RSA " +
                    "PRIVATE KEY' but was '%s'", pemObject.getType()));
        }
        final KeySpec keySpec = new PKCS8EncodedKeySpec(pemObject.getContent());
        final KeyFactory factory = KeyFactory.getInstance("RSA");
        RSAPrivateKey key = (RSAPrivateKey) factory.generatePrivate(keySpec);
        return key;
    }
}
