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
    private String dnRegex;

    public String getDnRegex() {
        return dnRegex;
    }

    public void setDnRegex(String dnRegex) {
        this.dnRegex = dnRegex;
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
        return Objects.equals(dnRegex, that.dnRegex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dnRegex);
    }
}
