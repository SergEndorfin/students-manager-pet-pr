package com.vinsguru.students.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank(message = "name must not be blank") @Size(max = 255, message = "name must not exceed 255 characters") String name,
        @NotNull(message = "age must not be null") @Min(value = 1, message = "age must be greater than 0") Integer age,
        @NotBlank(message = "class must not be blank") @Size(max = 255, message = "class must not exceed 255 characters") String studentClass
) {
}
