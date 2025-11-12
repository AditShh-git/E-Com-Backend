package com.one.exception;

public class NotAuthorizedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotAuthorizedException(String msg) {
		super(msg);
	}

}
