package com.one.aim.rs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RecommendationRs {
    private Long productId;
    private String name;
    private Double price;
    private Long score; // frequency or weight
}
