package com.one.aim.rs;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRS {

    private Long id;
    private String type;
    private String title;
    private String description;
    private String imageUrl;
    private String redirectUrl;
    private Boolean isRead;
    private String timestamp;

}
