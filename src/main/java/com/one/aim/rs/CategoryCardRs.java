package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCardRs {

    private Long id;
    private String name;
    private String image;     // Public URL
    private Long productCount;
}
