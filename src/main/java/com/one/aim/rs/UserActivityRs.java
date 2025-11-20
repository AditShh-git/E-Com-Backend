package com.one.aim.rs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityRs {

    private Long totalActiveUsers;
    private Integer percentChange;

    private List<WeeklyUserActivityRs> weeklyActivity;
}

