package com.one.aim.rs.data;

import com.one.aim.rs.ProductRs;
import com.one.vm.core.BaseDataRs;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDataRs extends BaseDataRs {

    private static final long serialVersionUID = 1L;

    private ProductRs product;

    public ProductDataRs(String message) {
        super(message);
    }

    public ProductDataRs(String message, ProductRs product) {
        super(message);
        this.product = product;
    }
}