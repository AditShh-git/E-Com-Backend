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

	private String docId;

	private String pName;

	private String description;

	private long price;

	private String category;

	private int offer;

	private byte[] image;

}

