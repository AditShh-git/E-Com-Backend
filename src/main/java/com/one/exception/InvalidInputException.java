package com.one.exception;

import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidInputException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private List<String> errors = Collections.emptyList();

	public InvalidInputException(String msg) {
		super(msg);
	}

	public InvalidInputException(String message, List<String> errors) {
		super(message);
		this.errors = errors;
	}

}
