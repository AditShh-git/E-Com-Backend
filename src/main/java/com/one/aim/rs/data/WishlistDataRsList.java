package com.one.aim.rs.data;

import java.util.List;

import com.one.aim.rs.WishlistRs;
import com.one.vm.core.BaseDataRs;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WishlistDataRsList {

    private String message;
    private List<WishlistRs> wishlist;
}
