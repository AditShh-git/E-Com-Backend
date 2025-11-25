package com.one.aim.controller;

import com.one.aim.rs.RecommendationRs;
import com.one.aim.service.RecommendationService;
import com.one.vm.core.BaseDataRs;
import com.one.vm.core.BaseRs;
import com.one.vm.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recService;

    @GetMapping("/fbt/{productId}")
    public BaseRs fbt(@PathVariable Long productId,
                      @RequestParam(defaultValue = "5") int limit) {
        List<RecommendationRs> list = recService.getFrequentlyBoughtTogether(productId, limit);
        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs("Frequently bought together", list));
        return base;
    }

    @GetMapping("/pab/{productId}")
    public BaseRs pab(@PathVariable Long productId,
                      @RequestParam(defaultValue = "10") int limit) {
        List<RecommendationRs> list = recService.getPeopleAlsoBought(productId, limit);
        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs("People also bought", list));
        return base;
    }

    @GetMapping("/recommended")
    @PreAuthorize("hasAuthority('USER')")
    public BaseRs recommended(@RequestParam(defaultValue = "10") int limit) {

        List<RecommendationRs> list = recService.getRecommendedForUser(limit);

        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs("Recommended for you", list));
        return base;
    }



    @GetMapping("/trending")
    public BaseRs trending(@RequestParam(defaultValue = "7") int days,
                           @RequestParam(defaultValue = "10") int limit) {
        List<RecommendationRs> list = recService.getTrending(days, limit);
        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs("Trending products", list));
        return base;
    }

    @GetMapping("/category/{category}")
    public BaseRs topByCategory(@PathVariable String category,
                                @RequestParam(defaultValue = "10") int limit) {
        List<RecommendationRs> list = recService.getTopByCategory(category, limit);
        BaseRs base = new BaseRs();
        base.setStatus("SUCCESS");
        base.setData(new BaseDataRs("Top products in category", list));
        return base;
    }
}