package com.one.aim.rq;

import com.one.vm.core.BaseVM;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartRq extends BaseVM {

    private static final long serialVersionUID = 1L;

    private String docId;   // cartId when updating

    private Long productId; // REQUIRED for creating cart snapshot

    private String pName;       // (ignored during creation, used only when updating)
    private String description; // (ignored during creation)
    private String category;    // (ignored during creation)

    private long price;     // (ignored during creation)
    private int offer;      // currently not used, but kept for future
    private int returnDay;

    private boolean enabled = true;

    private byte[] image; // optional snapshot image

}
