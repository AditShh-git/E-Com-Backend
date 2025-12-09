package com.one.aim.rq;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleAnnouncementRq {
    private String type;
    private String title;
    private String description;
    private Long imageFileId;
    private Long redirectRefId;
    private String redirectUrl;
}


