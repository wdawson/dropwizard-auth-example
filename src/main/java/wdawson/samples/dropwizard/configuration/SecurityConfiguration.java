package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Configuration for security
 *
 * @author wdawson
 */
public class SecurityConfiguration {

    @Valid
    @NotNull
    @JsonProperty
    private TLSAuthorizationConfiguration tlsAuthZ;

    @Valid
    @NotNull
    @JsonProperty
    private JwtVerificationConfiguration jwtVerification;

    public TLSAuthorizationConfiguration getTlsAuthZ() {
        return tlsAuthZ;
    }

    public void setTlsAuthZ(TLSAuthorizationConfiguration tlsAuthZ) {
        this.tlsAuthZ = tlsAuthZ;
    }

    public JwtVerificationConfiguration getJwtVerification() {
        return jwtVerification;
    }

    public void setJwtVerification(JwtVerificationConfiguration jwtVerification) {
        this.jwtVerification = jwtVerification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecurityConfiguration that = (SecurityConfiguration) o;
        return Objects.equals(tlsAuthZ, that.tlsAuthZ) &&
                Objects.equals(jwtVerification, that.jwtVerification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tlsAuthZ, jwtVerification);
    }
}
