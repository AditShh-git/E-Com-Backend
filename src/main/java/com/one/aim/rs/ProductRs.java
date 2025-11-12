package com.one.aim.rs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductRs implements Serializable {

    private static final long serialVersionUID = 1L;

    private String docId;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String categoryName;
    private String sellerName;
    private String slug;
    private List<String> imageUrls = new ArrayList<>();

    // ✅ NEW FIELD
    private String shareMessage;

}
