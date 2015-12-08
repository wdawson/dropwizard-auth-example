package wdawson.samples.dropwizard.auth;

import java.security.Principal;
import java.util.Set;

/**
 * @author Jon Todd
 */
public final class User implements Principal {

    private final String id;
    private final String orgId;
    private final Set<String> roles;

    /*
     * Constructors
     */

    private User(Builder builder) {
        id = builder.id;
        orgId = builder.orgId;
        roles = builder.roles;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(User copy) {
        Builder builder = new Builder();
        builder.id = copy.id;
        builder.orgId = copy.orgId;
        builder.roles = copy.roles;
        return builder;
    }

    /*
     * Implement Principal interface
     */

    @Override
    public String getName() {
        return id;
    }

    /*
     * Getters
     */

    public String getId() {
        return id;
    }

    public String getOrgId() {
        return orgId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    /*
     * Builder
     */

    public static final class Builder {
        private String id;
        private String orgId;
        private Set<String> roles;

        private Builder() {
        }

        public Builder withId(String val) {
            id = val;
            return this;
        }

        public Builder withOrgId(String val) {
            orgId = val;
            return this;
        }

        public Builder withRoles(Set<String> val) {
            roles = val;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
