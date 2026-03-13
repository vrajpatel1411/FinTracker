package org.vrajpatel.personalexpense.responseDto;

import lombok.Data;

@Data
public class PresignedUrlResponse {
    private String url;
    private String key;

    public PresignedUrlResponse(String url, String key) {
        this.url = url;
        this.key = key;
    }
}
