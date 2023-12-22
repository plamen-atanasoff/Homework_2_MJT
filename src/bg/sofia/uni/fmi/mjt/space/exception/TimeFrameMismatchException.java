package bg.sofia.uni.fmi.mjt.space.exception;

public class TimeFrameMismatchException extends RuntimeException {
    TimeFrameMismatchException(String message) {
        super(message);
    }

    TimeFrameMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
