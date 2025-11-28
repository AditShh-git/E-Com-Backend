package com.one.aim.rs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class StandardRs {
    private boolean success;
    private String message;
    private Object data;
    private LocalDateTime timestamp;
}
