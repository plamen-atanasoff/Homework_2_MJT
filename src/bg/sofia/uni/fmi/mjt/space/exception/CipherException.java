package bg.sofia.uni.fmi.mjt.space.exception;

public class CipherException extends Exception {
    CipherException(String message) {
        super(message);
    }

    CipherException(String message, Throwable cause) {
        super(message, cause);
    }
}