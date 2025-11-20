package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyUserActivityRs {
    private String weekLabel;
    private Long userCount;
}

