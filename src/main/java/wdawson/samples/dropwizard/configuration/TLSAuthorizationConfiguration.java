package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Objects;

/**
 * Configuration for TLS Authorization
 *
 * @author wdawson
 */
public class TLSAuthorizationConfiguration {

    @NotEmpty
    @JsonProperty
    private String allowedClientDnRegex;

    public String getAllowedClientDnRegex() {
        return allowedClientDnRegex;
    }

    public void setAllowedClientDnRegex(String allowedClientDnRegex) {
        this.allowedClientDnRegex = allowedClientDnRegex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TLSAuthorizationConfiguration that = (TLSAuthorizationConfiguration) o;
        return Objects.equals(allowedClientDnRegex, that.allowedClientDnRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allowedClientDnRegex);
    }
}
