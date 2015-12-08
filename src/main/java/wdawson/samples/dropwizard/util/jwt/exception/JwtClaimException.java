package wdawson.samples.dropwizard.util.jwt.exception;

/**
 * @author Jon Todd
 */
public class JwtClaimException extends RuntimeException {

    public JwtClaimException(String s) {
        super(s);
    }

}
