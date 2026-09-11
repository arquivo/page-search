package pt.arquivo.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(value = ApiRequestException.class)
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;
        ApiException apiException = new ApiException(
                e.getMessage(),
                badRequest.value(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(value = ApiNotFoundResourceException.class)
    public ResponseEntity<Object> handleApiNotFoundResourceException(ApiNotFoundResourceException e) {
        HttpStatus notFound = HttpStatus.NOT_FOUND;
        ApiException apiException = new ApiException(
                e.getMessage(),
                notFound.value(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
       return new ResponseEntity<>(apiException, notFound);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception e) {
        LOG.error("Unexpected exception handling API request", e);
        HttpStatus internalError = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiException apiException = new ApiException(
                "An unexpected error occurred while processing the request",
                internalError.value(),
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(apiException, internalError);
    }
}
