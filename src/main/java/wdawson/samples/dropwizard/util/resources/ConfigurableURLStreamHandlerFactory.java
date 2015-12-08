package wdawson.samples.dropwizard.util.resources;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * A configurable URLStreamHandlerFactory. This class allows for customized protocols
 * to be added and recognized by the {@link URL} class.
 *
 * @author wdawson
 */
public class ConfigurableURLStreamHandlerFactory implements URLStreamHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableURLStreamHandlerFactory.class);

    private final Map<String, URLStreamHandler> protocolHandlers;

    public ConfigurableURLStreamHandlerFactory() {
        this.protocolHandlers = new HashMap<>();
    }

    public ConfigurableURLStreamHandlerFactory(String protocol, URLStreamHandler handler) {
        this.protocolHandlers = new HashMap<>();
        this.protocolHandlers.put(protocol, handler);
    }

    /**
     * Adds a handler for the given protocol to the known URLStreamHandlers
     *
     * @param protocol The protocol
     * @param handler The handler that handles the protocol
     * @return The factory object
     */
    public ConfigurableURLStreamHandlerFactory withHandler(String protocol, URLStreamHandler handler) {
        this.protocolHandlers.put(protocol, handler);
        return this;
    }

    /**
     * Sets the standard java handlers to the their appropriate protocols on the factory.
     *
     * Standard protocols are:
     *
     * {@code http, https, ftp, file, and jar}
     *
     * @return The factory
     */
    public ConfigurableURLStreamHandlerFactory withStandardJavaHandlers() {
        // Get default java handlers for known protocols
        return this.withHandler("http" , ConfigurableURLStreamHandlerFactory.getURLStreamHandler("http"))
                .withHandler("https", ConfigurableURLStreamHandlerFactory.getURLStreamHandler("https"))
                .withHandler("ftp"  , ConfigurableURLStreamHandlerFactory.getURLStreamHandler("ftp"))
                .withHandler("file" , ConfigurableURLStreamHandlerFactory.getURLStreamHandler("file"))
                .withHandler("jar"  , ConfigurableURLStreamHandlerFactory.getURLStreamHandler("jar"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return this.protocolHandlers.get(protocol);
    }

    /**
     * Finds the handler of a specified protocol that the {@link URL} class will use.
     *
     * This method is useful before setting an instance of this class as the factory for the {@link URL} to supplement
     * any configured protocols with java defaults. Guaranteed protocols to exist before setting a new factory are:
     *
     * {@code http, https, ftp, file, and jar}
     *
     * @param protocol The protocol to find a handler for
     * @return the handler for the protocol
     */
    public static URLStreamHandler getURLStreamHandler(String protocol) {
        try {
            // Use reflection on a package private method (hopefully less likely to change in releases of java)
            // There is a unit test for this as well, so we will know if this method changes.
            Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
            method.setAccessible(true);
            return (URLStreamHandler) method.invoke(null, protocol);
        } catch (Exception e) {
            LOGGER.warn("Tried to get handler for the {} protocol but failed", protocol, e);
            return null;
        }
    }

    /**
     * Clear factory from URL class so that it can be set again later.
     *
     * @throws NoSuchFieldException If reflection fails (should not happen)
     * @throws IllegalAccessException If reflection fails (should not happen)
     */
    @VisibleForTesting
    public static void unsetURLStringHandlerFactory() throws NoSuchFieldException, IllegalAccessException {
        // We have to use reflection since java doesn't let you do this normally

        Field f = URL.class.getDeclaredField("factory");
        f.setAccessible(true);
        f.set(null, null);
        URL.setURLStreamHandlerFactory(null);
    }
}
