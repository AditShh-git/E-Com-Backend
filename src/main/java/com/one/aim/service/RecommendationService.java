package com.one.aim.service;

import com.one.aim.rs.RecommendationRs;

import java.util.List;

public interface RecommendationService {

    List<RecommendationRs> getFrequentlyBoughtTogether(Long productId, int limit);

    List<RecommendationRs> getPeopleAlsoBought(Long productId, int limit);

    List<RecommendationRs> getRecommendedForUser(int limit); // category + exclude

    List<RecommendationRs> getTrending(int days, int limit); // global trending in last N days

    List<RecommendationRs> getTopByCategory(String category, int limit);
}