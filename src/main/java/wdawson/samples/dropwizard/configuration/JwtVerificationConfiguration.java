package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import wdawson.samples.dropwizard.util.jwt.JwtVerifier;
import wdawson.samples.dropwizard.util.x509.X509CertificateUtil;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Jon Todd
 */
public class JwtVerificationConfiguration {

    private static final Provider SECURITY_PROVIDER = new BouncyCastleProvider();

    @NotNull
    @JsonProperty
    private String allowedSignerDnRegex;

    @NotNull
    @JsonProperty
    private Duration certificateCacheExpireTime;

    @NotNull
    @JsonProperty
    private String certificateRevocationChecking;

    @NotNull
    @JsonProperty
    private List<String> trustedRootCaCertificates;

    public JwtVerifier newInstanceFromConfig() {
        // Register Bouncycastle as a SecurityProvider
        Security.addProvider(SECURITY_PROVIDER);

        try {
            return new JwtVerifier(Pattern.compile(this.getAllowedSignerDnRegex()),
                    X509CertificateUtil.parsePemEncodedCertificates(this.getTrustedRootCaCertificates(), SECURITY_PROVIDER));
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAllowedSignerDnRegex() {
        return allowedSignerDnRegex;
    }

    public void setAllowedSignerDnRegex(String allowedSignerDnRegex) {
        this.allowedSignerDnRegex = allowedSignerDnRegex;
    }

    public Duration getCertificateCacheExpireTime() {
        return certificateCacheExpireTime;
    }

    public void setCertificateCacheExpireTime(Duration certificateCacheExpireTime) {
        this.certificateCacheExpireTime = certificateCacheExpireTime;
    }

    public String getCertificateRevocationChecking() {
        return certificateRevocationChecking;
    }

    public void setCertificateRevocationChecking(String certificateRevocationChecking) {
        this.certificateRevocationChecking = certificateRevocationChecking;
    }

    public List<String> getTrustedRootCaCertificates() {
        return trustedRootCaCertificates;
    }

    public void setTrustedRootCaCertificates(List<String> trustedRootCaCertificates) {
        this.trustedRootCaCertificates = trustedRootCaCertificates;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedSignerDnRegex, certificateCacheExpireTime, certificateRevocationChecking,
                trustedRootCaCertificates);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final JwtVerificationConfiguration other = (JwtVerificationConfiguration) obj;
        return Objects.equals(this.allowedSignerDnRegex, other.allowedSignerDnRegex)
                && Objects.equals(this.certificateCacheExpireTime, other.certificateCacheExpireTime)
                && Objects.equals(this.certificateRevocationChecking, other.certificateRevocationChecking)
                && Objects.equals(this.trustedRootCaCertificates, other.trustedRootCaCertificates);
    }
}
