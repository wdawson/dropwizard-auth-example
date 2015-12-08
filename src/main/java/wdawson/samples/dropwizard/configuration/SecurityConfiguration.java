package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author Jon Todd
 */
public class SecurityConfiguration {

    @Valid
    @NotNull
    @JsonProperty("jwtVerification")
    private JwtVerificationConfiguration jwtVerificationConfiguration;

    public JwtVerificationConfiguration getJwtVerificationConfiguration() {
        return jwtVerificationConfiguration;
    }

    public void setJwtVerificationConfiguration(JwtVerificationConfiguration jwtVerificationConfiguration) {
        this.jwtVerificationConfiguration = jwtVerificationConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(jwtVerificationConfiguration);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SecurityConfiguration other = (SecurityConfiguration) obj;
        return Objects.equals(this.jwtVerificationConfiguration, other.jwtVerificationConfiguration);
    }
}
