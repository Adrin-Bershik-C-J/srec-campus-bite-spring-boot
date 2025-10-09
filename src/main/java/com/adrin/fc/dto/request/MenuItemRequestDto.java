package com.adrin.fc.dto.request;

import com.adrin.fc.enums.MenuTag;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequestDto {
    @NotBlank
    private String itemName;

    @Min(0)
    private Double price;

    @NotNull
    private MenuTag tag;
}
