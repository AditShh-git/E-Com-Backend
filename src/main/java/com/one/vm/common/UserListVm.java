package com.one.vm.common;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserListVm {

	private Long id;
    private String fullName;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;
}
