package wdawson.samples.dropwizard.util.x509;

import com.google.common.collect.ImmutableList;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

/**
 * @author Jon Todd
 */
public class X509CertificateUtil {
    public static X509Certificate parsePemEncodedCertificateString(String certificateString, Provider securityProvider) {
        try {
            PemReader pemReader = new PemReader(new StringReader(certificateString));
            PemObject pemObject = pemReader.readPemObject();
            if (pemObject == null) {
                throw new RuntimeException("PEM parser returned a null result. Ensure your certificate is PEM encoded");

            }
            if (!"CERTIFICATE".equals(pemObject.getType())) {
                throw new RuntimeException(String.format(
                        "Expected encoded root CA certificate to be of type 'CERTIFICATE' but was '%s'.",
                        pemObject.getType()));
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509", securityProvider);
            InputStream is = new ByteArrayInputStream(pemObject.getContent());
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(is);

            if (certificate == null) {
                throw new RuntimeException("Got null x509certificate from parser. Is certificate in valid " +
                        "PEM encoded format?");
            }

            return certificate;
        } catch (CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<X509Certificate> parsePemEncodedCertificates(Collection<String> encodeCertificates,
                                                                    Provider securityProvider)
            throws IOException, CertificateException {

        ImmutableList.Builder<X509Certificate> certificates = ImmutableList.builder();

        for (String encodedCertificate : encodeCertificates) {
            certificates.add(parsePemEncodedCertificateString(encodedCertificate, securityProvider));
        }

        return certificates.build();
    }
}
