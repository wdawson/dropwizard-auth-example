package wdawson.samples.dropwizard.configuration;

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
    private DataConfiguration data;

    public DataConfiguration getData() {
        return data;
    }

    public void setData(DataConfiguration data) {
        this.data = data;
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
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
