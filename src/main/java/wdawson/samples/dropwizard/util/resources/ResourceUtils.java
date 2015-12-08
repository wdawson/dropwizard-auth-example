package wdawson.samples.dropwizard.util.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Contains utility methods for reading classpath resources
 *
 * @author wdawson
 */
public class ResourceUtils {

    /**
     * Reads the lines from a resource given it's location on the classpath
     *
     * @param resourceName The location of the resource on the classpath
     * @return The lines in the resource as a List of Strings
     * @throws IllegalArgumentException If the resource could not be found or parsed
     */
    public static List<String> readLinesFromResource(String resourceName) {
        URL namesResourceURL = Resources.getResource(resourceName);
        try {
            return Resources.readLines(namesResourceURL, Charsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Some I/O error occurred when reading " + resourceName);
        }
    }

}
