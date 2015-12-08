package wdawson.samples.dropwizard.util.jwt.dto;

import com.google.common.collect.Sets;
import org.joda.time.DateTimeConstants;
import wdawson.samples.dropwizard.util.jwt.exception.JwtClaimException;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Jon Todd
 */
public final class JwtClaims {

    public static final String JWT_ID = "jti";
    public static final String SUBJECT = "sub";
    public static final String AUDIENCE = "aud";
    public static final String ISSUER = "iss";
    public static final String EXPIRATION_TIME = "exp";
    public static final String NOT_BEFORE = "nbf";
    public static final String ISSUED_AT = "iat";
    private static final Set<String> RESERVED_CLAIMS = Sets.newHashSet(JWT_ID, SUBJECT, AUDIENCE, ISSUER, ISSUED_AT,
            EXPIRATION_TIME, NOT_BEFORE, ISSUED_AT);

    private final Map<String, Object> claimsMap;
    private final Set<String> customClaimKeys;

    private JwtClaims(Map<String, Object> claimsMap, Set<String> customClaimKeys) {
        this.claimsMap = claimsMap;
        this.customClaimKeys = customClaimKeys;
    }

    public static final class JwtClaimsBuilder {

        private Map<String, Object> claimsMap;

        private JwtClaimsBuilder() {
            this.claimsMap = new HashMap<String, Object>();
        }

        private JwtClaimsBuilder(Map<String, Object> claimsMap) {
            this.claimsMap = claimsMap;
        }

        public static JwtClaimsBuilder newInstance() {
            return new JwtClaimsBuilder();
        }

        public static JwtClaimsBuilder newInstanceFromClaimsMap(Map<String, Object> claimsMap) {
            return new JwtClaimsBuilder(claimsMap);
        }

        public JwtClaimsBuilder addJwtId(String jwtId) {
            addClaim(JWT_ID, jwtId);
            return this;
        }

        public JwtClaimsBuilder addSubject(String subject) {
            addClaim(SUBJECT, subject);
            return this;
        }

        public JwtClaimsBuilder addAudience(String audience) {
            addClaim(AUDIENCE, audience);
            return this;
        }

        public JwtClaimsBuilder addIssuer(String issuer) {
            addClaim(ISSUER, issuer);
            return this;
        }

        public JwtClaimsBuilder addExpirationTime(Date expirationTime) {
            addDateClaim(EXPIRATION_TIME, expirationTime);
            return this;
        }

        public JwtClaimsBuilder addIssuedAt(Date issuedAt) {
            addDateClaim(ISSUED_AT, issuedAt);
            return this;
        }

        public JwtClaimsBuilder addNotBefore(Date notBefore) {
            addDateClaim(NOT_BEFORE, notBefore);
            return this;
        }

        public JwtClaimsBuilder addCustomClaim(String claim, Object value) {
            if (JwtClaims.RESERVED_CLAIMS.contains(claim)) {
                throw new JwtClaimException(String.format("Claim %s is a reserved claim", claim));
            }
            addClaim(claim, value);
            return this;
        }

        private void addClaim(String claim, Object value) {
            claimsMap.put(claim, value);
        }

        private void addDateClaim(String claim, Date date) {
            claimsMap.put(claim, date.getTime() / DateTimeConstants.MILLIS_PER_SECOND);
        }

        public JwtClaims build() {
            Set<String> customClaimKeys = new HashSet<String>();
            for (String key : claimsMap.keySet()) {
                if (!JwtClaims.RESERVED_CLAIMS.contains(key)) {
                    customClaimKeys.add(key);
                }
            }
            return new JwtClaims(claimsMap, customClaimKeys);
        }
    }

    public Object getClaim(String key) {
        return claimsMap.get(key);
    }

    public Set<String> getCustomClaimKeys() {
        return customClaimKeys;
    }

    public String getJwtId() {
        return getClaimAsString(JWT_ID);
    }

    public String getSubject() {
        return getClaimAsString(SUBJECT);
    }

    public String getAudience() {
        return getClaimAsString(AUDIENCE);
    }

    public String getIssuer() {
        return getClaimAsString(ISSUER);
    }

    public Date getIssuedAt() {
        return getClaimAsDate(ISSUED_AT);
    }

    public Date getExpirationTime() {
        return getClaimAsDate(EXPIRATION_TIME);
    }

    public Date getNotBefore() {
        return getClaimAsDate(NOT_BEFORE);
    }

    private String getClaimAsString(String key) {
        if (getClaim(key) == null) {
            return null;
        }
        return getClaim(key).toString();
    }

    private Date getClaimAsDate(String key) {
        if (getClaim(key) == null) {
            return null;
        }
        return new Date((Long) getClaim(key) * DateTimeConstants.MILLIS_PER_SECOND);
    }
}
