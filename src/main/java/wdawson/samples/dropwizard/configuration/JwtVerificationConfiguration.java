package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import org.hibernate.validator.constraints.NotEmpty;
import wdawson.samples.dropwizard.util.jwt.JwtVerifier;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Jon Todd
 */
public class JwtVerificationConfiguration {

    @NotNull
    @JsonProperty
    private String allowedSignerDnRegex;

    @NotEmpty
    @JsonProperty
    private String truststoreResourcePath;

    @NotEmpty
    @JsonProperty
    private String truststorePassphrase;

    @NotEmpty
    @JsonProperty
    private List<String> truststoreAliases;

    public JwtVerifier newInstanceFromConfig() {
        List<X509Certificate> trustedCertificates = new LinkedList<>();
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(Resources.getResource(getTruststoreResourcePath()).openStream(),
                    getTruststorePassphrase().toCharArray());
            for(String alias : getTruststoreAliases()) {
                if (trustStore.isCertificateEntry(alias)) {
                    trustedCertificates.add((X509Certificate) trustStore.getCertificate(alias));
                } else {
                    throw new IllegalArgumentException("Alias not a certificate entry: " + alias);
                }
            }
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException(e);
        }

        return new JwtVerifier(getAllowedSignerDnRegex(), trustedCertificates);
    }

    public String getAllowedSignerDnRegex() {
        return allowedSignerDnRegex;
    }

    public void setAllowedSignerDnRegex(String allowedSignerDnRegex) {
        this.allowedSignerDnRegex = allowedSignerDnRegex;
    }

    public String getTruststoreResourcePath() {
        return truststoreResourcePath;
    }

    public void setTruststoreResourcePath(String truststoreResourcePath) {
        this.truststoreResourcePath = truststoreResourcePath;
    }

    public String getTruststorePassphrase() {
        return truststorePassphrase;
    }

    public void setTruststorePassphrase(String truststorePassphrase) {
        this.truststorePassphrase = truststorePassphrase;
    }

    public List<String> getTruststoreAliases() {
        return truststoreAliases;
    }

    public void setTruststoreAliases(List<String> truststoreAliases) {
        this.truststoreAliases = truststoreAliases;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        JwtVerificationConfiguration other = (JwtVerificationConfiguration) obj;
        return Objects.equals(this.allowedSignerDnRegex, other.allowedSignerDnRegex) &&
                Objects.equals(this.truststoreResourcePath, other.truststoreResourcePath) &&
                Objects.equals(this.truststorePassphrase, other.truststorePassphrase) &&
                Objects.equals(this.truststoreAliases, other.truststoreAliases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedSignerDnRegex, truststoreResourcePath, truststorePassphrase, truststoreAliases);
    }
}
