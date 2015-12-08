package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Objects;

/**
 * Configuration for the data source
 *
 * @author wdawson
 */
public class DataConfiguration {

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
        DataConfiguration that = (DataConfiguration) o;
        return Objects.equals(namesResource, that.namesResource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namesResource);
    }
}
