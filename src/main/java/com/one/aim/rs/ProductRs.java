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
    private Integer quantity = 1;

    // Seller details
    private String sellerName;

    private String slug;

    // Multiple image URLs
    private List<String> imageUrls = new ArrayList<>();

    // Optional – used in shareable links
    private String shareMessage;

}
