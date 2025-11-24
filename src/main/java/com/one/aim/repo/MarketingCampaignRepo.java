package com.one.aim.repo;

import com.one.aim.bo.MarketingCampaignBO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingCampaignRepo extends JpaRepository<MarketingCampaignBO,Long> {
}
