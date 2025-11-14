package com.one.aim.rs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WishlistRs implements Serializable {

	private static final long serialVersionUID = 1L;

    private String productId;
    private String name;
    private String description;
    private double price;
    private String category;
    private Long imageId;

}

