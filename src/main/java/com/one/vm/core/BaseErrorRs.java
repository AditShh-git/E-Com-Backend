package com.one.vm.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.one.vm.common.ErrorRs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseErrorRs implements Serializable {

    private static final long serialVersionUID = -8101327695273217603L;

    private String code;

    private String message;

    private List<ErrorRs> errors = Collections.<ErrorRs>emptyList();

}
