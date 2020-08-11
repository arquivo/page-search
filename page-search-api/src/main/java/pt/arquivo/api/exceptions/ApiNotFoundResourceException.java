package pt.arquivo.api.exceptions;

public class ApiNotFoundResourceException extends RuntimeException {
    public ApiNotFoundResourceException(String s) {

        super(s);
    }

    public ApiNotFoundResourceException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
