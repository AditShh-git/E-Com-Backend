package com.one.aim.rq;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRq {

    private Long id; // for update

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category must be under 100 characters")
    private String name;

    private boolean active = true;
}
