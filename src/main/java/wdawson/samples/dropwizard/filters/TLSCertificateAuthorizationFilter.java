package wdawson.samples.dropwizard.filters;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

/**
 * Filter for doing additional authorization on top of TLS Client Authentication by checking the certificate
 *
 * @author wdawson
 */
@Priority(Priorities.AUTHORIZATION)
@PreMatching
public class TLSCertificateAuthorizationFilter implements ContainerRequestFilter {

    @VisibleForTesting
    static final String X509_CERTIFICATE_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    // Although this is a class level field, Jersey actually injects a proxy
    // which is able to simultaneously serve multiple requests.
    @Context
    private HttpServletRequest servletRequest;

    private final Pattern dnRegex;

    public TLSCertificateAuthorizationFilter(String dnRegex) {
        this.dnRegex = Pattern.compile(dnRegex);
    }

    @VisibleForTesting
    TLSCertificateAuthorizationFilter(String dnRegex, HttpServletRequest servletRequest) {
        this(dnRegex);
        this.servletRequest = servletRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        X509Certificate[] certificateChain = (X509Certificate[]) servletRequest.getAttribute(X509_CERTIFICATE_ATTRIBUTE);

        if (ArrayUtils.isEmpty(certificateChain) || certificateChain[0] == null) {
            requestContext.abortWith(buildForbiddenResponse("No certificate chain found"));
            return;
        }

        // The certificate of the client is always the first in the chain.
        X509Certificate clientCert = certificateChain[0];
        String clientCertDN = clientCert.getSubjectDN().getName();

        if (!dnRegex.matcher(clientCertDN).matches()) {
            requestContext.abortWith(buildForbiddenResponse("Certificate subject is not recognized"));
        }
    }

    private Response buildForbiddenResponse(String message) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(message)
                .build();
    }

    @VisibleForTesting
    public Pattern getDnRegex() {
        return dnRegex;
    }
}
