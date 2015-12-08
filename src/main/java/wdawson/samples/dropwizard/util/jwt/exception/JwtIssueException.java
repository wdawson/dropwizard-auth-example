package wdawson.samples.dropwizard.util.jwt.exception;

/**
 * @author Jon Todd
 */
public class JwtIssueException extends Exception {

    public JwtIssueException(Throwable throwable) {
        super(throwable);
    }

    public JwtIssueException(String message, Throwable cause) {
        super(message, cause);
    }
}
