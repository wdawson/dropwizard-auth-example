package wdawson.samples.dropwizard.util.jwt.exception;

/**
 * @author Jon Todd
 */
public class JwtParseException extends Exception {

    public JwtParseException(String s) {
        super(s);
    }

    public JwtParseException(Throwable throwable) {
        super(throwable);
    }

}
