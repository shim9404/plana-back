package com.example.plana.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "object", nullable = true, example = "null")
public final class EmptyData {
    private EmptyData() {}
}