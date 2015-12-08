package wdawson.samples.dropwizard.util.jwt;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wdawson.samples.dropwizard.util.jwt.dto.JwtClaims;
import wdawson.samples.dropwizard.util.jwt.exception.JwtIssueException;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.UUID;

/**
 * @author Jon Todd
 */
public class JwtIssuer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtIssuer.class);

    /**
     * Private portion of the key used for signing the JWT
     */
    private RSAPrivateKey privateKey;

    /**
     * Certificate of the key used to sign the JWT
     */
    private X509Certificate signingCertificate;

    public JwtIssuer(RSAPrivateKey privateKey, X509Certificate signingCertificate) {
        this.privateKey = privateKey;
        this.signingCertificate = signingCertificate;
    }

    public String issueToken(JwtClaims claims) throws JwtIssueException {
        JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder();
        if (Strings.isNullOrEmpty(claims.getJwtId())) {
            claimsSetBuilder.jwtID(UUID.randomUUID().toString());
        } else {
            claimsSetBuilder.jwtID(claims.getJwtId());
        }
        claimsSetBuilder.subject(claims.getSubject());
        claimsSetBuilder.issueTime(claims.getIssuedAt());
        claimsSetBuilder.notBeforeTime(claims.getNotBefore());
        claimsSetBuilder.expirationTime(claims.getExpirationTime());
        for (String key : claims.getCustomClaimKeys()) {
            claimsSetBuilder.claim(key, claims.getClaim(key));
        }

        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);

        // Put the signing certificate in the header if present. This allows verifier to follow certificate chain
        // back to the root for verification.
        if (signingCertificate != null) {
            if (!Strings.isNullOrEmpty(claims.getIssuer())) {
                LOGGER.warn("Issuer {} passed in from claims will be ignored. Issuer is determined from signing certificate: {}",
                        claims.getIssuer(), signingCertificate.getSubjectDN().getName());
            }
            LOGGER.info("DN: {}", signingCertificate.getSubjectDN());
            claimsSetBuilder.issuer(signingCertificate.getSubjectDN().getName());
            try {
                headerBuilder.x509CertChain(ImmutableList.of(Base64.encode(signingCertificate.getEncoded())));
            } catch (CertificateEncodingException e) {
                String message = String.format("Failed to get encoded value for signing certificate. Subject: '%s'",
                        signingCertificate.getSubjectDN());
                throw new JwtIssueException(message, e);
            }
        } else {
            claimsSetBuilder.issuer(claims.getIssuer());
        }

        JWSObject jwsObject = new JWSObject(headerBuilder.build(), new Payload(claimsSetBuilder.build().toJSONObject()));
        RSASSASigner rsaSigner = new RSASSASigner(privateKey);

        try {
            jwsObject.sign(rsaSigner);
        } catch (JOSEException e) {
            throw new RuntimeException("An error occurred while signing the JWT", e);
        }

        return jwsObject.serialize();
    }
}
