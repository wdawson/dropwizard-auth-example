package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Configuration for this sample application
 *
 * @author wdawson
 */
public class UserInfoConfiguration extends Configuration {

    /**
     * Resource on the classpath containing names of users
     */
    @NotEmpty
    private String namesResource;

    @Valid
    @NotNull
    @JsonProperty("security")
    private SecurityConfiguration securityConfiguration;

    @JsonProperty
    public String getNamesResource() {
        return namesResource;
    }

    @JsonProperty
    public void setNamesResource(String names) {
        this.namesResource = names;
    }

    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(namesResource, securityConfiguration);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final UserInfoConfiguration other = (UserInfoConfiguration) obj;
        return Objects.equals(this.namesResource, other.namesResource)
                && Objects.equals(this.securityConfiguration, other.securityConfiguration);
    }
}
