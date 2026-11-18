package in.flatmate.common;

import java.util.Map;

/** Uniform error body: { status, code, message, fieldErrors? }. */
public record ErrorResponse(int status, String code, String message, Map<String, String> fieldErrors) {

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, null);
    }
}
