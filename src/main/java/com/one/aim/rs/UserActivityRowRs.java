package com.one.aim.rs;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityRowRs {

    private String userName;
    private Object userId;
    private String activityType;
    private String description;
    private LocalDateTime time;
}
