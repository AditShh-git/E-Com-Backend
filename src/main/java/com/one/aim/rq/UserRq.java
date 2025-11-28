package com.one.aim.rq;

import java.util.Collections;
import java.util.List;

import com.one.vm.core.BaseVM;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class UserRq {

    private String fullName;

    private String email;

    private String phoneNo;

    private String password;
}

