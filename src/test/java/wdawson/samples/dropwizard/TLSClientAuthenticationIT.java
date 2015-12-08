package wdawson.samples.dropwizard;

import io.dropwizard.client.JerseyClientBuilder;
import org.apache.http.NoHttpResponseException;
import org.junit.Test;
import wdawson.samples.dropwizard.api.UserInfo;
import wdawson.samples.dropwizard.helpers.IntegrationTest;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.SocketException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * @author wdawson
 */
public class TLSClientAuthenticationIT extends IntegrationTest {

    @Test
    public void httpClientIsRefused() {
        Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("http client");

        try {
            client.target(String.format("http://localhost:%d/users", 8443))
                    .request()
                    .get(new GenericType<List<UserInfo>>() {
                    });
            failBecauseExceptionWasNotThrown(ProcessingException.class);
        } catch (ProcessingException e) {
            assertThat(e).hasCauseExactlyInstanceOf(NoHttpResponseException.class);
        }
    }

    @Test
    public void thirdPartyHttpsClientIsRefused() throws Exception {
        Client client = getNewSecureClient("tls/third-party-service-keystore.jks");

        try {
            client.target(String.format("https://localhost:%d/users", 8443))
                    .request()
                    .get(new GenericType<List<UserInfo>>() {
                    });
            failBecauseExceptionWasNotThrown(ProcessingException.class);
        } catch (ProcessingException e) {
            assertThat(e).hasCauseExactlyInstanceOf(SocketException.class);
            assertThat(e).hasMessageEndingWith("Connection reset");
        }
    }

    @Test
    public void validHttpsClientIsAllowed() throws Exception{
        Client client = getNewSecureClient();

        Response response = client.target(String.format("https://localhost:%d/users", 8443))
                .request()
                .get();

        assertThat(response.getStatus()).isBetween(200, 299);

        response.close();
    }
}
