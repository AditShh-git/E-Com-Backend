package com.one.aim.rs;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsRs {

    private SummaryCardsRs summary;
    private SalesPerformanceRs salesPerformance;
    private UserActivityRs userActivity;
    private List<SalesTableRowRs> salesTable;
}

