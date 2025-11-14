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

    private String docId;
    private String pName;
    private String description;
    private String category;
    private long price;// legacy — contains discountedPrice for UI backwards compatibility
    private long originalPrice;     // new — original DB price
    private long discountedPrice;   // new — price after discount (if enabled)
    private int offer;
    private boolean varified;
    private byte[] image;
    private int totalItem;
    private int soldItem;
    private int quantity;

    public void setPName(String pName) { this.pName = pName; }
    public String getPName() { return this.pName; }

}
