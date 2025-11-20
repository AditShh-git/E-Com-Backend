package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueRs {
    private String month;   // "JAN", "FEB"...
    private Long revenue;
}

