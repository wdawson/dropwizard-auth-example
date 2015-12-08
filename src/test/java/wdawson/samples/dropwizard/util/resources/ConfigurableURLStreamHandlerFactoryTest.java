package wdawson.samples.dropwizard.util.resources;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class ConfigurableURLStreamHandlerFactoryTest {

    private static final ImmutableList<String> STANDARD_PROTOCOLS = ImmutableList.of(
            "http",
            "https",
            "ftp",
            "file",
            "jar"
    );

    @Test
    public void testThatDefaultJavaHandlersCanBeExtractedForKnownProtocols() {
        STANDARD_PROTOCOLS.forEach(
                protocol -> assertThat(ConfigurableURLStreamHandlerFactory.getURLStreamHandler(protocol)).isNotNull()
        );
    }

    @Test
    public void testThatStandardHandlersAreAddedConveniently() {
        final ConfigurableURLStreamHandlerFactory factory = new ConfigurableURLStreamHandlerFactory();
        STANDARD_PROTOCOLS.forEach(protocol -> assertThat(factory.createURLStreamHandler(protocol)).isNull());

        factory.withStandardJavaHandlers();
        STANDARD_PROTOCOLS.forEach(protocol -> assertThat(factory.createURLStreamHandler(protocol)).isNotNull());
    }

    @Test
    public void testThatFactoryIsConfigurableViaConstructor() {
        final ConfigurableURLStreamHandlerFactory factory = new ConfigurableURLStreamHandlerFactory(
                ClasspathURLStreamHandler.PROTOCOL,
                new ClasspathURLStreamHandler()
        );
        assertThat(factory.createURLStreamHandler(ClasspathURLStreamHandler.PROTOCOL)).isNotNull();
    }

    @Test
    public void testThatFactoryIsConfigurableViaMethod() {
        final ConfigurableURLStreamHandlerFactory factory = new ConfigurableURLStreamHandlerFactory();
        assertThat(factory.createURLStreamHandler(ClasspathURLStreamHandler.PROTOCOL)).isNull();

        factory.withHandler(ClasspathURLStreamHandler.PROTOCOL, new ClasspathURLStreamHandler());
        assertThat(factory.createURLStreamHandler(ClasspathURLStreamHandler.PROTOCOL)).isNotNull();
    }
}
