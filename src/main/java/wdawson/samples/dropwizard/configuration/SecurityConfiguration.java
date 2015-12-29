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

    public TLSAuthorizationConfiguration getTlsAuthZ() {
        return tlsAuthZ;
    }

    public void setTlsAuthZ(TLSAuthorizationConfiguration tlsAuthZ) {
        this.tlsAuthZ = tlsAuthZ;
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
        return Objects.equals(tlsAuthZ, that.tlsAuthZ);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tlsAuthZ);
    }
}
