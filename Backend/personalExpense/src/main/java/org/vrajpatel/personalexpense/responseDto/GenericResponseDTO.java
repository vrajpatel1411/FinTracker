package org.vrajpatel.personalexpense.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponseDTO<T> {
    private String status;
    private String error;

    private T data;

    public static <T> GenericResponseDTO<T> success(T data) {
        return GenericResponseDTO.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    public static <T> GenericResponseDTO<T> error(String error) {

        return GenericResponseDTO.<T>builder().status("failure").error(error).build();
    }

}
