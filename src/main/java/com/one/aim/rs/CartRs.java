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
public class CartRs implements Serializable {

	private static final long serialVersionUID = 1L;

    private Long id;
    private String pname;
    private String description;
    private long price;

    private String category;

    private int offer;
    private int returnDay;

    private int quantity; // user-selected qty

    private String imageUrl; // generated from file service

}
