package com.one.exception;

public class InvalidUserRoleException extends RuntimeException {

	private static final long serialVersionUID = -2235594649812789030L;

	public InvalidUserRoleException(String message) {
		super(message);
	}

}
