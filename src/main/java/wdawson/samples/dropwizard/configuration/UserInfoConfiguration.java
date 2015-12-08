package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

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

    @JsonProperty
    public String getNamesResource() {
        return namesResource;
    }

    @JsonProperty
    public void setNamesResource(String names) {
        this.namesResource = names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInfoConfiguration that = (UserInfoConfiguration) o;
        return Objects.equals(namesResource, that.namesResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namesResource);
    }
}
