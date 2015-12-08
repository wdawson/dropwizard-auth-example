package wdawson.samples.dropwizard.util.resources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Handler for the "classpath" protocol in a URL
 *
 * @author wdawson
 */
public class ClasspathURLStreamHandler extends URLStreamHandler {

    public static final String PROTOCOL = "classpath";

    private ClassLoader classLoader;

    public ClasspathURLStreamHandler() {
        this.classLoader = getClass().getClassLoader();
    }

    public ClasspathURLStreamHandler(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        final URL resourceUrl = classLoader.getResource(u.getPath());
        if (resourceUrl == null) {
            throw new FileNotFoundException("Could not find resource on the classpath: " + u);
        }
        return resourceUrl.openConnection();
    }
}
