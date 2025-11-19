package com.one.aim.rs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPageResponse {
    private List<UserRs> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}

