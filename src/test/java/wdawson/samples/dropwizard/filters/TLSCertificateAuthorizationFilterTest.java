package wdawson.samples.dropwizard.filters;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static wdawson.samples.dropwizard.filters.TLSCertificateAuthorizationFilter.X509_CERTIFICATE_ATTRIBUTE;

/**
 * @author wdawson
 */
@RunWith(MockitoJUnitRunner.class)
public class TLSCertificateAuthorizationFilterTest {

    private static final char[] PASSPHRASE = "notsecret".toCharArray();

    private static final String REG_EX = "^.*\\bCN=homepage-service\\b(?:,.*|\\s*)$";

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private ContainerRequestContext requestContext;

    private static X509Certificate[] homepageCertificateChain;
    private static X509Certificate[] eventCertificateChain;

    @BeforeClass
    public static void setupClass() throws Exception {
        homepageCertificateChain = getCertificateChainFromKeyStore("tls/homepage-service-keystore.jks");
        eventCertificateChain = getCertificateChainFromKeyStore("tls/event-service-keystore.jks");
    }

    @After
    public void teardown() {
        reset(servletRequest, requestContext);
    }

    private static X509Certificate[] getCertificateChainFromKeyStore(String keyStoreResource) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(resourceFilePath(keyStoreResource)), PASSPHRASE);
        Certificate[] certificateChain = keyStore.getCertificateChain("service-tls");

        X509Certificate[] x509CertificateChain = new X509Certificate[certificateChain.length];

        for (int i = 0; i < certificateChain.length; ++i) {
            assertThat(certificateChain[i]).isInstanceOf(X509Certificate.class);
            x509CertificateChain[i] = (X509Certificate) certificateChain[i];
        }
        return x509CertificateChain;
    }

    @Test
    public void filterPassesWhenSubjectMatchesRegex() throws Exception {
        TLSCertificateAuthorizationFilter sut = new TLSCertificateAuthorizationFilter(REG_EX, servletRequest);

        when(servletRequest.getAttribute(X509_CERTIFICATE_ATTRIBUTE)).thenReturn(homepageCertificateChain);

        sut.filter(requestContext);

        verifyZeroInteractions(requestContext);
    }

    @Test
    public void filterAbortsWhenBadSubjectIsFound() throws Exception {
        TLSCertificateAuthorizationFilter sut = new TLSCertificateAuthorizationFilter(REG_EX, servletRequest);

        when(servletRequest.getAttribute(X509_CERTIFICATE_ATTRIBUTE)).thenReturn(eventCertificateChain);

        sut.filter(requestContext);

        assertThatRequestAbortedWithMessage("Certificate subject is not recognized");
    }

    @Test
    public void filterAbortsWhenCertificateChainIsNull() throws Exception {
        TLSCertificateAuthorizationFilter sut = new TLSCertificateAuthorizationFilter(REG_EX, servletRequest);

        when(servletRequest.getAttribute(X509_CERTIFICATE_ATTRIBUTE)).thenReturn(null);

        sut.filter(requestContext);

        assertThatRequestAbortedWithMessage("No certificate chain found");
    }

    @Test
    public void filterAbortsWhenCertificateChainIsEmpty() throws Exception {
        TLSCertificateAuthorizationFilter sut = new TLSCertificateAuthorizationFilter(REG_EX, servletRequest);

        when(servletRequest.getAttribute(X509_CERTIFICATE_ATTRIBUTE)).thenReturn(new X509Certificate[] {});

        sut.filter(requestContext);

        assertThatRequestAbortedWithMessage("No certificate chain found");
    }

    @Test
    public void filterAbortsWhenCertificateChainHasNullEntry() throws Exception {
        TLSCertificateAuthorizationFilter sut = new TLSCertificateAuthorizationFilter(REG_EX, servletRequest);

        when(servletRequest.getAttribute(X509_CERTIFICATE_ATTRIBUTE)).thenReturn(new X509Certificate[] {null});

        sut.filter(requestContext);

        assertThatRequestAbortedWithMessage("No certificate chain found");
    }

    private void assertThatRequestAbortedWithMessage(String expectedMessage) {
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(requestContext).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());

        assertThat(response.getEntity()).isInstanceOf(String.class);
        String responseMessage = (String) response.getEntity();
        assertThat(responseMessage).isEqualTo(expectedMessage);
    }
}
