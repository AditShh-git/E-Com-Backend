package com.one.aim.repo;

import com.one.aim.bo.MarketingConversionBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingConversionRepo extends JpaRepository<MarketingConversionBO,Long> {

    @Query("""
           SELECT mc.campaign.id,
                  mc.campaign.name,
                  COUNT(mc.id),
                  SUM(mc.revenueGenerated)
           FROM MarketingConversionBO mc
           GROUP BY mc.campaign.id, mc.campaign.name
           """)
    List<Object[]> getCampaignAggregates();
}
