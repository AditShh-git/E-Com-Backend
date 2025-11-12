package com.one.vm.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.one.constants.StringConstants;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseRs extends BaseVM {

	private static final long serialVersionUID = 4334669267115607360L;

	private String status = StringConstants.EMPTY;

	private BaseErrorRs error;

	private BaseDataRs data;

}
