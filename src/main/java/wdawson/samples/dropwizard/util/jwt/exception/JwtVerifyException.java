package wdawson.samples.dropwizard.util.jwt.exception;

/**
 * @author Jon Todd
 */
public class JwtVerifyException extends Exception {

    public JwtVerifyException(String s, Object... args) {
        super(String.format(s, args));
    }

    public JwtVerifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtVerifyException(Throwable throwable) {
        super(throwable);
    }

}
