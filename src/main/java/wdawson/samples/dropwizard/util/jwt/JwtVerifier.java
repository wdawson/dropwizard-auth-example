package wdawson.samples.dropwizard.util.jwt;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.Base64;
import net.minidev.json.JSONObject;
import org.bouncycastle.asn1.x500.X500Name;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Seconds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdawson.samples.dropwizard.util.jwt.dto.Jwt;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;
import wdawson.samples.dropwizard.util.jwt.exception.JwtVerifyException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jon Todd
 */
public class JwtVerifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtVerifier.class);

    protected static final String ISSUE_TIME_EXCEPTION = "Issue time %s is after current time %s";
    protected static final String EXP_TIME_EXCEPTION = "Token has expired (expiration: %s, current time: %s)";
    protected static final String NOT_BEFORE_EXCEPTION = "Token is not yet valid (not before: %s, current time: %s)";
    protected static final String TIME_RANGE_EXCEPTION = "Time range is not valid: expiration time %s is before " +
            "issue time %s";
    protected static final String JWS_VERIFICATION_EXCEPTION = "Failed JWS verification";
    protected static final String SIGNATURE_MATCH_EXCEPTION = "Could not verify JWT signature using signing " +
            "certificate subject '%s'.";
    protected static final String SIGNER_MATCH_EXCEPTION = "Signer DN doesn't match allowed pattern. " +
            "Signer DN: '%s' allowed pattern '%s'";
    protected static final String MISSING_CERT_CHAIN_EXCEPTION = "Cannot validate signature, JWT header is missing " +
            "certificate chain";
    protected static final String ISSUER_MISMATCH_EXCEPTION = "JWT issuer doesn't match certificate signer. " +
            "Issuer: '%s' Signer DN: '%s'";
    protected static final String ISSUER_FORMAT_EXCEPTION = "Invalid issuer format";

    private static final int TIME_BUFFER_IN_SECONDS = 2 * DateTimeConstants.SECONDS_PER_MINUTE;

    /**
     * Regex pattern for verifying JWT is signed by an allowed signer. The subject DN of the signing certificate must
     * match this pattern. This particularly useful for locking down access when there are multiple issuing certs from
     * the same root CA with differing access domains e.g. to prevent access between dev and prod services.
     */
    private final Pattern allowedSignerDnPattern;

    public JwtVerifier(Pattern allowedSignerPattern, List<X509Certificate> certificateChain) {
        this.allowedSignerDnPattern = allowedSignerPattern;
    }

    public JwtClaims authenticate(Jwt jwt) throws JwtVerifyException {
        JWSObject jwsObject = jwt.getJwsObject();
        JSONObject payload = jwsObject.getPayload().toJSONObject();

        // Do the verification steps in order of performance cost / likelihood of failing. Signature verification is
        // costly so we do that last.
        assertTimeValid(payload);

        X509Certificate signingCertificate = getSigningCertificate(jwsObject);

        assertAllowedSignerDn(signingCertificate);
        assertJwtIssuerMatchesSigner(signingCertificate, payload.get(JwtClaims.ISSUER).toString());
        assertSignatureValid(jwsObject, signingCertificate);

        //
        // IMPORTANT!
        //
        // You need to assert that signing certificate is valid by walking trust chain. This will depend on
        // your CA hierarchy and revocation policy. This has been left for the implementer to decide but is a
        // critical part of the verification process.

        LOGGER.debug("Verified JWT (jti={})", payload.get(JwtClaims.JWT_ID));
        JwtClaims.JwtClaimsBuilder builder = JwtClaims.JwtClaimsBuilder.newInstanceFromClaimsMap(payload);
        return builder.build();
    }

    private void assertTimeValid(JSONObject payload) throws JwtVerifyException {
        Seconds currentTime = Seconds.seconds((int) (DateTime.now().getMillis() / DateTimeConstants.MILLIS_PER_SECOND));

        Seconds issueTime = Seconds.seconds(((Long) payload.get(JwtClaims.ISSUED_AT)).intValue());
        if (issueTime.isGreaterThan(currentTime.plus(TIME_BUFFER_IN_SECONDS))) {
            throw new JwtVerifyException(ISSUE_TIME_EXCEPTION, issueTime, currentTime);
        }

        if (payload.get(JwtClaims.NOT_BEFORE) != null) {
            Seconds notBeforeTime = Seconds.seconds(((Long) payload.get(JwtClaims.NOT_BEFORE)).intValue());
            if (currentTime.isLessThan(notBeforeTime.minus(TIME_BUFFER_IN_SECONDS))) {
                throw new JwtVerifyException(NOT_BEFORE_EXCEPTION, notBeforeTime, currentTime);
            }
        }

        if (payload.get(JwtClaims.EXPIRATION_TIME) != null) {
            Seconds expTime = Seconds.seconds(((Long) payload.get(JwtClaims.EXPIRATION_TIME)).intValue());
            if (expTime.isLessThan(currentTime.minus(TIME_BUFFER_IN_SECONDS))) {
                throw new JwtVerifyException(EXP_TIME_EXCEPTION, expTime, currentTime);
            }
            if (expTime.isLessThan(issueTime)) {
                throw new JwtVerifyException(TIME_RANGE_EXCEPTION, expTime, issueTime);
            }
        }
    }

    private void assertAllowedSignerDn(X509Certificate certificate) throws JwtVerifyException {
        String dn = certificate.getSubjectDN().getName();
        boolean signerMatches = allowedSignerDnPattern.matcher(dn).matches();
        if (!signerMatches) {
            throw new JwtVerifyException(SIGNER_MATCH_EXCEPTION, dn, allowedSignerDnPattern.pattern());
        }
    }

    private void assertJwtIssuerMatchesSigner(X509Certificate certificate, String issuer) throws JwtVerifyException {
        try {
            X500Name dnName = new X500Name(certificate.getSubjectDN().getName());
            X500Name issuerName = new X500Name(issuer);
            if (!issuerName.equals(dnName)) {
                throw new JwtVerifyException(ISSUER_MISMATCH_EXCEPTION, issuerName, dnName);
            }
        } catch (IllegalArgumentException e) {
            throw new JwtVerifyException(ISSUER_FORMAT_EXCEPTION, e);
        }
    }

    private X509Certificate getSigningCertificate(JWSObject jwsObject) throws JwtVerifyException {
        List<X509Certificate> certificateChain = decodeCertificates(jwsObject.getHeader().getX509CertChain());
        if (!certificateChain.isEmpty()) {
            // Per the JWS spec: 'The certificate containing the public key corresponding to the key used to
            // digitally sign the JWS MUST be the first certificate.'
            //
            // We are currently ignoring the rest of the chain because we know our implementation won't provide
            // it and we instead download it via AIA url. In a future release when the CA can support it we'll
            // move to using URLs instead of putting any of the chain in the token to keep it small.
            return certificateChain.get(0);
        }
        throw new JwtVerifyException(MISSING_CERT_CHAIN_EXCEPTION);
    }

    private void assertSignatureValid(JWSObject jwsObject, X509Certificate signingCertificate) throws JwtVerifyException {
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) signingCertificate.getPublicKey());
        try {
            // Verify the JWT was signed by the signing certificate
            if (!jwsObject.verify(verifier)) {
                // The contract on the verify method above is odd. Some failure scenarios result in returning false
                // while others throw. To distinguish between the two we throw different exception for each case.
                String message = String.format(SIGNATURE_MATCH_EXCEPTION, signingCertificate.getSubjectDN().getName());
                throw new JwtVerifyException(message);
            }
        } catch (JOSEException e) {
            throw new JwtVerifyException(JWS_VERIFICATION_EXCEPTION, e);
        }
    }

    /**
     * Takes JWS 'x5c' encoded header value and decodes it to a list of X509Certificates.
     *
     * @param encodedCertificates list of base64 encoded certificate values
     * @return
     */
    private List<X509Certificate> decodeCertificates(List<Base64> encodedCertificates) {
        if (encodedCertificates == null) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<X509Certificate> certificates = ImmutableList.builder();
        for (Base64 encodedCertificate : encodedCertificates) {
            try {
                InputStream is = new ByteArrayInputStream(encodedCertificate.decode());
                X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                        .generateCertificate(is);
                certificates.add(certificate);
            } catch (CertificateException e) {
                Throwables.propagate(e);
            }
        }
        return certificates.build();
    }
}
