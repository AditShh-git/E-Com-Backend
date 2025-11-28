package com.one.vm.analytics;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyOrderCountVm {
    private String date;   // "Jul 18"
    private Integer orders;
}
