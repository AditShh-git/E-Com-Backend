package com.one.aim.rq;

import com.one.vm.core.BaseVM;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductRq extends BaseVM {

    private static final long serialVersionUID = 1L;

    private String docId;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must be under 100 characters")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private Double price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    private Long categoryId;
    private String customCategoryName;
    private String categoryName;

    //  Multiple images for upload
    private List<MultipartFile> images;
}
