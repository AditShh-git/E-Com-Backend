package com.one.aim.rs.data;

import com.one.aim.rs.ProductRs;
import com.one.vm.core.BaseDataRs;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductDataRsList extends BaseDataRs {

    private static final long serialVersionUID = 1L;

    private List<ProductRs> products;

    public ProductDataRsList(String message) {
        super(message);
    }

    public ProductDataRsList(String message, List<ProductRs> products) {
        super(message);
        this.products = products;
    }
}

