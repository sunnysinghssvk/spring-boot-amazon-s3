package com.amazon.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class S3ApiResponse {
    private String message;
    private int httpStatusCode;
    private Object fileData;
}
