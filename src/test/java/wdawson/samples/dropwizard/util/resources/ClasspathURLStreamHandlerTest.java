package wdawson.samples.dropwizard.util.resources;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author wdawson
 */
public class ClasspathURLStreamHandlerTest {

    private static ClasspathURLStreamHandler classpathURLStreamHandler = new ClasspathURLStreamHandler();

    @BeforeClass
    public static void setup() throws Exception {
        ConfigurableURLStreamHandlerFactory urlHandlerFactory = new ConfigurableURLStreamHandlerFactory()
                .withHandler(ClasspathURLStreamHandler.PROTOCOL, classpathURLStreamHandler);
        URL.setURLStreamHandlerFactory(urlHandlerFactory);
    }

    @AfterClass
    public static void teardown() throws Exception {
        ConfigurableURLStreamHandlerFactory.unsetURLStringHandlerFactory();
    }

    @Test
    public void testThatGetResourceOnTheClasspathWorksWithGoodURL() throws IOException {
        URLConnection connection = classpathURLStreamHandler.openConnection(new URL("classpath:fixtures/hello.txt"));

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        String line = bufferedReader.readLine().trim();

        assertThat(line).isEqualTo("Hello World!");
    }

    @Test(expected = FileNotFoundException.class)
    public void testThatGetResourceOnTheClasspathThrowsWhenNoResourceIsFound() throws IOException {
        classpathURLStreamHandler.openConnection(new URL("classpath:fixtures/goodbye.txt"));
    }
}
