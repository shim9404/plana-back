package com.example.plana.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBody {
    private boolean success;
    private Integer code;
    private String message;
    private Object data;

}
