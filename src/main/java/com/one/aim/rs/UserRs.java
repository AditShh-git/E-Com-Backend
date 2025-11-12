package com.one.aim.rs;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRs implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long docId;

	private String fullName;

	private String email;

	private String phoneNo;

	// private List<AttachmentRs> atts = Collections.emptyList();
	private String roll;

	private byte[] image;

	public UserRs(String fullName, Long docId) {
		this.fullName = fullName;
		this.docId = docId;
	}
}
