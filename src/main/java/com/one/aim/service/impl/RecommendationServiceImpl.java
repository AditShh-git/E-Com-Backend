package com.one.aim.service.impl;

import com.one.aim.bo.ProductBO;
import com.one.aim.repo.OrderItemBORepo;
import com.one.aim.repo.ProductRepo;
import com.one.aim.rs.RecommendationRs;
import com.one.aim.service.RecommendationService;
import com.one.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final OrderItemBORepo orderItemRepo;
    private final ProductRepo productRepo;

    @Override
    public List<RecommendationRs> getFrequentlyBoughtTogether(Long productId, int limit) {
        var rows = orderItemRepo.findFrequentlyBoughtTogether(productId, PageRequest.of(0, limit));
        return rowsToRs(rows);
    }

    @Override
    public List<RecommendationRs> getPeopleAlsoBought(Long productId, int limit) {
        var rows = orderItemRepo.findPeopleAlsoBought(productId, PageRequest.of(0, limit));
        return rowsToRs(rows);
    }

    @Override
    public List<RecommendationRs> getRecommendedForUser(int limit) {

        // Logged-in user
        Long userId = AuthUtils.findLoggedInUser().getDocId();

        // 1. Products user already purchased
        List<Long> purchasedIds = orderItemRepo.findProductIdsPurchasedByUser(userId);

        // 2. Trending products (last 3 months)
        LocalDateTime start = LocalDateTime.now().minusMonths(3);
        List<Object[]> trending = orderItemRepo.findTopSelling(
                start,
                LocalDateTime.now(),
                PageRequest.of(0, 100)
        );

        // 3. Score products (exclude already purchased)
        Map<Long, Long> scoreMap = new LinkedHashMap<>();

        for (Object[] row : trending) {
            Long pId = ((Number) row[0]).longValue();
            Long score = ((Number) row[2]).longValue(); // quantity sold

            if (purchasedIds.contains(pId)) continue;

            scoreMap.put(pId, score);
            if (scoreMap.size() >= limit) break;
        }

        // 4. Fetch product details
        return fetchProductsFromIds(scoreMap);
    }


    @Override
    public List<RecommendationRs> getTrending(int days, int limit) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        List<Object[]> rows = orderItemRepo.findTopSelling(start, LocalDateTime.now(), PageRequest.of(0, limit));
        return rowsToRs(rows);
    }

    @Override
    public List<RecommendationRs> getTopByCategory(String category, int limit) {
        LocalDateTime start = LocalDateTime.now().minusMonths(3);
        List<Object[]> rows = orderItemRepo.findTopSellingByCategory(category, start, LocalDateTime.now(), PageRequest.of(0, limit));
        return rowsToRs(rows);
    }

    // ---------- helpers ----------
    private List<RecommendationRs> rowsToRs(List<Object[]> rows) {
        if (rows == null) return Collections.emptyList();
        return rows.stream().map(r -> {
            Long productId = ((Number) r[0]).longValue();
            String name = r[1] == null ? "" : r[1].toString();
            Long score = r[2] == null ? 0L : ((Number) r[2]).longValue();
            Double price = productRepo.findById(productId).map(p -> p.getPrice() == null ? 0.0 : p.getPrice()).orElse(0.0);
            return new RecommendationRs(productId, name, price, score);
        }).toList();
    }

    private List<RecommendationRs> fetchProductsFromIds(Map<Long, Long> scoreMap) {
        List<Long> ids = new ArrayList<>(scoreMap.keySet());
        List<ProductBO> products = productRepo.findAllById(ids);
        Map<Long, ProductBO> map = products.stream().collect(Collectors.toMap(ProductBO::getId, p -> p));
        List<RecommendationRs> res = new ArrayList<>();
        for (Long id : ids) {
            ProductBO p = map.get(id);
            if (p == null) continue;
            res.add(new RecommendationRs(id, p.getName(), p.getPrice() == null ? 0.0 : p.getPrice(), scoreMap.get(id)));
        }
        return res;
    }
}