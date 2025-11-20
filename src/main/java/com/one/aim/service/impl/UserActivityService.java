package com.one.aim.service.impl;

import com.one.aim.bo.UserActivityBO;
import com.one.aim.repo.UserActivityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepo repo;

    public void log(Long userId, String type, String description) {
        UserActivityBO activity = UserActivityBO.builder()
                .userId(userId)
                .activityType(type)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(activity);
    }
}
