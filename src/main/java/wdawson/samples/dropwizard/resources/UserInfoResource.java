package wdawson.samples.dropwizard.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.dropwizard.auth.Auth;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.auth.Role;
import wdawson.samples.dropwizard.auth.User;
import wdawson.samples.dropwizard.util.resources.ResourceUtils;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedList;
import java.util.List;

/**
 * Resource for user info
 *
 * @author wdawson
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserInfoResource {

    private final List<String> names;

    public UserInfoResource(String namesResource) {
        names = ResourceUtils.readLinesFromResource(namesResource);
    }

    /**
     * Gets the info about a user given their ID
     *
     * @param id The id of the user to fetch
     * @return The user
     */
    @GET
    @Path(("/{id}"))
    @Timed
    @RolesAllowed(Role.USER_READ_ONLY)
    public UserInfo getUser(@Min(1) @PathParam("id") long id,
                            @Auth User user) {
        Preconditions.checkArgument(user.getId().equals(String.valueOf(id)));

        if (id > names.size()) {
            throw new NotFoundException("User with id=" + id + " was not found");
        }

        int userIndex = (int) (id - 1);
        return new UserInfo(id, names.get(userIndex));
    }

    /**
     * Gets the info about all users in the system
     *
     * @return The list of users
     */
    @GET
    @Timed
    @RolesAllowed(Role.ADMIN)
    public List<UserInfo> getUsers() {
        List<UserInfo> result = new LinkedList<>();
        for (int i = 1; i <= names.size(); ++i) {
            result.add(new UserInfo(i, names.get(i - 1)));
        }
        return result;
    }

    @VisibleForTesting
    public List<String> getNames() {
        return names;
    }
}
