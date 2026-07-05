package org.vrajpatel.userauthservice.ResponseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard response envelope returned by every endpoint in this service.
 * <p>
 * Success:  {@code success=true},  {@code data} contains the payload (null when there is no payload).
 * Failure:  {@code success=false}, {@code data} is null, {@code message} describes the problem.
 *
 * <pre>
 * {
 *   "success":   true,
 *   "message":   "Successfully logged in",
 *   "data":      { ... },
 *   "timestamp": "2026-04-10T10:15:30.123Z"
 * }
 * </pre>
 */
@Schema(description = "Standard API response envelope — every endpoint returns this structure")
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    @Schema(description = "True when the operation completed successfully", example = "true")
    private final boolean success;

    @Schema(description = "Human-readable description of the result", example = "Successfully logged in")
    private final String message;

    @Schema(description = "Response payload — null when the operation produces no data (e.g. logout, simple success)")
    private final T data;

    @Schema(description = "ISO-8601 UTC timestamp of when this response was generated", example = "2026-04-10T10:15:30.123Z")
    private final String timestamp;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().toString();
    }

    // ── Factory methods ────────────────────────────────────────────────────────

    /** Successful response with a data payload. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /** Successful response with no data payload (e.g. logout, simple acknowledgements). */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    /** Failed response — no data payload. Used by GlobalExceptionHandler and error paths. */
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Failed response with a data payload — used when a failure still needs to
     * return structured data to the client (e.g. email verification required).
     */
    public static <T> ApiResponse<T> failure(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    // ── Accessors ──────────────────────────────────────────────────────────────

    public boolean isSuccess()    { return success; }
    public String  getMessage()   { return message; }
    public T       getData()      { return data; }
    public String  getTimestamp() { return timestamp; }
}
