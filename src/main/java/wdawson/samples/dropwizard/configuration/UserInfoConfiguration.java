package wdawson.samples.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Configuration for this sample application
 *
 * @author wdawson
 */
public class UserInfoConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty
    private DataConfiguration data;

    @Valid
    @NotNull
    @JsonProperty
    private SecurityConfiguration security;

    public DataConfiguration getData() {
        return data;
    }

    public void setData(DataConfiguration data) {
        this.data = data;
    }

    public SecurityConfiguration getSecurity() {
        return security;
    }

    public void setSecurity(SecurityConfiguration security) {
        this.security = security;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserInfoConfiguration that = (UserInfoConfiguration) o;
        return Objects.equals(data, that.data) &&
                Objects.equals(security, that.security);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, security);
    }
}
